package heej.net.domain.accommodation.infra;

import heej.net.domain.accommodation.model.Accommodation;
import heej.net.domain.accommodation.model.AccommodationType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccommodationInfraImpl implements AccommodationInfra {

    private final AccommodationJpaRepository repository;
    private final EntityManager entityManager;

    @Override
    public Optional<Accommodation> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<Accommodation> searchAccommodations(
            String keyword,
            String city,
            String region,
            AccommodationType type,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minRating,
            Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Accommodation> query = cb.createQuery(Accommodation.class);
        Root<Accommodation> accommodation = query.from(Accommodation.class);

        // 검색 조건 생성
        Predicate wherePredicate = buildSearchPredicates(cb, accommodation, keyword, city, region, type, minRating);

        query.where(wherePredicate);
        query.orderBy(cb.desc(accommodation.get("createdAt")));

        // 페이징 처리
        List<Accommodation> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // 전체 개수 조회 (새로운 쿼리와 Root 생성)
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Accommodation> countRoot = countQuery.from(Accommodation.class);
        Predicate countWherePredicate = buildSearchPredicates(cb, countRoot, keyword, city, region, type, minRating);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countWherePredicate);
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    private Predicate buildSearchPredicates(
            CriteriaBuilder cb,
            Root<Accommodation> accommodation,
            String keyword,
            String city,
            String region,
            AccommodationType type,
            Integer minRating
    ) {
        List<Predicate> predicates = new ArrayList<>();

        // 키워드 검색 (이름 또는 주소)
        if (keyword != null && !keyword.isBlank()) {
            Predicate namePredicate = cb.like(accommodation.get("name"), "%" + keyword + "%");
            Predicate addressPredicate = cb.like(accommodation.get("address"), "%" + keyword + "%");
            predicates.add(cb.or(namePredicate, addressPredicate));
        }

        // 도시 필터
        if (city != null && !city.isBlank()) {
            predicates.add(cb.equal(accommodation.get("city"), city));
        }

        // 지역 필터
        if (region != null && !region.isBlank()) {
            predicates.add(cb.equal(accommodation.get("region"), region));
        }

        // 숙소 타입 필터
        if (type != null) {
            predicates.add(cb.equal(accommodation.get("type"), type));
        }

        // 평점 필터
        if (minRating != null) {
            predicates.add(cb.greaterThanOrEqualTo(accommodation.get("rating"), minRating));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    @Override
    public Accommodation save(Accommodation accommodation) {
        return repository.save(accommodation);
    }

    @Override
    public void delete(Accommodation accommodation) {
        repository.delete(accommodation);
    }
}

