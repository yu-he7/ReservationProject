package heej.net.domain.accommodation.api.dto;

import heej.net.domain.accommodation.model.AccommodationStatus;
import heej.net.domain.accommodation.model.AccommodationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationDetailResponse {
    private Long id;
    private String name;
    private AccommodationType type;
    private String description;
    private String address;
    private String city;
    private String region;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String mainImage;
    private AccommodationStatus status;
    private Integer rating;
    private List<String> images;
    private List<RoomResponse> rooms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

