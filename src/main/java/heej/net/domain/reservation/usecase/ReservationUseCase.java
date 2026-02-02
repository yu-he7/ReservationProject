package heej.net.domain.reservation.usecase;

import heej.net.domain.reservation.api.dto.CheckAvailabilityResponse;
import heej.net.domain.reservation.api.dto.CreateReservationRequest;
import heej.net.domain.reservation.api.dto.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReservationUseCase {

    ReservationResponse createReservation(Long memberId, CreateReservationRequest request);

    void cancelReservation(Long memberId, Long reservationId, String reason);

    List<ReservationResponse> getMyReservations(Long memberId);

    ReservationResponse getReservation(Long memberId, Long reservationId);

    List<CheckAvailabilityResponse> checkAvailability(
            Long accommodationId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer guestCount
    );


}

