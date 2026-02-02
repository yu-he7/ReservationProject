package heej.net.domain.accommodation.api.dto;

import heej.net.domain.accommodation.model.AccommodationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationSearchRequest {
    private String keyword;          // 검색 키워드 (이름, 주소)
    private String city;             // 도시
    private String region;           // 지역
    private AccommodationType type;  // 숙소 타입
    private BigDecimal minPrice;     // 최소 가격
    private BigDecimal maxPrice;     // 최대 가격
    private Integer minRating;       // 최소 평점
    private Integer page = 0;        // 페이지 번호
    private Integer size = 10;       // 페이지 크기
}

