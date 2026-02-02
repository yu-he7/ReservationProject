package heej.net.domain.accommodation.infra;

import heej.net.domain.accommodation.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationJpaRepository extends JpaRepository<Accommodation, Long> {
}

