package heej.net.domain.reservation.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCancelRequest {
    @NotNull(message = "취소 사유는 필수입니다.")
    private String cancelReason;
}
