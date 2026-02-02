package heej.net.domain.accommodation.infra;

import heej.net.domain.accommodation.model.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoomInfraImpl implements RoomInfra {

    private final RoomJpaRepository repository;

    @Override
    public Optional<Room> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Room> findByIdWithLock(Long id) {
        return repository.findByIdWithLock(id);
    }

    @Override
    public List<Room> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<Room> findByAccommodationId(Long accommodationId, Pageable pageable) {
        return repository.findByAccommodationId(accommodationId, pageable);
    }

    @Override
    public List<Room> findAllByAccommodationId(Long accommodationId) {
        return repository.findAllByAccommodationId(accommodationId);
    }

    @Override
    public List<Room> findByMaxCapacityGreaterThanEqual(Integer guestCount) {
        return repository.findByMaxCapacityGreaterThanEqual(guestCount);
    }

    @Override
    public Room save(Room room) {
        return repository.save(room);
    }

    @Override
    public void delete(Room room) {
        repository.delete(room);
    }
}

