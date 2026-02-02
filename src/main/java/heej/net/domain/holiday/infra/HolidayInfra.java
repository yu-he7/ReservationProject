package heej.net.domain.holiday.infra;

import heej.net.domain.holiday.model.Holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayInfra {

    Holiday save(Holiday holiday);

    Optional<Holiday> findById(Long id);

    Optional<Holiday> findByDate(LocalDate date);

    List<Holiday> findByYearAndMonth(Integer year, Integer month);

    List<Holiday> findByYear(Integer year);

    List<Holiday> findByDateRange(LocalDate startDate, LocalDate endDate);

    boolean isHoliday(LocalDate date);

    void deleteAll();
}

