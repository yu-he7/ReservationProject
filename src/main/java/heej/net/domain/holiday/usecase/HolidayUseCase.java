package heej.net.domain.holiday.usecase;

import heej.net.domain.holiday.api.dto.HolidayResponse;
import heej.net.domain.holiday.model.Holiday;

import java.time.LocalDate;
import java.util.List;

public interface HolidayUseCase {

    /**
     * 공휴일 API에서 데이터를 가져와 DB에 저장합니다.
     *
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 저장된 공휴일 개수
     */
    int syncHolidays(Integer year, Integer month);

    /**
     * 특정 날짜가 공휴일인지 확인합니다.
     *
     * @param date 확인할 날짜
     * @return 공휴일 여부
     */
    boolean isHoliday(LocalDate date);

    /**
     * 특정 날짜가 예약 가능한 날짜인지 확인합니다 (공휴일이 아닌 경우 true).
     *
     * @param date 확인할 날짜
     * @return 예약 가능 여부
     */
    boolean isBookableDate(LocalDate date);

    /**
     * 특정 연도/월의 공휴일 목록을 조회합니다.
     *
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 공휴일 목록
     */
    List<HolidayResponse> getHolidays(Integer year, Integer month);

    List<HolidayResponse> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate);

    HolidayResponse getHoliday(LocalDate date);

    HolidayResponse addHolidayManually(LocalDate date, String name, String description);
}

