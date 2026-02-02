package heej.net.domain.reservation.infra;

import heej.net.domain.reservation.model.Reservation;
import heej.net.domain.reservation.model.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByRoomId(Long roomId);

    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND ((r.checkInDate <= :checkOutDate AND r.checkOutDate >= :checkInDate))")
    List<Reservation> findOverlappingReservations(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND ((r.checkInDate <= :checkOutDate AND r.checkOutDate >= :checkInDate))")
    List<Reservation> findOverlappingReservationsWithLock(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId AND r.status = :status")
    List<Reservation> findByMemberIdAndStatus(
            @Param("memberId") Long memberId,
            @Param("status") ReservationStatus status);
}

