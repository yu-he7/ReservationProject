package heej.net.domain.holiday.infra;

import heej.net.domain.holiday.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayJpaRepository extends JpaRepository<Holiday, Long> {

    Optional<Holiday> findByHolidayDate(LocalDate holidayDate);

    List<Holiday> findByYearAndMonth(Integer year, Integer month);

    List<Holiday> findByYear(Integer year);

    @Query("SELECT h FROM Holiday h WHERE h.holidayDate BETWEEN :startDate AND :endDate")
    List<Holiday> findByDateRange(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    boolean existsByHolidayDate(LocalDate holidayDate);
}

