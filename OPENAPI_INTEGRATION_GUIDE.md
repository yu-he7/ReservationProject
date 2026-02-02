# 공공데이터 Open API 연동 가이드

## 목차
1. [Open API란?](#open-api란)
2. [공공데이터포털 특일정보 API](#공공데이터포털-특일정보-api)
3. [API 키 발급 방법](#api-키-발급-방법)
4. [시스템 아키텍처](#시스템-아키텍처)
5. [구현 구조](#구현-구조)
6. [API 연동 과정](#api-연동-과정)
7. [코드 상세 설명](#코드-상세-설명)
8. [스케줄러를 통한 자동 동기화](#스케줄러를-통한-자동-동기화)
9. [에러 처리](#에러-처리)
10. [테스트 방법](#테스트-방법)

---

## Open API란?

### Open API (Application Programming Interface)
- 외부에서 사용할 수 있도록 공개된 API
- HTTP 프로토콜을 통해 데이터를 주고받음
- 주로 REST API 형태로 제공됨

### 공공데이터포털
- **URL**: https://www.data.go.kr
- 정부에서 제공하는 다양한 공공데이터 API
- 무료로 사용 가능 (일부 제한 있음)
- 인증키(Service Key) 필요

---

## 공공데이터포털 특일정보 API

### API 정보
- **API명**: 특일정보 조회 서비스 (공휴일 정보)
- **제공기관**: 행정안전부
- **API URL**: `http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo`
- **데이터 형식**: XML, JSON

### 제공 정보
- 공휴일 날짜
- 공휴일 이름
- 음력/양력 구분
- 관공서 휴일 여부

### 호출 제한
- 일일 트래픽: 1,000건 (기본 계정)
- 초당 요청: 10건
- 제한 초과 시 서비스 일시 중단

---

## API 키 발급 방법

### 1. 공공데이터포털 회원가입
```
1. https://www.data.go.kr 접속
2. 회원가입 (일반회원 또는 개발자회원)
3. 이메일 인증 완료
```

### 2. API 신청
```
1. 로그인 후 검색창에 "특일정보" 검색
2. "특일정보 조회 서비스" 클릭
3. "활용신청" 버튼 클릭
4. 활용목적 작성 (예: 숙소 예약 시스템 개발)
5. 신청 완료
```

### 3. 인증키 확인
```
1. 마이페이지 → 오픈API → 개발계정
2. 승인 완료 후 "일반 인증키(Encoding)" 복사
3. application.yml에 설정
```

### 실제 인증키 예시
```
puufIdyervphKPB1dDcSp2nzeFh0CNq9RPu4pgKBVkVOjgZ7A6hbgajsn779EYXCyvckPPZolarl6VwBCmXCMQ==
```

---

## 시스템 아키텍처

### 전체 플로우
```
┌────────────────┐
│ 공공데이터포털  │
│ Open API       │
└───────┬────────┘
        │ HTTP Request (REST)
        │ GET /getRestDeInfo?ServiceKey=...&solYear=2026&solMonth=01
        ↓
┌───────────────────────────────┐
│ Spring Boot Application       │
│                               │
│ ┌─────────────────────────┐   │
│ │ HolidayScheduler        │   │
│ │ (자동 동기화)            │   │
│ └────────┬────────────────┘   │
│          ↓                    │
│ ┌─────────────────────────┐   │
│ │ HolidayUseCase          │   │
│ │ (비즈니스 로직)          │   │
│ └────────┬────────────────┘   │
│          ↓                    │
│ ┌─────────────────────────┐   │
│ │ PublicHolidayApiClient  │   │
│ │ (RestTemplate)          │   │
│ └────────┬────────────────┘   │
│          ↓                    │
│ ┌─────────────────────────┐   │
│ │ Holiday Entity          │   │
│ │ (Database)              │   │
│ └─────────────────────────┘   │
└───────────────────────────────┘
```

### 데이터 흐름
1. **스케줄러**: 정해진 시간에 자동 실행
2. **UseCase**: 연도/월 기준으로 API 호출
3. **ApiClient**: RestTemplate로 HTTP 요청
4. **응답 파싱**: JSON → Java 객체
5. **데이터 저장**: 중복 체크 후 DB 저장
6. **예약 체크**: 예약 시 공휴일 여부 확인

---

## 구현 구조

### 프로젝트 구조
```
src/main/java/heej/net/domain/holiday/
├── api/
│   ├── HolidayEndpoint.java                # REST API Controller
│   └── dto/
│       ├── PublicHolidayApiResponse.java   # Open API 응답 DTO
│       ├── HolidayResponse.java            # 클라이언트 응답 DTO
│       └── HolidayItem.java                # 공휴일 항목 DTO
├── usecase/
│   ├── HolidayUseCase.java                 # 비즈니스 로직 인터페이스
│   └── HolidayUseCaseImpl.java             # 비즈니스 로직 구현
├── model/
│   └── Holiday.java                        # 공휴일 엔티티
├── infra/
│   ├── PublicHolidayApiClient.java         # API 클라이언트 인터페이스
│   ├── PublicHolidayApiClientImpl.java     # RestTemplate 구현 (핵심)
│   ├── HolidayInfra.java                   # Repository 인터페이스
│   └── HolidayInfraImpl.java               # Repository 구현
└── scheduler/
    └── HolidayScheduler.java               # 스케줄러 (자동 동기화)
```

---

## API 연동 과정

### 1. 의존성 설정 (build.gradle)
```gradle
dependencies {
    // Spring Web (RestTemplate 포함)
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // Jackson (JSON 파싱)
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

### 2. application.yml 설정
```yaml
public-holiday:
  api:
    url: http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo
    service-key: puufIdyervphKPB1dDcSp2nzeFh0CNq9RPu4pgKBVkVOjgZ7A6hbgajsn779EYXCyvckPPZolarl6VwBCmXCMQ==
```

### 3. RestTemplate Bean 등록
```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### 4. API 응답 DTO 정의
```java
// PublicHolidayApiResponse.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicHolidayApiResponse {
    private Response response;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Header header;
        private Body body;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        private String resultCode;  // "00" = 정상
        private String resultMsg;   // "NORMAL SERVICE"
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        private Items items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Items {
        @JsonProperty("item")
        private List<HolidayItem> item;
    }
}

// HolidayItem.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayItem {
    private String dateKind;    // 날짜 구분 (양력/음력)
    private String dateName;    // 공휴일 이름
    private String isHoliday;   // 공휴일 여부 (Y/N)
    private String locdate;     // 날짜 (YYYYMMDD)
    private int seq;            // 순번
}
```

### 5. API Client 구현
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class PublicHolidayApiClientImpl implements PublicHolidayApiClient {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${public-holiday.api.url}")
    private String apiUrl;
    
    @Value("${public-holiday.api.service-key}")
    private String serviceKey;
    
    @Override
    public PublicHolidayApiResponse fetchHolidays(Integer year, Integer month) {
        try {
            // 1. URL 구성 (자동 인코딩)
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("ServiceKey", serviceKey)  // 인증키
                    .queryParam("solYear", year)           // 연도
                    .queryParam("solMonth", String.format("%02d", month))  // 월 (01~12)
                    .queryParam("numOfRows", 100)          // 최대 조회 건수
                    .queryParam("_type", "json")           // 응답 형식 (json)
                    .build()
                    .toUriString();
            
            log.info("Fetching holidays from API: year={}, month={}", year, month);
            log.info("Request URL: {}", url.replaceAll("ServiceKey=[^&]+", "ServiceKey=***"));
            
            // 2. HTTP GET 요청
            String response = restTemplate.getForObject(url, String.class);
            log.info("API Response received, length: {}", response != null ? response.length() : 0);
            log.debug("API Response: {}", response);
            
            // 3. JSON → Java 객체 변환
            return objectMapper.readValue(response, PublicHolidayApiResponse.class);
            
        } catch (Exception e) {
            log.error("Failed to fetch holidays from API: year={}, month={}", year, month, e);
            throw new RuntimeException("공휴일 정보를 가져오는데 실패했습니다.", e);
        }
    }
}
```

---

## 코드 상세 설명

### 1. API 요청 URL 구성
```java
// UriComponentsBuilder 사용 (자동 인코딩)
String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
    .queryParam("ServiceKey", serviceKey)  // 인증키
    .queryParam("solYear", year)           // 연도 (예: 2026)
    .queryParam("solMonth", String.format("%02d", month))  // 월 (예: 01)
    .queryParam("numOfRows", 100)          // 최대 조회 건수
    .queryParam("_type", "json")           // 응답 형식 (json 또는 xml)
    .build()
    .toUriString();

// 결과 URL 예시:
// http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo
//   ?ServiceKey=puufIdye...
//   &solYear=2026
//   &solMonth=01
//   &numOfRows=100
//   &_type=json
```

### 2. RestTemplate HTTP 요청
```java
// GET 요청 (응답을 String으로 받음)
String response = restTemplate.getForObject(url, String.class);

// RestTemplate 주요 메서드:
// - getForObject(): GET 요청 + 응답을 객체로 변환
// - getForEntity(): GET 요청 + ResponseEntity 반환 (상태 코드 포함)
// - postForObject(): POST 요청 + 응답을 객체로 변환
// - exchange(): 모든 HTTP 메서드 지원 (가장 범용적)
```

### 3. JSON 응답 파싱
```java
// ObjectMapper를 사용한 JSON → Java 객체 변환
PublicHolidayApiResponse apiResponse = 
    objectMapper.readValue(response, PublicHolidayApiResponse.class);

// Jackson 어노테이션 활용:
// - @JsonProperty("item"): JSON의 "item" 필드를 Java의 item 필드에 매핑
// - @Data: Lombok으로 getter/setter 자동 생성
```

### 4. API 응답 예시
```json
{
  "response": {
    "header": {
      "resultCode": "00",
      "resultMsg": "NORMAL SERVICE"
    },
    "body": {
      "items": {
        "item": [
          {
            "dateKind": "01",
            "dateName": "신정",
            "isHoliday": "Y",
            "locdate": "20260101",
            "seq": 1
          },
          {
            "dateKind": "01",
            "dateName": "설날",
            "isHoliday": "Y",
            "locdate": "20260129",
            "seq": 1
          }
        ]
      },
      "numOfRows": 100,
      "pageNo": 1,
      "totalCount": 2
    }
  }
}
```

### 5. UseCase에서 데이터 처리
```java
@Override
@Transactional
public int syncHolidays(Integer year, Integer month) {
    log.info("Syncing holidays: year={}, month={}", year, month);
    
    // 1. Open API 호출
    PublicHolidayApiResponse apiResponse = holidayApiClient.fetchHolidays(year, month);
    
    // 2. 응답 검증
    if (apiResponse == null || apiResponse.getResponse() == null) {
        log.warn("Empty response from holiday API");
        return 0;
    }
    
    PublicHolidayApiResponse.Response response = apiResponse.getResponse();
    PublicHolidayApiResponse.Header header = response.getHeader();
    
    // 3. 결과 코드 확인
    if (!"00".equals(header.getResultCode())) {
        log.error("API returned error: code={}, msg={}", 
                 header.getResultCode(), header.getResultMsg());
        throw new RuntimeException("공휴일 API 호출 실패: " + header.getResultMsg());
    }
    
    // 4. 데이터 추출
    PublicHolidayApiResponse.Body body = response.getBody();
    if (body == null || body.getItems() == null || body.getItems().getItem() == null) {
        log.info("No holidays found for year={}, month={}", year, month);
        return 0;
    }
    
    List<HolidayItem> items = body.getItems().getItem();
    int count = 0;
    
    // 5. 각 공휴일 저장
    for (HolidayItem item : items) {
        try {
            // 날짜 파싱 (YYYYMMDD → LocalDate)
            LocalDate date = LocalDate.parse(item.getLocdate(), 
                                            DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 중복 체크
            if (holidayInfra.existsByHolidayDate(date)) {
                log.debug("Holiday already exists: {}", date);
                continue;
            }
            
            // Holiday 엔티티 생성 및 저장
            Holiday holiday = Holiday.builder()
                    .holidayDate(date)
                    .holidayName(item.getDateName())
                    .description(item.getDateName())
                    .isHoliday("Y".equals(item.getIsHoliday()))
                    .build();
            
            holidayInfra.save(holiday);
            count++;
            
        } catch (Exception e) {
            log.error("Failed to save holiday: {}", item, e);
        }
    }
    
    log.info("Synced {} holidays for year={}, month={}", count, year, month);
    return count;
}
```

---

## 스케줄러를 통한 자동 동기화

### HolidayScheduler 구현
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayScheduler {
    
    private final HolidayUseCase holidayUseCase;
    
    /**
     * 애플리케이션 시작 시 자동 실행
     * 현재 연도(2026년) + 다음 연도(2027년) 동기화
     */
    @PostConstruct
    public void initHolidays() {
        log.info("=== Initializing holidays on application startup ===");
        syncYearHolidays(LocalDate.now().getYear());
        syncYearHolidays(LocalDate.now().getYear() + 1);
        log.info("=== Holiday initialization completed ===");
    }
    
    /**
     * 매년 1월 1일 새벽 3시 자동 실행
     * Cron 표현식: 초 분 시 일 월 요일
     * 0 0 3 1 1 ? = 1월 1일 3시 0분 0초
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
     * 매월 1일 새벽 4시 자동 실행
     * Cron 표현식: 0 0 4 1 * ? = 매월 1일 4시 0분 0초
     * 보완용: API 변경사항이나 누락된 데이터 재동기화
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void syncMonthlyHolidays() {
        LocalDate now = LocalDate.now();
        LocalDate nextMonth = now.plusMonths(1);
        
        log.info("Starting monthly holiday resync for current and next month");
        
        try {
            int count1 = holidayUseCase.syncHolidays(now.getYear(), now.getMonthValue());
            int count2 = holidayUseCase.syncHolidays(nextMonth.getYear(), nextMonth.getMonthValue());
            log.info("Monthly holiday resync completed: current month count={}, next month count={}", 
                    count1, count2);
        } catch (Exception e) {
            log.error("Failed to resync holidays in monthly task", e);
        }
    }
    
    /**
     * 특정 연도의 1월~12월 모두 동기화
     */
    private void syncYearHolidays(int year) {
        log.info("Syncing all holidays for year={}", year);
        int totalCount = 0;
        
        for (int month = 1; month <= 12; month++) {
            try {
                int count = holidayUseCase.syncHolidays(year, month);
                totalCount += count;
                log.info("Synced holidays: year={}, month={}, count={}", year, month, count);
                
                // API 호출 제한 고려 (100ms 딜레이)
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("Failed to sync holidays: year={}, month={}", year, month, e);
            }
        }
        
        log.info("Year {} holiday sync completed: total count={}", year, totalCount);
    }
}
```

### Cron 표현식 설명
```
  ┌───────────── 초 (0-59)
  │ ┌─────────── 분 (0-59)
  │ │ ┌───────── 시 (0-23)
  │ │ │ ┌─────── 일 (1-31)
  │ │ │ │ ┌───── 월 (1-12)
  │ │ │ │ │ ┌─── 요일 (0-7, 0=일요일, 7=일요일)
  │ │ │ │ │ │
  * * * * * ?

예시:
0 0 3 1 1 ?  = 1월 1일 새벽 3시
0 0 4 1 * ?  = 매월 1일 새벽 4시
0 0 * * * ?  = 매일 매시간 정각
0 */30 * * * ? = 30분마다
```

### 스케줄러 활성화 (SchedulerConfig)
```java
@Configuration
@EnableScheduling  // 스케줄러 활성화
public class SchedulerConfig {
    // 설정 없음 (Spring이 자동으로 처리)
}
```

---

## 에러 처리

### 1. API 호출 실패
```java
try {
    String response = restTemplate.getForObject(url, String.class);
} catch (HttpClientErrorException e) {
    // 4xx 에러 (잘못된 요청)
    log.error("HTTP 4xx Error: status={}, body={}", 
             e.getStatusCode(), e.getResponseBodyAsString());
    throw new RuntimeException("API 호출 실패: 잘못된 요청");
} catch (HttpServerErrorException e) {
    // 5xx 에러 (서버 오류)
    log.error("HTTP 5xx Error: status={}, body={}", 
             e.getStatusCode(), e.getResponseBodyAsString());
    throw new RuntimeException("API 호출 실패: 서버 오류");
} catch (ResourceAccessException e) {
    // 네트워크 오류 (타임아웃, 연결 실패)
    log.error("Network Error: {}", e.getMessage());
    throw new RuntimeException("API 호출 실패: 네트워크 오류");
}
```

### 2. JSON 파싱 실패
```java
try {
    return objectMapper.readValue(response, PublicHolidayApiResponse.class);
} catch (JsonProcessingException e) {
    log.error("JSON parsing failed: response={}", response, e);
    throw new RuntimeException("JSON 파싱 실패", e);
}
```

### 3. API 결과 코드 확인
```java
if (!"00".equals(header.getResultCode())) {
    // 00: 정상
    // 01: 애플리케이션 에러
    // 03: 데이터베이스 에러
    // 04: HTTP 에러
    // 05: 서비스 연결 실패
    // 10: 잘못된 요청 파라미터
    // 12: 해당하는 데이터가 없음
    // 22: 서비스키가 유효하지 않음
    // 30: 활용 승인이 나지 않음
    // 31: 트래픽 초과
    
    log.error("API returned error: code={}, msg={}", 
             header.getResultCode(), header.getResultMsg());
    throw new RuntimeException("공휴일 API 호출 실패: " + header.getResultMsg());
}
```

### 4. 날짜 파싱 실패
```java
try {
    LocalDate date = LocalDate.parse(item.getLocdate(), 
                                    DateTimeFormatter.ofPattern("yyyyMMdd"));
} catch (DateTimeParseException e) {
    log.error("Date parsing failed: locdate={}", item.getLocdate(), e);
    continue; // 해당 항목은 건너뛰고 계속 진행
}
```

---

## 테스트 방법

### 1. 수동 동기화 API 테스트
```bash
# 특정 월 동기화
curl -X POST "http://localhost:8080/api/holidays/sync?year=2026&month=1"

# 응답
{
  "success": true,
  "message": "공휴일 동기화가 완료되었습니다.",
  "year": 2026,
  "month": 1,
  "syncedCount": 3
}
```

### 2. 전체 연도 동기화
```bash
# 2026년 전체 동기화
curl -X POST "http://localhost:8080/api/holidays/sync-year?year=2026"

# 응답
{
  "success": true,
  "message": "2026년 전체 공휴일 동기화가 완료되었습니다.",
  "year": 2026,
  "totalSyncedCount": 15
}
```

### 3. 공휴일 확인
```bash
# 특정 날짜 확인
curl -X GET "http://localhost:8080/api/holidays/check?date=2026-01-01"

# 응답
{
  "date": "2026-01-01",
  "isHoliday": true,
  "isBookable": false,
  "holidayName": "신정",
  "description": "1월 1일"
}
```

### 4. 공휴일 목록 조회
```bash
# 특정 월 목록
curl -X GET "http://localhost:8080/api/holidays?year=2026&month=1"

# 응답
[
  {
    "id": 1,
    "holidayDate": "2026-01-01",
    "holidayName": "신정",
    "description": "1월 1일",
    "isHoliday": true
  },
  {
    "id": 2,
    "holidayDate": "2026-01-29",
    "holidayName": "설날",
    "description": "설날 당일",
    "isHoliday": true
  }
]
```

### 5. 예약 시 공휴일 체크 테스트
```bash
# 공휴일에 예약 시도 (실패해야 함)
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "roomId": 1,
    "checkInDate": "2026-01-01",
    "checkOutDate": "2026-01-03",
    "guestCount": 2
  }'

# 응답 (에러)
{
  "timestamp": "2026-01-31T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "공휴일에는 예약할 수 없습니다: 2026-01-01 (신정)"
}
```

---

## 로그 확인

### 애플리케이션 시작 시 로그
```
2026-01-31 10:00:00 INFO  HolidayScheduler - === Initializing holidays on application startup ===
2026-01-31 10:00:00 INFO  HolidayScheduler - Syncing all holidays for year=2026
2026-01-31 10:00:00 INFO  PublicHolidayApiClientImpl - Fetching holidays from API: year=2026, month=1
2026-01-31 10:00:00 INFO  PublicHolidayApiClientImpl - Request URL: http://apis.data.go.kr/.../getRestDeInfo?ServiceKey=***&solYear=2026&solMonth=01...
2026-01-31 10:00:01 INFO  PublicHolidayApiClientImpl - API Response received, length: 1234
2026-01-31 10:00:01 INFO  HolidayUseCaseImpl - Synced 3 holidays for year=2026, month=1
...
2026-01-31 10:00:30 INFO  HolidayScheduler - Year 2026 holiday sync completed: total count=15
2026-01-31 10:00:30 INFO  HolidayScheduler - === Holiday initialization completed ===
```

---

## 주요 특징

### 구현된 내용
1. RestTemplate을 통한 HTTP 통신: GET 요청으로 Open API 호출
2. JSON 파싱: Jackson ObjectMapper로 응답 변환
3. 자동 동기화: 스케줄러로 애플리케이션 시작 시 + 정기적으로 동기화
4. 중복 체크: 이미 저장된 공휴일은 건너뛰기
5. 에러 처리: 다양한 예외 상황 처리 및 로깅
6. 예약 연동: 공휴일 체크 후 예약 차단

### 핵심 포인트
- URL 인코딩: UriComponentsBuilder로 자동 인코딩
- API 호출 제한: 100ms 딜레이로 초당 요청 제한 준수
- 로그 마스킹: Service Key를 로그에 노출하지 않음
- 트랜잭션: @Transactional로 데이터 일관성 보장

### 데이터 플로우
```
Open API → JSON → Java Object → Entity → Database → 예약 체크
```

---

## 요약

### 공공데이터 Open API 연동 완료
1. API 키 발급: 공공데이터포털에서 인증키 발급
2. RestTemplate 설정: Spring Boot의 HTTP 클라이언트
3. API 호출: UriComponentsBuilder로 URL 구성
4. JSON 파싱: Jackson으로 응답 변환
5. 데이터 저장: JPA로 DB에 저장
6. 자동 동기화: 스케줄러로 정기적 업데이트
7. 예약 연동: 공휴일 체크 로직 적용

### 학습 포인트
- REST API 호출 방법
- JSON 데이터 파싱
- Spring Scheduler 사용법
- 외부 API 연동 시 에러 처리

---

**문서 버전**: 1.0  
**최종 업데이트**: 2026-01-31  
**작성자**: 유희재

