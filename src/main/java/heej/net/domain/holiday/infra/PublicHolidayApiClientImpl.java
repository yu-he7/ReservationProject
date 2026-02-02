package heej.net.domain.holiday.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import heej.net.domain.holiday.api.dto.PublicHolidayApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class PublicHolidayApiClientImpl implements PublicHolidayApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${public-holiday.api.url:http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo}")
    private String apiUrl;

    @Value("${public-holiday.api.service-key}")
    private String serviceKey;

    @Override
    public PublicHolidayApiResponse fetchHolidays(Integer year, Integer month) {
        try {
            // UriComponentsBuilder를 사용하여 URL 구성 (자동 인코딩)
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("ServiceKey", serviceKey)  // ServiceKey (대문자 S)
                    .queryParam("solYear", year)
                    .queryParam("solMonth", String.format("%02d", month))
                    .queryParam("numOfRows", 100)
                    .queryParam("_type", "json")
                    .build()  // false가 기본값이며 자동으로 인코딩됨
                    .toUriString();

            log.info("Fetching holidays from API: year={}, month={}", year, month);
            log.info("Request URL: {}", url.replaceAll("ServiceKey=[^&]+", "ServiceKey=***")); // ServiceKey는 로그에서 가림

            String response = restTemplate.getForObject(url, String.class);
            log.info("API Response received, length: {}", response != null ? response.length() : 0);
            log.debug("API Response: {}", response);

            return objectMapper.readValue(response, PublicHolidayApiResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch holidays from API: year={}, month={}", year, month, e);
            throw new RuntimeException("공휴일 정보를 가져오는데 실패했습니다.", e);
        }
    }
}

