package heej.net.domain.accommodation.api;

import heej.net.domain.accommodation.api.dto.*;
import heej.net.domain.accommodation.usecase.AccommodationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationEndpoint {

    private final AccommodationUseCase accommodationUseCase;

    /**
     * 숙소 검색 및 목록 조회
     * GET /api/accommodations?keyword=제주&city=제주&type=HOTEL&minRating=4&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<AccommodationResponse>> searchAccommodations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("숙소 검색 API 호출: keyword={}, city={}, region={}, type={}, minRating={}",
                keyword, city, region, type, minRating);

        AccommodationSearchRequest request = AccommodationSearchRequest.builder()
                .keyword(keyword)
                .city(city)
                .region(region)
                .type(type != null ? heej.net.domain.accommodation.model.AccommodationType.valueOf(type) : null)
                .minRating(minRating)
                .page(page)
                .size(size)
                .build();

        Page<AccommodationResponse> result = accommodationUseCase.searchAccommodations(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 숙소 상세 조회 (이미지 및 객실 목록 포함)
     * GET /api/accommodations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationDetailResponse> getAccommodationDetail(@PathVariable Long id) {
        log.info("숙소 상세 조회 API 호출: accommodationId={}", id);

        AccommodationDetailResponse response = accommodationUseCase.getAccommodationDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 숙소의 객실 목록 조회
     * GET /api/accommodations/{id}/rooms?page=0&size=10
     */
    @GetMapping("/{id}/rooms")
    public ResponseEntity<Page<RoomResponse>> getRoomsByAccommodation(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("숙소별 객실 목록 조회 API 호출: accommodationId={}", id);

        Page<RoomResponse> result = accommodationUseCase.getRoomsByAccommodation(id, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 숙소 등록
     * POST /api/accommodations
     */
    @PostMapping
    public ResponseEntity<AccommodationResponse> createAccommodation(
            @jakarta.validation.Valid @RequestBody AccommodationCreateRequest request
    ) {
        log.info("숙소 등록 API 호출: name={}", request.getName());

        AccommodationResponse response = accommodationUseCase.createAccommodation(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }
}

