package heej.net.domain.reservation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckAvailabilityResponse {

    private Long roomId;
    private String roomName;
    private String roomType;
    private Integer availableCount;  // 예약 가능한 객실 수
    private BigDecimal pricePerNight;
    private Integer maxOccupancy;
    private boolean available;  // 예약 가능 여부

    // 편의시설 정보
    private List<String> amenities;

    // 총 가격 (숙박일수 * 1박 가격)
    private BigDecimal totalPrice;
    private Integer nights;  // 숙박일수
}

