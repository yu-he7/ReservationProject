package heej.net.domain.reservation.infra;

import heej.net.domain.reservation.model.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationInfra {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByRoomId(Long roomId);

    List<Reservation> findOverlappingReservations(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    List<Reservation> findOverlappingReservationsWithLock(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    boolean hasOverlappingReservation(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    boolean hasOverlappingReservationWithLock(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);
}

