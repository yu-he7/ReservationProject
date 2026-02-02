package heej.net.domain.holiday.scheduler;

import heej.net.domain.holiday.usecase.HolidayUseCase;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayScheduler {

    private final HolidayUseCase holidayUseCase;

    /**
     * 애플리케이션 시작 시 현재 연도와 다음 연도의 모든 공휴일을 동기화합니다
     */
    @PostConstruct
    public void initHolidays() {
        log.info("=== Initializing holidays on application startup ===");
        syncYearHolidays(LocalDate.now().getYear());
        syncYearHolidays(LocalDate.now().getYear() + 1);
        log.info("=== Holiday initialization completed ===");
    }

    /**
     * 매년 1월 1일 새벽 3시에 현재 연도와 다음 연도의 공휴일을 동기화합니다
     */
    @Scheduled(cron = "0 0 3 1 1 ?")
    public void syncAnnualHolidays() {
        log.info("=== Starting annual holiday sync ===");
        int currentYear = LocalDate.now().getYear();
        syncYearHolidays(currentYear);
        syncYearHolidays(currentYear + 1);
        log.info("=== Annual holiday sync completed ===");
    }

    /**
     * 매월 1일 새벽 4시에 현재 월과 다음 월의 공휴일을 재동기화합니다 (보완용)
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void syncMonthlyHolidays() {
        LocalDate now = LocalDate.now();
        LocalDate nextMonth = now.plusMonths(1);

        log.info("Starting monthly holiday resync for current and next month");

        try {
            int count1 = holidayUseCase.syncHolidays(now.getYear(), now.getMonthValue());
            int count2 = holidayUseCase.syncHolidays(nextMonth.getYear(), nextMonth.getMonthValue());
            log.info("Monthly holiday resync completed: current month count={}, next month count={}", count1, count2);
        } catch (Exception e) {
            log.error("Failed to resync holidays in monthly task", e);
        }
    }

    /**
     * 특정 연도의 모든 공휴일(1월~12월)을 동기화합니다
     */
    private void syncYearHolidays(int year) {
        log.info("Syncing all holidays for year={}", year);
        int totalCount = 0;

        for (int month = 1; month <= 12; month++) {
            try {
                int count = holidayUseCase.syncHolidays(year, month);
                totalCount += count;
                log.info("Synced holidays: year={}, month={}, count={}", year, month, count);

                // API 호출 제한을 고려하여 약간의 딜레이 추가함
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("Failed to sync holidays: year={}, month={}", year, month, e);
            }
        }

        log.info("Year {} holiday sync completed: total count={}", year, totalCount);
    }
}

