package heej.net.domain.member.usecase;
import heej.net.domain.member.api.dto.LoginRequest;
import heej.net.domain.member.api.dto.LoginResponse;
import heej.net.domain.member.api.dto.MemberResponse;
import heej.net.domain.member.api.dto.SignupRequest;
import heej.net.domain.member.api.dto.*;
import heej.net.domain.member.infra.MemberInfra;
import heej.net.domain.member.model.Member;
import heej.net.domain.member.model.MemberRole;
import heej.net.domain.member.model.MemberStatus;
import heej.net.domain.member.util.JwtTokenProvider;
import heej.net.domain.member.util.RedisTokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MemberUseCaseImpl implements MemberUseCase {
    private final MemberInfra memberInfra;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final Optional<RedisTokenManager> redisTokenManager;

    public MemberUseCaseImpl(
            MemberInfra memberInfra,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            @Autowired(required = false) RedisTokenManager redisTokenManager) {
        this.memberInfra = memberInfra;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTokenManager = Optional.ofNullable(redisTokenManager);
    }
    @Override
    @Transactional
    public MemberResponse signup(SignupRequest request) {
        if (memberInfra.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : MemberRole.USER)
                .status(MemberStatus.ACTIVE)
                .build();
        Member savedMember = memberInfra.save(member);
        log.info("회원가입 완료: {}", savedMember.getEmail());
        return toMemberResponse(savedMember);
    }
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Member member = memberInfra.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new IllegalStateException("활성화되지 않은 회원입니다: " + member.getStatus());
        }
        String accessToken = jwtTokenProvider.generateAccessToken(
                member.getId(), member.getEmail(), member.getRole().getKey());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                member.getId(), member.getEmail(), member.getRole().getKey());

        // Redis가 활성화된 경우에만 Refresh Token 저장
        redisTokenManager.ifPresent(manager ->
                manager.saveRefreshToken(member.getId(), refreshToken, jwtTokenProvider.getRefreshTokenExpiration()));

        log.info("로그인 성공: {}", member.getEmail());
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
                .build();
    }
    @Override
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다");
        }
        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        String email = jwtTokenProvider.getEmail(refreshToken);
        String role = jwtTokenProvider.getRole(refreshToken);

        // Redis가 활성화된 경우에만 저장된 Refresh Token 검증
        if (redisTokenManager.isPresent()) {
            String savedRefreshToken = redisTokenManager.get().getRefreshToken(memberId);
            if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
                throw new IllegalArgumentException("만료되거나 유효하지 않은 Refresh Token입니다");
            }
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(memberId, email, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId, email, role);

        // Redis가 활성화된 경우에만 새로운 Refresh Token 저장
        redisTokenManager.ifPresent(manager ->
                manager.saveRefreshToken(memberId, newRefreshToken, jwtTokenProvider.getRefreshTokenExpiration()));

        log.info("토큰 갱신 성공: memberId={}", memberId);
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
                .build();
    }
    @Override
    @Transactional
    public void logout(Long memberId, String accessToken) {
        // Redis가 활성화된 경우에만 Refresh Token 삭제 및 블랙리스트 추가
        redisTokenManager.ifPresent(manager -> {
            manager.deleteRefreshToken(memberId);
            manager.addToBlacklist(accessToken, jwtTokenProvider.getAccessTokenExpiration());
        });
        log.info("로그아웃 성공: memberId={}", memberId);
    }
    @Override
    public MemberResponse getMemberInfo(Long memberId) {
        Member member = memberInfra.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));
        return toMemberResponse(member);
    }
    private MemberResponse toMemberResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .phone(member.getPhone())
                .role(member.getRole())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .build();
    }
}