package heej.net.domain.reservation.infra;

import heej.net.domain.reservation.model.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationInfraImpl implements ReservationInfra {

    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public List<Reservation> findByMemberId(Long memberId) {
        return reservationJpaRepository.findByMemberId(memberId);
    }

    @Override
    public List<Reservation> findByRoomId(Long roomId) {
        return reservationJpaRepository.findByRoomId(roomId);
    }

    @Override
    public List<Reservation> findOverlappingReservations(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return reservationJpaRepository.findOverlappingReservations(roomId, checkInDate, checkOutDate);
    }

    @Override
    public List<Reservation> findOverlappingReservationsWithLock(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return reservationJpaRepository.findOverlappingReservationsWithLock(roomId, checkInDate, checkOutDate);
    }

    @Override
    public boolean hasOverlappingReservation(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Reservation> overlapping = findOverlappingReservations(roomId, checkInDate, checkOutDate);
        return !overlapping.isEmpty();
    }

    @Override
    public boolean hasOverlappingReservationWithLock(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Reservation> overlapping = findOverlappingReservationsWithLock(roomId, checkInDate, checkOutDate);
        return !overlapping.isEmpty();
    }
}

