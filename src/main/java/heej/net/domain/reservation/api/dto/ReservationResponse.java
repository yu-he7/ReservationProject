package heej.net.domain.reservation.api.dto;

import heej.net.domain.reservation.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long roomId;
    private String roomName;
    private String accommodationName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private String specialRequests;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

