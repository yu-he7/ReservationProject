package heej.net.domain.accommodation.api.dto;

import heej.net.domain.accommodation.model.AccommodationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationCreateRequest {
    @NotBlank(message = "숙소 이름은 필수입니다")
    private String name;

    @NotNull(message = "숙소 타입은 필수입니다")
    private AccommodationType type;

    @NotBlank(message = "숙소 설명은 필수입니다")
    private String description;

    @NotBlank(message = "주소는 필수입니다")
    private String address;

    @NotBlank(message = "도시는 필수입니다")
    private String city;

    @NotBlank(message = "지역은 필수입니다")
    private String region;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String mainImage;
}
