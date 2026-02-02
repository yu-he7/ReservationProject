package heej.net.domain.reservation.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckAvailabilityRequest {

    // 숙소 ID (선택사항 - null이면 모든 숙소 조회)
    private Long accommodationId;

    @NotNull(message = "체크인 날짜는 필수입니다")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @NotNull(message = "체크아웃 날짜는 필수입니다")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    @NotNull(message = "투숙 인원은 필수입니다")
    @Min(value = 1, message = "투숙 인원은 최소 1명 이상이어야 합니다")
    private Integer guestCount;

    // 특정 객실 타입 조회시 사용 (선택사항)
    private Long roomId;
}
