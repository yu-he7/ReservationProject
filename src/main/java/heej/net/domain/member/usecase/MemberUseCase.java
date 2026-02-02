package heej.net.domain.member.usecase;
import heej.net.domain.member.api.dto.LoginRequest;
import heej.net.domain.member.api.dto.LoginResponse;
import heej.net.domain.member.api.dto.MemberResponse;
import heej.net.domain.member.api.dto.SignupRequest;
import heej.net.domain.member.api.dto.*;
public interface MemberUseCase {
    MemberResponse signup(SignupRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    void logout(Long memberId, String accessToken);
    MemberResponse getMemberInfo(Long memberId);
}