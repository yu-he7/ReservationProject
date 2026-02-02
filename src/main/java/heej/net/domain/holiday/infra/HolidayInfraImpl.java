package heej.net.domain.holiday.infra;

import heej.net.domain.holiday.model.Holiday;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HolidayInfraImpl implements HolidayInfra {

    private final HolidayJpaRepository holidayJpaRepository;

    @Override
    public Holiday save(Holiday holiday) {
        return holidayJpaRepository.save(holiday);
    }

    @Override
    public Optional<Holiday> findById(Long id) {
        return holidayJpaRepository.findById(id);
    }

    @Override
    public Optional<Holiday> findByDate(LocalDate date) {
        return holidayJpaRepository.findByHolidayDate(date);
    }

    @Override
    public List<Holiday> findByYearAndMonth(Integer year, Integer month) {
        return holidayJpaRepository.findByYearAndMonth(year, month);
    }

    @Override
    public List<Holiday> findByYear(Integer year) {
        return holidayJpaRepository.findByYear(year);
    }

    @Override
    public List<Holiday> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return holidayJpaRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidayJpaRepository.existsByHolidayDate(date);
    }

    @Override
    public void deleteAll() {
        holidayJpaRepository.deleteAll();
    }
}

