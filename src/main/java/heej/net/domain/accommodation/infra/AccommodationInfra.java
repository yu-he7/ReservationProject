package heej.net.domain.accommodation.infra;

import heej.net.domain.accommodation.model.Accommodation;
import heej.net.domain.accommodation.model.AccommodationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccommodationInfra {
    Optional<Accommodation> findById(Long id);

    Page<Accommodation> searchAccommodations(
            String keyword,
            String city,
            String region,
            AccommodationType type,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minRating,
            Pageable pageable
    );

    Accommodation save(Accommodation accommodation);

    void delete(Accommodation accommodation);
}

