package heej.net.domain.holiday.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayResponse {
    private Long id;
    private LocalDate holidayDate;
    private String holidayName;
    private String description;
    private Integer year;
    private Integer month;
}

