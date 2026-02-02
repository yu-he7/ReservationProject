package heej.net.domain.accommodation.infra;

import heej.net.domain.accommodation.model.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomJpaRepository extends JpaRepository<Room, Long> {
    Page<Room> findByAccommodationId(Long accommodationId, Pageable pageable);
    List<Room> findAllByAccommodationId(Long accommodationId);

    List<Room> findByMaxCapacityGreaterThanEqual(Integer guestCount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :roomId")
    Optional<Room> findByIdWithLock(@Param("roomId") Long roomId);
}

