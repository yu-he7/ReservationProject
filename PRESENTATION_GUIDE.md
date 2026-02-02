# 프로젝트 발표 준비 가이드

## 목차
1. [프로젝트 개요 설명](#프로젝트-개요-설명)
2. [JWT 인증 시스템 - 핵심 설명](#jwt-인증-시스템---핵심-설명)
3. [Open API 연동 - 핵심 설명](#open-api-연동---핵심-설명)
4. [예상 질문과 답변](#예상-질문과-답변)
5. [시연 시나리오](#시연-시나리오)
6. [기술 아키텍처 요약](#기술-아키텍처-요약)

---

## 프로젝트 개요 설명

### 30초 요약
"공공데이터포털의 공휴일 API를 연동하여, 공휴일에는 예약을 할 수 없도록 제한하는 숙소 예약 시스템입니다. JWT 기반 인증과 Spring Security를 사용했고, Clean Architecture를 적용하여 도메인별로 레이어를 분리했습니다."

### 핵심 기능 3가지
1. **JWT 인증**: Access Token(1시간) + Refresh Token(7일) 분리 설계
2. **Open API 연동**: 공공데이터포털에서 공휴일 자동 동기화
3. **예약 시스템**: 비관적 락으로 중복 예약 방지, 공휴일 체크

### 사용 기술 스택
- Backend: Spring Boot 3.2.1, Java 17
- Database: MySQL 8.x, JPA/Hibernate
- Security: Spring Security + JWT (JJWT 라이브러리)
- External API: RestTemplate로 공공데이터포털 연동
- Architecture: Clean Architecture (api → usecase → model → infra)

---

## JWT 인증 시스템 - 핵심 설명

### 1. JWT가 무엇인가?
JWT(JSON Web Token)는 JSON 형태의 정보를 안전하게 전송하기 위한 토큰입니다.

**구조**
```
Header.Payload.Signature

예시:
eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6MSwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIn0.xxx
```

- **Header**: 알고리즘 정보 (HS256)
- **Payload**: 사용자 정보 (memberId, email, role 등)
- **Signature**: 위변조 방지를 위한 서명

### 2. 왜 JWT를 사용했는가?
**기존 세션 방식의 문제점**
- 서버에 세션을 저장해야 함 (메모리 사용)
- 여러 서버로 확장 시 세션 공유 문제

**JWT의 장점**
- 서버에 상태를 저장하지 않음 (Stateless)
- 토큰에 필요한 정보를 모두 포함
- 확장성이 좋음 (여러 서버에 쉽게 배포 가능)

### 3. 구현한 방식

#### Access Token + Refresh Token 분리
```
Access Token: 1시간 (짧음) → 자주 사용, 탈취 시 피해 최소화
Refresh Token: 7일 (김) → Access Token 재발급용
```

#### 전체 인증 플로우
```
1. 로그인
   POST /api/members/login
   → 이메일/비밀번호 검증
   → Access Token + Refresh Token 발급

2. API 호출
   GET /api/reservations/my
   Header: Authorization: Bearer {accessToken}
   → JWT 필터가 자동으로 토큰 검증
   → SecurityContext에 사용자 정보 저장
   → Controller에서 현재 로그인 유저 정보 사용

3. 토큰 만료 시
   POST /api/members/refresh
   Body: { "refreshToken": "..." }
   → Refresh Token 검증
   → 새로운 Access Token + Refresh Token 발급
```

### 4. 핵심 코드 설명

#### JwtTokenProvider (토큰 생성/검증)
```java
public String createAccessToken(Long memberId, String email, String role) {
    return Jwts.builder()
        .setSubject(String.valueOf(memberId))  // 토큰 주체
        .claim("memberId", memberId)           // 사용자 ID
        .claim("email", email)                 // 이메일
        .claim("role", role)                   // 권한
        .setIssuedAt(new Date())              // 발급 시간
        .setExpiration(new Date(now + 3600000)) // 만료 시간 (1시간)
        .signWith(getSigningKey())            // HS256으로 서명
        .compact();
}

public boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token);  // 서명 검증 + 만료 시간 체크
        return true;
    } catch (ExpiredJwtException e) {
        return false;  // 만료된 토큰
    }
}
```

**설명할 포인트**
- `setSubject()`: 토큰의 주체 (여기서는 memberId)
- `claim()`: 추가 정보 저장 (key-value 형태)
- `signWith()`: 비밀키로 서명 (HS256 알고리즘)
- `parseClaimsJws()`: 서명 검증 + 파싱

#### JwtAuthenticationFilter (Spring Security 통합)
```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                 HttpServletResponse response, 
                                 FilterChain filterChain) {
    // 1. Authorization 헤더에서 토큰 추출
    String token = resolveToken(request);
    
    // 2. 토큰 검증
    if (token != null && jwtTokenProvider.validateToken(token)) {
        // 3. 토큰에서 사용자 정보 추출
        Claims claims = jwtTokenProvider.getClaims(token);
        Long memberId = claims.get("memberId", Long.class);
        String role = claims.get("role", String.class);
        
        // 4. Spring Security의 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(memberId, null, authorities);
        
        // 5. SecurityContext에 저장 (이후 모든 곳에서 사용 가능)
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    // 6. 다음 필터로 진행
    filterChain.doFilter(request, response);
}
```

**설명할 포인트**
- Filter: 모든 HTTP 요청이 Controller에 도달하기 전에 실행됨
- SecurityContext: Spring Security가 인증 정보를 저장하는 곳
- 한 번 필터에서 인증하면, 이후 모든 곳에서 사용 가능

#### SecurityUtil (현재 로그인 유저 정보 가져오기)
```java
public static Long getCurrentMemberId() {
    Authentication authentication = SecurityContextHolder
        .getContext()
        .getAuthentication();
    
    if (authentication == null || authentication.getName() == null) {
        throw new UnauthorizedException("인증되지 않은 사용자");
    }
    
    return Long.parseLong(authentication.getName());
}
```

**Controller에서 사용 예시**
```java
@GetMapping("/my")
public List<ReservationResponse> getMyReservations() {
    Long memberId = SecurityUtil.getCurrentMemberId();  // 현재 로그인 유저 ID
    return reservationService.getMyReservations(memberId);
}
```

### 5. 보안 고려사항

#### Secret Key 관리
```yaml
# application.yml
jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation1234567890
```
- 실제 운영에서는 환경 변수로 관리해야 함
- 최소 256비트 (32자) 이상 권장

#### HTTPS 필수
- JWT는 암호화가 아닌 서명(Signature)만 있음
- HTTP에서는 토큰이 평문으로 전송되어 탈취 위험
- 운영 환경에서는 반드시 HTTPS 사용

#### 짧은 만료 시간
- Access Token: 1시간 (짧게 설정하여 탈취 시 피해 최소화)
- Refresh Token: 7일 (긴 시간이지만 재발급 용도로만 사용)

---

## Open API 연동 - 핵심 설명

### 1. Open API가 무엇인가?
Open API는 외부에서 제공하는 공개 API입니다. 공공데이터포털에서 제공하는 "특일정보 API"를 사용했습니다.

**특일정보 API**
- 제공: 행정안전부
- 내용: 공휴일, 기념일 정보
- 형식: XML 또는 JSON
- 호출 제한: 일 1,000건, 초당 10건

### 2. 왜 Open API를 사용했는가?
- 공휴일 데이터를 직접 입력하면 누락 가능
- 매년 변경되는 공휴일(설날, 추석 등) 자동 반영
- 공공데이터를 활용한 실무 경험

### 3. 구현한 방식

#### API 호출 URL 구조
```
https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo
?serviceKey={인증키}
&solYear=2026
&solMonth=01
&_type=json
```

#### 전체 동기화 플로우
```
1. 애플리케이션 시작 시
   → DataInitializer가 2년치 공휴일 자동 동기화

2. 스케줄러 (자동)
   → 매년 1월 1일 새벽 3시: 전체 연도 동기화
   → 매월 1일 새벽 4시: 해당 월 동기화

3. 수동 동기화 (관리자)
   → POST /api/holidays/sync?year=2026&month=1
   → POST /api/holidays/sync-year?year=2026

4. 예약 시 공휴일 체크
   → 예약 생성 전 자동으로 공휴일 여부 확인
   → 공휴일이면 예외 발생 (예약 불가)
```

### 4. 핵심 코드 설명

#### HolidayApiClient (API 호출)
```java
public String callHolidayApi(int year, int month) {
    // 1. URL 구성 (UriComponentsBuilder로 자동 인코딩)
    String url = UriComponentsBuilder
        .fromHttpUrl(baseUrl)
        .queryParam("serviceKey", serviceKey)
        .queryParam("solYear", year)
        .queryParam("solMonth", String.format("%02d", month))
        .queryParam("_type", "json")
        .build(true)  // 이미 인코딩된 파라미터 사용
        .toUriString();
    
    // 2. RestTemplate으로 GET 요청
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    
    // 3. 응답 확인
    if (response.getStatusCode() == HttpStatus.OK) {
        return response.getBody();
    }
    
    throw new RuntimeException("API 호출 실패");
}
```

**설명할 포인트**
- `UriComponentsBuilder`: URL을 안전하게 구성 (자동 인코딩)
- `RestTemplate`: Spring에서 제공하는 HTTP 클라이언트
- `getForEntity()`: GET 요청 + 응답을 String으로 받음

#### JSON 파싱
```java
public List<HolidayDto> parseHolidayResponse(String jsonResponse) {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode rootNode = objectMapper.readTree(jsonResponse);
    
    // JSON 구조: response → body → items → item → [...]
    JsonNode itemsNode = rootNode
        .path("response")
        .path("body")
        .path("items")
        .path("item");
    
    List<HolidayDto> holidays = new ArrayList<>();
    
    if (itemsNode.isArray()) {
        for (JsonNode itemNode : itemsNode) {
            String dateName = itemNode.path("dateName").asText();  // 공휴일 이름
            String locdate = itemNode.path("locdate").asText();    // 날짜 (20260101)
            
            // 날짜 변환: "20260101" → LocalDate(2026-01-01)
            LocalDate date = LocalDate.parse(locdate, 
                DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            holidays.add(new HolidayDto(date, dateName, true));
        }
    }
    
    return holidays;
}
```

**설명할 포인트**
- `ObjectMapper`: JSON을 Java 객체로 변환 (Jackson 라이브러리)
- `path()`: JSON 경로 탐색
- `asText()`: JSON 값을 String으로 추출

#### 데이터베이스 저장
```java
@Transactional
public void syncHolidays(int year, int month) {
    // 1. API 호출
    String jsonResponse = holidayApiClient.callHolidayApi(year, month);
    
    // 2. JSON 파싱
    List<HolidayDto> holidays = holidayApiClient.parseHolidayResponse(jsonResponse);
    
    // 3. 데이터베이스 저장 (중복 체크)
    for (HolidayDto dto : holidays) {
        // 이미 존재하는지 확인
        if (holidayRepository.existsByHolidayDate(dto.getHolidayDate())) {
            continue;  // 이미 있으면 건너뛰기
        }
        
        // Entity 생성 및 저장
        Holiday holiday = Holiday.builder()
            .holidayDate(dto.getHolidayDate())
            .holidayName(dto.getHolidayName())
            .isHoliday(dto.isHoliday())
            .description(dto.getDescription())
            .build();
        
        holidayRepository.save(holiday);
    }
}
```

**설명할 포인트**
- `@Transactional`: 전체가 성공하거나 전체가 실패 (원자성)
- 중복 체크: 같은 날짜가 이미 있으면 저장하지 않음
- Builder 패턴: 객체 생성 시 가독성 향상

#### 스케줄러 설정
```java
@Component
public class HolidaySyncScheduler {
    
    // 매년 1월 1일 새벽 3시
    @Scheduled(cron = "0 0 3 1 1 ?")
    public void syncYearlyHolidays() {
        int currentYear = LocalDate.now().getYear();
        // 올해와 내년 공휴일 동기화
        syncService.syncYear(currentYear);
        syncService.syncYear(currentYear + 1);
    }
    
    // 매월 1일 새벽 4시
    @Scheduled(cron = "0 0 4 1 * ?")
    public void syncMonthlyHolidays() {
        LocalDate now = LocalDate.now();
        syncService.syncHolidays(now.getYear(), now.getMonthValue());
    }
}
```

**Cron 표현식 설명**
```
"0 0 3 1 1 ?"
 │ │ │ │ │ │
 │ │ │ │ │ └─ 요일 (? = 상관없음)
 │ │ │ │ └─── 월 (1 = 1월)
 │ │ │ └───── 일 (1 = 1일)
 │ │ └─────── 시 (3 = 새벽 3시)
 │ └───────── 분 (0 = 0분)
 └─────────── 초 (0 = 0초)
```

### 5. 예약 시 공휴일 체크
```java
@Transactional
public ReservationResponse createReservation(CreateReservationRequest request, Long memberId) {
    LocalDate checkInDate = request.getCheckInDate();
    LocalDate checkOutDate = request.getCheckOutDate();
    
    // 체크인~체크아웃 사이의 모든 날짜 확인
    LocalDate date = checkInDate;
    while (!date.isAfter(checkOutDate)) {
        // 공휴일 체크
        if (holidayService.isHoliday(date)) {
            Holiday holiday = holidayService.getHoliday(date);
            throw new BusinessException(
                String.format("공휴일(%s)에는 예약할 수 없습니다.", 
                    holiday.getHolidayName())
            );
        }
        date = date.plusDays(1);
    }
    
    // 예약 진행...
}
```

---

## 예상 질문과 답변

### JWT 관련 질문

**Q1: JWT와 세션 방식의 차이는?**
A: 세션은 서버에 사용자 정보를 저장하지만, JWT는 토큰 자체에 정보를 담아 클라이언트가 보관합니다. JWT는 서버 메모리를 사용하지 않아 확장성이 좋고, 여러 서버로 쉽게 배포할 수 있습니다.

**Q2: Access Token과 Refresh Token을 왜 분리했나?**
A: Access Token은 자주 사용되므로 탈취 위험이 있어 짧게(1시간) 설정했습니다. Refresh Token은 재발급 용도로만 사용하고 7일로 길게 설정하여, 사용자가 매번 로그인하지 않아도 되게 했습니다.

**Q3: JWT의 보안 약점은?**
A: 
1. 토큰이 탈취되면 만료될 때까지 사용 가능 → 짧은 만료 시간으로 완화
2. 토큰을 강제로 무효화할 수 없음 → Redis 블랙리스트로 해결 가능 (현재 미구현)
3. HTTPS 필수 (토큰이 평문으로 전송됨)

**Q4: JWT 서명은 어떻게 검증하나?**
A: HMAC-SHA256 알고리즘으로 서명합니다. 서버의 Secret Key로 Header + Payload를 해싱한 값과, 토큰의 Signature가 일치하는지 확인합니다. Secret Key가 없으면 서명을 위조할 수 없습니다.

**Q5: SecurityContext는 무엇인가?**
A: Spring Security가 현재 요청의 인증 정보를 저장하는 곳입니다. Filter에서 한 번 인증하면, 이후 Controller, Service 어디서든 SecurityContextHolder로 접근할 수 있습니다. 각 HTTP 요청마다 별도의 SecurityContext가 생성됩니다.

### Open API 관련 질문

**Q6: RestTemplate 대신 다른 방법은?**
A: WebClient(Spring WebFlux)나 Feign Client도 가능합니다. RestTemplate은 동기 방식이지만 사용이 간단하고, 이 프로젝트에서는 공휴일 동기화가 실시간성이 중요하지 않아 RestTemplate을 선택했습니다.

**Q7: API 호출 실패 시 어떻게 처리하나?**
A: 
1. HTTP 에러: try-catch로 잡아서 로그 남기고 재시도
2. JSON 파싱 에러: 응답 형식 확인 후 에러 로그
3. 스케줄러가 자동으로 재시도하므로 일시적 실패는 문제없음

**Q8: API 호출 제한(일 1,000건)은 어떻게 관리하나?**
A: 
1. 스케줄러로 정해진 시간에만 호출 (월 1회, 연 1회)
2. 중복 저장 방지 (이미 있는 공휴일은 건너뛰기)
3. 실제로는 월 12회 + 연 1회 = 약 13회만 호출

**Q9: 공휴일 데이터가 없으면?**
A: 애플리케이션 시작 시 DataInitializer가 2년치 데이터를 자동으로 동기화합니다. 만약 없다면 관리자가 수동으로 동기화 API를 호출할 수 있습니다.

### 아키텍처 관련 질문

**Q10: Clean Architecture를 왜 사용했나?**
A: 
1. 비즈니스 로직(UseCase)과 외부 의존성(API, DB)을 분리
2. 테스트하기 쉬움 (UseCase만 독립적으로 테스트 가능)
3. 확장성이 좋음 (DB를 MySQL에서 PostgreSQL로 바꿔도 UseCase는 수정 불필요)

**Q11: 비관적 락은 언제 사용했나?**
A: 예약 생성 시 중복 예약을 방지하기 위해 사용했습니다. 같은 객실에 동시에 여러 예약 요청이 오면, SELECT FOR UPDATE로 행을 락 걸어 순차적으로 처리합니다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM Room r WHERE r.id = :roomId")
Optional<Room> findByIdWithLock(@Param("roomId") Long roomId);
```

**Q12: Validation은 어떻게 했나?**
A: Jakarta Validation (Hibernate Validator)를 사용했습니다.

```java
public class SignupRequest {
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;
    
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다")
    private String name;
}
```

Controller에서 `@Valid`를 붙이면 자동으로 검증하고, 실패 시 400 에러 반환합니다.

---

## 시연 시나리오

### 준비사항
```bash
# 1. MySQL 실행 확인
mysql -u accoheej -p

# 2. 애플리케이션 실행
gradlew.bat bootRun

# 3. Postman 또는 cURL 준비
```

### 시나리오 1: JWT 인증 플로우

#### 1단계: 회원가입
```bash
POST http://localhost:8080/api/members/signup
Content-Type: application/json

{
  "email": "demo@example.com",
  "password": "password123!",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}

# 결과: 회원 정보 반환 (비밀번호는 BCrypt로 암호화되어 저장됨)
```

#### 2단계: 로그인
```bash
POST http://localhost:8080/api/members/login
Content-Type: application/json

{
  "email": "demo@example.com",
  "password": "password123!"
}

# 결과: accessToken, refreshToken 반환
# accessToken 복사해두기!
```

#### 3단계: 내 정보 조회 (인증 필요)
```bash
GET http://localhost:8080/api/members/me
Authorization: Bearer {복사한_accessToken}

# 결과: 로그인한 사용자 정보 반환
# JwtAuthenticationFilter가 토큰을 검증하고 SecurityContext에 저장
```

#### 4단계: 토큰 갱신
```bash
POST http://localhost:8080/api/members/refresh
Content-Type: application/json

{
  "refreshToken": "{복사한_refreshToken}"
}

# 결과: 새로운 accessToken, refreshToken 발급
```

### 시나리오 2: Open API 공휴일 동기화

#### 1단계: 2026년 1월 공휴일 동기화
```bash
POST http://localhost:8080/api/holidays/sync?year=2026&month=1

# 결과: 공휴일 3개 동기화 완료 (신정, 설날 등)
# 백그라운드: API 호출 → JSON 파싱 → DB 저장
```

#### 2단계: 공휴일 확인
```bash
GET http://localhost:8080/api/holidays/check?date=2026-01-01

# 결과:
{
  "date": "2026-01-01",
  "isHoliday": true,
  "holidayName": "신정"
}
```

#### 3단계: 공휴일에 예약 시도 (실패)
```bash
POST http://localhost:8080/api/reservations
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "roomId": 1,
  "checkInDate": "2026-01-01",
  "checkOutDate": "2026-01-02",
  "guestCount": 2
}

# 결과: 400 Bad Request
{
  "message": "공휴일(신정)에는 예약할 수 없습니다."
}
```

#### 4단계: 평일에 예약 (성공)
```bash
POST http://localhost:8080/api/reservations
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "roomId": 1,
  "checkInDate": "2026-02-10",
  "checkOutDate": "2026-02-12",
  "guestCount": 2
}

# 결과: 201 Created
{
  "id": 1,
  "totalPrice": 200000.00,
  "status": "PENDING"
}
```

### 시연 시 강조할 포인트

**JWT 인증**
1. 로그인 시 토큰 2개 발급 (Access, Refresh 분리)
2. 이후 모든 API 호출 시 Authorization 헤더에 토큰 포함
3. 서버는 매번 DB 조회 없이 토큰만으로 사용자 확인

**Open API 연동**
1. 공공데이터포털에서 실시간으로 공휴일 정보 가져옴
2. 스케줄러가 자동으로 동기화 (수동 개입 불필요)
3. 예약 시 자동으로 공휴일 체크하여 차단

**비관적 락**
1. 같은 객실에 동시 예약 시도 시 순차 처리
2. 데이터 일관성 보장

---

## 기술 아키텍처 요약

### 전체 구조
```
[ 클라이언트 ]
      ↓
[ JWT Filter ] ← 모든 요청이 여기를 통과
      ↓
[ Security Context ] ← 인증 정보 저장
      ↓
[ Controller (API Layer) ] ← REST API 엔드포인트
      ↓
[ UseCase (Business Logic) ] ← 비즈니스 로직
      ↓
[ Repository (Data Layer) ] ← DB 접근
      ↓
[ MySQL Database ]

[ Scheduler ] → 정해진 시간에 자동 실행
      ↓
[ Open API Client ] → RestTemplate로 외부 API 호출
```

### 패키지 구조
```
heej.net/
├── config/                 # 설정 클래스
│   ├── SecurityConfig      # Spring Security 설정
│   ├── JpaConfig          # JPA 설정
│   └── SchedulerConfig    # 스케줄러 설정
│
├── domain/
│   ├── member/
│   │   ├── api/           # MemberEndpoint (Controller)
│   │   ├── usecase/       # MemberService (비즈니스 로직)
│   │   ├── model/         # Member (Entity)
│   │   └── infra/         # MemberRepository (DB)
│   │
│   ├── holiday/
│   │   ├── api/           # HolidayEndpoint
│   │   ├── usecase/       # HolidayService
│   │   ├── model/         # Holiday (Entity)
│   │   ├── infra/         # HolidayApiClient (Open API)
│   │   └── scheduler/     # HolidaySyncScheduler
│   │
│   └── reservation/
│       ├── api/           # ReservationEndpoint
│       ├── usecase/       # ReservationService
│       ├── model/         # Reservation (Entity)
│       └── infra/         # ReservationRepository
│
└── security/
    ├── JwtTokenProvider          # JWT 생성/검증
    ├── JwtAuthenticationFilter   # Spring Security Filter
    └── SecurityUtil              # 현재 유저 정보 조회
```

### 핵심 의존성
```gradle
dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    // Database
    implementation 'mysql:mysql-connector-java'
    implementation 'org.flywaydb:flyway-core'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

---

## 마지막 체크리스트

### 꼭 기억할 것
- [ ] JWT는 무상태(Stateless) 인증 방식
- [ ] Access Token 1시간, Refresh Token 7일
- [ ] Filter → SecurityContext → Controller 흐름
- [ ] Open API는 RestTemplate로 호출
- [ ] 스케줄러가 자동으로 공휴일 동기화
- [ ] 예약 시 공휴일 자동 체크
- [ ] 비관적 락으로 중복 예약 방지
- [ ] Clean Architecture (api → usecase → model → infra)

### 추가 질문 대비
- [ ] Redis는 왜 안 썼나? → 설정만 해두고 구현 시간 부족
- [ ] 테스트 코드는? → 시간 부족으로 미구현, 추후 추가 예정
- [ ] 결제 시스템은? → 예약 시스템에 집중, 확장 시 토스페이먼츠 연동 계획

### 자신감 있게 말할 것
1. "6주 동안 JWT 인증과 Open API 연동을 중심으로 개발했습니다."
2. "Clean Architecture를 적용하여 유지보수성을 고려했습니다."
3. "비관적 락으로 동시성 문제를 해결했습니다."
4. "스케줄러로 공휴일을 자동 동기화하여 관리 부담을 줄였습니다."

---

**작성일**: 2026-02-01  
**작성자**: 유희재  
**목적**: 내일 선임님께 프로젝트 설명하기 위한 준비 문서

화이팅하세요! 충분히 잘 만드셨습니다. 자신감 가지고 설명하시면 됩니다.

