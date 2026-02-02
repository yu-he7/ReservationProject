package heej.net.domain.accommodation.api.dto;

import heej.net.domain.accommodation.model.RoomStatus;
import heej.net.domain.accommodation.model.RoomType;
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
public class RoomDetailResponse {
    private Long id;
    private Long accommodationId;
    private String accommodationName;
    private String name;
    private RoomType type;
    private Integer capacity;
    private Integer maxCapacity;
    private BigDecimal pricePerNight;
    private String description;
    private Integer size;
    private String mainImage;
    private List<String> images;
    private RoomStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

