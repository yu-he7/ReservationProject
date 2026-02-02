package heej.net.domain.reservation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCancelResponse {
    private Boolean success;
    private String message;
    private String reservationId;
    private String cancelReason;
    private String roomName;
}
