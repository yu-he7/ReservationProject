package heej.net.domain.holiday.infra;

import heej.net.domain.holiday.api.dto.PublicHolidayApiResponse;

public interface PublicHolidayApiClient {

    PublicHolidayApiResponse fetchHolidays(Integer year, Integer month);
}

