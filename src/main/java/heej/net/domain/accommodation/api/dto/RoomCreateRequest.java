package heej.net.domain.accommodation.api.dto;

import heej.net.domain.accommodation.model.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCreateRequest {
    @NotNull(message = "숙소 ID는 필수입니다")
    private Long accommodationId;

    @NotBlank(message = "객실 이름은 필수입니다")
    private String name;

    @NotNull(message = "객실 타입은 필수입니다")
    private RoomType type;

    @NotNull(message = "기본 수용 인원은 필수입니다")
    @Positive(message = "기본 수용 인원은 양수여야 합니다")
    private Integer capacity;

    @NotNull(message = "최대 수용 인원은 필수입니다")
    @Positive(message = "최대 수용 인원은 양수여야 합니다")
    private Integer maxCapacity;

    @NotNull(message = "1박 가격은 필수입니다")
    @Positive(message = "1박 가격은 양수여야 합니다")
    private BigDecimal pricePerNight;

    private String description;

    @NotNull(message = "객실 크기는 필수입니다")
    @Positive(message = "객실 크기는 양수여야 합니다")
    private Integer size;

    private String mainImage;
}

