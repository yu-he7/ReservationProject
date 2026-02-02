package heej.net.domain.reservation.api;

import heej.net.domain.reservation.api.dto.*;
import heej.net.domain.reservation.usecase.ReservationUseCase;
import heej.net.security.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationEndpoint {

    private final ReservationUseCase reservationUseCase;

    /**
     * 예약 생성 (공휴일 체크 포함)
     * POST /api/reservations
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {

        Long memberId = SecurityUtil.getCurrentMemberId();
        log.info("Creating reservation for member: {}", memberId);

        ReservationResponse response = reservationUseCase.createReservation(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        List<ReservationResponse> reservations = reservationUseCase.getMyReservations(memberId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservation(
            @PathVariable Long reservationId) {

        Long memberId = SecurityUtil.getCurrentMemberId();
        ReservationResponse response = reservationUseCase.getReservation(memberId, reservationId);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/{reservationId}/cancel")
//    public ResponseEntity<Map<String, Object>> cancelReservation(
//            @PathVariable Long reservationId,
//            @RequestBody(required = false) Map<String, String> requestBody) {
//
//        Long memberId = SecurityUtil.getCurrentMemberId();
//        String reason = requestBody != null ? requestBody.get("reason") : null;
//
//        log.info("Cancelling reservation: reservationId={}, memberId={}", reservationId, memberId);
//        reservationUseCase.cancelReservation(memberId, reservationId, reason);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("message", "예약이 취소되었습니다.");
//
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/check-availability")
    public ResponseEntity<List<CheckAvailabilityResponse>> checkAvailability(
            @Valid @ModelAttribute CheckAvailabilityRequest request) {

        log.info("Checking availability: accommodationId={}, checkIn={}, checkOut={}, guests={}",
                request.getAccommodationId(), request.getCheckInDate(), request.getCheckOutDate(),
                request.getGuestCount());

        List<CheckAvailabilityResponse> availableRooms =
            reservationUseCase.checkAvailability(
                    request.getAccommodationId(),
                    request.getCheckInDate(),
                    request.getCheckOutDate(),
                    request.getGuestCount()
            );

        return ResponseEntity.ok(availableRooms);
    }

    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<ReservationCancelResponse> cancelReservationById(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationCancelRequest request
            ){
        Long memberId = SecurityUtil.getCurrentMemberId();

        reservationUseCase.cancelReservation(memberId, reservationId, request.getCancelReason());
        ReservationResponse reservationInfo = reservationUseCase.getReservation(memberId, reservationId);
        ReservationCancelResponse response =
                ReservationCancelResponse.builder()
                        .success(true)
                        .message("예약이 취소되었습니다.")
                        .reservationId(String.valueOf(reservationId))
                        .cancelReason(request.getCancelReason())
                        .roomName(reservationInfo.getRoomName())
                        .build();
        return ResponseEntity.ok(response);
    }
}

