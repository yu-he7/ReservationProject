package heej.net.domain.accommodation.infra;

import heej.net.domain.accommodation.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RoomInfra {
    Optional<Room> findById(Long id);

    Optional<Room> findByIdWithLock(Long id);

    List<Room> findAll();

    Page<Room> findByAccommodationId(Long accommodationId, Pageable pageable);

    List<Room> findAllByAccommodationId(Long accommodationId);

    List<Room> findByMaxCapacityGreaterThanEqual(Integer guestCount);

    Room save(Room room);

    void delete(Room room);
}

