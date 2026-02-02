package heej.net.domain.member.api.dto;
import heej.net.domain.member.model.MemberRole;
import heej.net.domain.member.model.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private MemberRole role;
    private MemberStatus status;
    private LocalDateTime createdAt;
}