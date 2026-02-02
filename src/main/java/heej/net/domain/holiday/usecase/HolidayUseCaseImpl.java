package heej.net.domain.holiday.usecase;

import heej.net.domain.holiday.api.dto.HolidayResponse;
import heej.net.domain.holiday.api.dto.PublicHolidayApiResponse;
import heej.net.domain.holiday.infra.HolidayInfra;
import heej.net.domain.holiday.infra.PublicHolidayApiClient;
import heej.net.domain.holiday.model.Holiday;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HolidayUseCaseImpl implements HolidayUseCase {

    private final HolidayInfra holidayInfra;
    private final PublicHolidayApiClient publicHolidayApiClient;

    @Override
    @Transactional
    public int syncHolidays(Integer year, Integer month) {
        log.info("Syncing holidays for year={}, month={}", year, month);

        try {
            PublicHolidayApiResponse response = publicHolidayApiClient.fetchHolidays(year, month);

            if (response == null || response.getResponse() == null
                    || response.getResponse().getBody() == null
                    || response.getResponse().getBody().getItems() == null) {
                log.warn("No holiday data received from API");
                return 0;
            }

            List<PublicHolidayApiResponse.Item> items = response.getResponse().getBody().getItems();
            if (items.isEmpty()) {
                log.info("No holidays found for year={}, month={}", year, month);
                return 0;
            }

            int savedCount = 0;
            for (PublicHolidayApiResponse.Item item : items) {
                if ("Y".equals(item.getIsHoliday())) {
                    LocalDate holidayDate = parseDate(item.getLocdate());

                    // 이미 존재하는 공휴일인지 확인
                    if (!holidayInfra.isHoliday(holidayDate)) {
                        Holiday holiday = Holiday.builder()
                                .holidayDate(holidayDate)
                                .holidayName(item.getDateName())
                                .description(item.getDateKind())
                                .year(year)
                                .month(month)
                                .build();

                        holidayInfra.save(holiday);
                        savedCount++;
                        log.debug("Saved holiday: date={}, name={}", holidayDate, item.getDateName());
                    }
                }
            }

            log.info("Successfully synced {} holidays for year={}, month={}", savedCount, year, month);
            return savedCount;

        } catch (Exception e) {
            log.error("Failed to sync holidays: year={}, month={}", year, month, e);
            throw new RuntimeException("공휴일 동기화에 실패했습니다.", e);
        }
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidayInfra.isHoliday(date);
    }

    @Override
    public boolean isBookableDate(LocalDate date) {
        // 공휴일이 아니면 예약 가능
        return !isHoliday(date);
    }

    @Override
    public List<HolidayResponse> getHolidays(Integer year, Integer month) {
        List<Holiday> holidays = holidayInfra.findByYearAndMonth(year, month);
        return holidays.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<HolidayResponse> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Holiday> holidays = holidayInfra.findByDateRange(startDate, endDate);
        return holidays.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HolidayResponse getHoliday(LocalDate date) {
        return holidayInfra.findByDate(date)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public HolidayResponse addHolidayManually(LocalDate date, String name, String description) {
        log.info("Manually adding holiday: date={}, name={}", date, name);

        // 이미 존재하는지 확인
        if (holidayInfra.isHoliday(date)) {
            log.warn("Holiday already exists for date: {}", date);
            return getHoliday(date);
        }

        Holiday holiday = Holiday.builder()
                .holidayDate(date)
                .holidayName(name)
                .description(description)
                .year(date.getYear())
                .month(date.getMonthValue())
                .build();

        Holiday saved = holidayInfra.save(holiday);
        log.info("Holiday added manually: id={}, date={}, name={}", saved.getId(), date, name);

        return toResponse(saved);
    }

    private LocalDate parseDate(Integer locdate) {
        String dateStr = String.valueOf(locdate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(dateStr, formatter);
    }

    private HolidayResponse toResponse(Holiday holiday) {
        return HolidayResponse.builder()
                .id(holiday.getId())
                .holidayDate(holiday.getHolidayDate())
                .holidayName(holiday.getHolidayName())
                .description(holiday.getDescription())
                .year(holiday.getYear())
                .month(holiday.getMonth())
                .build();
    }
}

