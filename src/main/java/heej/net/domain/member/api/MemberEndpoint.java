package heej.net.domain.member.api;
import heej.net.domain.member.api.dto.*;
import heej.net.domain.member.api.dto.*;
import heej.net.domain.member.usecase.MemberUseCase;
import heej.net.domain.member.util.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberEndpoint {
    private final MemberUseCase memberUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("회원가입 요청: {}", request.getEmail());
        MemberResponse response = memberUseCase.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: {}", request.getEmail());
        LoginResponse response = memberUseCase.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("토큰 갱신 요청");
        LoginResponse response = memberUseCase.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        Long memberId = jwtTokenProvider.getMemberId(token);
        log.info("로그아웃 요청: memberId={}", memberId);
        memberUseCase.logout(memberId, token);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        Long memberId = jwtTokenProvider.getMemberId(token);
        log.info("내 정보 조회 요청: memberId={}", memberId);
        MemberResponse response = memberUseCase.getMemberInfo(memberId);
        return ResponseEntity.ok(response);
    }
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다");
    }
}