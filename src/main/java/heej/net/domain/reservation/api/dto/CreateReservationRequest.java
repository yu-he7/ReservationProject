package heej.net.domain.reservation.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    @NotNull(message = "객실 ID는 필수입니다.")
    private Long roomId;

    @NotNull(message = "체크인 날짜는 필수입니다.")
    @Future(message = "체크인 날짜는 미래 날짜여야 합니다.")
    @JsonFormat(pattern = "yyyy-M-d")
    private LocalDate checkInDate;

    @NotNull(message = "체크아웃 날짜는 필수입니다.")
    @Future(message = "체크아웃 날짜는 미래 날짜여야 합니다.")
    @JsonFormat(pattern = "yyyy-M-d")
    private LocalDate checkOutDate;

    @NotNull(message = "투숙 인원은 필수입니다.")
    @Min(value = 1, message = "투숙 인원은 최소 1명 이상이어야 합니다.")
    private Integer guestCount;

    @Size(max = 500, message = "특별 요청사항은 500자 이내로 입력해주세요.")
    private String specialRequests;
}

