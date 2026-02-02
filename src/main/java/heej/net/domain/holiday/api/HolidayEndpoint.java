package heej.net.domain.holiday.api;

import heej.net.domain.holiday.api.dto.HolidayResponse;
import heej.net.domain.holiday.usecase.HolidayUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@Slf4j
public class HolidayEndpoint {

    private final HolidayUseCase holidayUseCase;

    /**
     * 공휴일 API에서 데이터를 가져와 동기화합니다.
     * POST /api/holidays/sync?year=2026&month=1
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncHolidays(
            @RequestParam Integer year,
            @RequestParam Integer month) {

        log.info("Syncing holidays: year={}, month={}", year, month);
        int count = holidayUseCase.syncHolidays(year, month);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "공휴일 동기화가 완료되었습니다.");
        response.put("year", year);
        response.put("month", month);
        response.put("syncedCount", count);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 연도의 모든 공휴일(1~12월)을 동기화합니다.
     * POST /api/holidays/sync-year?year=2026
     */
    @PostMapping("/sync-year")
    public ResponseEntity<Map<String, Object>> syncYearHolidays(
            @RequestParam Integer year) {

        log.info("Syncing all holidays for year={}", year);

        int totalCount = 0;
        for (int month = 1; month <= 12; month++) {
            try {
                int count = holidayUseCase.syncHolidays(year, month);
                totalCount += count;
                log.info("Synced holidays: year={}, month={}, count={}", year, month, count);
                Thread.sleep(100); // API 호출 제한 고려
            } catch (Exception e) {
                log.error("Failed to sync holidays: year={}, month={}", year, month, e);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", year + "년 전체 공휴일 동기화가 완료되었습니다.");
        response.put("year", year);
        response.put("totalSyncedCount", totalCount);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 날짜가 공휴일인지 확인합니다.
     * GET /api/holidays/check?date=2026-01-01
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkHoliday(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        boolean isHoliday = holidayUseCase.isHoliday(date);
        HolidayResponse holiday = holidayUseCase.getHoliday(date);

        Map<String, Object> response = new HashMap<>();
        response.put("date", date);
        response.put("isHoliday", isHoliday);
        response.put("isBookable", !isHoliday);

        if (holiday != null) {
            response.put("holidayName", holiday.getHolidayName());
            response.put("description", holiday.getDescription());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 연도/월의 공휴일 목록을 조회합니다.
     * GET /api/holidays?year=2026&month=1
     */
    @GetMapping
    public ResponseEntity<List<HolidayResponse>> getHolidays(
            @RequestParam Integer year,
            @RequestParam Integer month) {

        List<HolidayResponse> holidays = holidayUseCase.getHolidays(year, month);
        return ResponseEntity.ok(holidays);
    }

    /**
     * 특정 날짜 범위의 공휴일 목록을 조회합니다.
     * GET /api/holidays/range?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/range")
    public ResponseEntity<List<HolidayResponse>> getHolidaysByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<HolidayResponse> holidays = holidayUseCase.getHolidaysByDateRange(startDate, endDate);
        return ResponseEntity.ok(holidays);
    }

    /**
     * 수동으로 공휴일을 추가합니다. (테스트용)
     * POST /api/holidays/manual?date=2026-03-01&name=삼일절
     */
    @PostMapping("/manual")
    public ResponseEntity<HolidayResponse> addHolidayManually(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "국경일") String description) {

        log.info("Manually adding holiday: date={}, name={}", date, name);
        HolidayResponse holiday = holidayUseCase.addHolidayManually(date, name, description);
        return ResponseEntity.ok(holiday);
    }
}

