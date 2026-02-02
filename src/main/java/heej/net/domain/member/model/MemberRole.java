package heej.net.domain.member.model;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum MemberRole {
    USER("ROLE_USER", "일반 사용자"),
    HOST("ROLE_HOST", "숙소 제공자"),
    ADMIN("ROLE_ADMIN", "관리자");
    private final String key;
    private final String description;
}