# 숙소 예약 시스템 프로젝트

## 프로젝트 소개

공공데이터포털의 특일정보 API를 활용하여 공휴일에는 예약을 할 수 없도록 제한하는 숙소 예약 시스템입니다.  
JWT 인증 시스템과 Open API 연동을 실습하기 위해 6주간 개발한 프로젝트입니다.

## 핵심 기능

### 1. 회원 관리
- 회원가입 / 로그인
- JWT 기반 인증 (Access Token + Refresh Token)
- 토큰 갱신 / 로그아웃
- 내 정보 조회

### 2. JWT 인증 시스템
- HS256 알고리즘 기반 JWT 토큰 생성/검증
- Spring Security 통합
- Access Token (1시간) / Refresh Token (7일)
- 비관적 락을 통한 동시성 제어

### 3. 공휴일 관리 (Open API 연동)
- 공공데이터포털 특일정보 API 연동
- RestTemplate을 통한 HTTP 통신
- 자동 동기화 (애플리케이션 시작 시 + 스케줄러)
- 공휴일 조회 / 확인 API

### 4. 숙소 / 객실 관리
- 숙소 등록 / 조회 / 검색 (도시, 지역, 타입, 평점)
- 객실 등록 / 조회
- 이미지 다중 등록 및 관리
- 페이징 처리

### 5. 예약 관리
- 예약 생성 (공휴일 자동 체크)
- 중복 예약 방지 (비관적 락)
- 예약 취소 (취소 사유 포함)
- 내 예약 목록 / 상세 조회
- 객실 가용성 확인

## 기술 스택

### Backend
- Framework: Spring Boot 3.2.1
- Language: Java 17
- Build Tool: Gradle 8.5

### Database
- Database: MySQL 8.x
- ORM: JPA / Hibernate
- Migration: Flyway

### Security
- Authentication: Spring Security + JWT
- Password: BCrypt
- Token: JJWT (io.jsonwebtoken)

### External API
- Open API: 공공데이터포털 특일정보 API
- HTTP Client: RestTemplate

### Others
- Scheduler: Spring Scheduler
- Validation: Jakarta Validation
- Logging: SLF4J + Logback
- Lombok: 코드 간소화

## 프로젝트 구조

```
src/main/java/heej/net/
├── accommodationReservationApplication.java  # Main Class
├── common/
│   └── exception/
│       └── GlobalExceptionHandler.java       # 전역 예외 처리
├── config/
│   ├── DataInitializer.java                  # 초기 데이터 설정
│   ├── JpaConfig.java                         # JPA 설정
│   ├── RestTemplateConfig.java                # RestTemplate 설정
│   ├── SchedulerConfig.java                   # 스케줄러 설정
│   └── SecurityConfig.java                    # Spring Security 설정
├── domain/
│   ├── member/                                # 회원 도메인
│   │   ├── api/                               # REST API Controller
│   │   ├── usecase/                           # 비즈니스 로직
│   │   ├── model/                             # 엔티티
│   │   ├── infra/                             # Repository
│   │   └── util/                              # JWT, Redis
│   ├── holiday/                               # 공휴일 도메인
│   │   ├── api/
│   │   ├── usecase/
│   │   ├── model/
│   │   ├── infra/                             # Open API Client
│   │   └── scheduler/                         # 자동 동기화
│   ├── accommodation/                         # 숙소/객실 도메인
│   │   ├── api/
│   │   ├── usecase/
│   │   ├── model/
│   │   └── infra/
│   └── reservation/                           # 예약 도메인
│       ├── api/
│       ├── usecase/
│       ├── model/
│       └── infra/
├── security/
│   ├── JwtAuthenticationFilter.java           # JWT 인증 필터
│   └── util/
│       └── SecurityUtil.java                  # 인증 유틸리티
└── health/
    └── HealthCheckController.java             # 헬스 체크 API

src/main/resources/
├── application.yml                            # 설정 파일
└── db/migration/
    └── V4__add_amenities_to_rooms.sql         # Flyway 마이그레이션
```

---

## 📚 문서 목록

프로젝트의 모든 기능과 구현 방법을 상세하게 문서화했습니다.

### 필독 문서

#### 1. [PROJECT_EVALUATION.md](PROJECT_EVALUATION.md)
6주 프로젝트 종합 평가 및 분석 보고서
- 구현된 모든 기능 목록
- 아키텍처 평가 (85/100점)
- 강점과 개선 필요 사항
- 추가로 구현하면 좋을 기능 (우선순위별)
- 선임님께 보여드릴 때 강조할 포인트

#### 2. [API_COMPLETE_DOCUMENTATION.md](API_COMPLETE_DOCUMENTATION.md)
전체 API 명세서 (상세)
- 모든 API 엔드포인트 정의
- Request/Response 예시
- HTTP 상태 코드
- 에러 응답 형식
- cURL 예시

#### 3. [JWT_IMPLEMENTATION_GUIDE.md](JWT_IMPLEMENTATION_GUIDE.md)
JWT 인증 시스템 구현 가이드
- JWT란 무엇인가?
- 토큰 생성/검증 과정
- Spring Security 통합
- 인증 플로우 다이어그램
- 보안 고려사항
- 코드 상세 설명

#### 4. [OPENAPI_INTEGRATION_GUIDE.md](OPENAPI_INTEGRATION_GUIDE.md)
공공데이터 Open API 연동 가이드
- Open API란?
- API 키 발급 방법
- RestTemplate 구현
- 스케줄러를 통한 자동 동기화
- JSON 파싱
- 에러 처리

#### 5. [DATABASE_ERD_DIAGRAM.md](DATABASE_ERD_DIAGRAM.md)
데이터베이스 ERD 및 테이블 분석
- DBML ERD 다이어그램
- 7개 테이블 상세 분석
- 관계 분석 (1:N, FK)
- 인덱스 전략
- 데이터 무결성

### 참고 문서

#### 6. [API_TEST_GUIDE.md](API_TEST_GUIDE.md)
API 테스트 가이드
- 초기 데이터가 없는 경우 테스트 방법
- 회원가입 → 로그인 → 예약 전체 플로우
- HTTP 요청 예시

#### 7. [HOLIDAY_SYNC_GUIDE.md](HOLIDAY_SYNC_GUIDE.md)
공휴일 동기화 가이드
- 자동 동기화 스케줄
- 수동 동기화 API
- 테스트 시나리오

#### 8. [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)
데이터베이스 스키마 (SQL)
- 모든 테이블 CREATE 문
- 샘플 데이터 INSERT 문

## 빠른 시작

### 1. 사전 요구사항
```bash
- JDK 17 이상
- MySQL 8.x
- Gradle 8.5 (또는 ./gradlew 사용)
```

### 2. 데이터베이스 설정
```sql
-- MySQL에 로그인
mysql -u root -p

-- 데이터베이스 생성
CREATE DATABASE accommodation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER 'accoheej'@'localhost' IDENTIFIED BY 'hjmj0723@@';
GRANT ALL PRIVILEGES ON accommodation.* TO 'accoheej'@'localhost';
FLUSH PRIVILEGES;
```

### 3. application.yml 설정
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/accommodation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: accoheej
    password: hjmj0723@@

jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation1234567890
  access-token-expiration: 3600000      # 1시간
  refresh-token-expiration: 604800000   # 7일

public-holiday:
  api:
    service-key: {공공데이터포털에서 발급받은 인증키}
```

### 4. 애플리케이션 실행
```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

### 5. 서버 확인
```bash
# 헬스 체크
curl http://localhost:8080/api/health

# 응답
{
  "status": "UP",
  "timestamp": "2026-01-31T10:00:00"
}
```

## API 테스트 예시

### 1. 회원가입
```bash
curl -X POST http://localhost:8080/api/members/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123!",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678"
  }'
```

### 2. 로그인
```bash
curl -X POST http://localhost:8080/api/members/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123!"
  }'
```

### 3. 공휴일 확인
```bash
curl http://localhost:8080/api/holidays/check?date=2026-01-01
```

### 4. 예약 생성 (인증 필요)
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "roomId": 1,
    "checkInDate": "2026-02-10",
    "checkOutDate": "2026-02-12",
    "guestCount": 2
  }'
```

자세한 API 테스트 방법은 [API_TEST_GUIDE.md](API_TEST_GUIDE.md)를 참조하세요.

## 프로젝트 평가

### 종합 평가: 85/100점 (매우 우수)

| 항목 | 점수 | 평가 |
|------|------|------|
| 기능 완성도 | 9/10 | 핵심 요구사항 모두 구현 |
| 아키텍처 | 8/10 | Clean Architecture 적용 우수 |
| 코드 품질 | 7/10 | 가독성 좋으나 테스트 부재 |
| 보안 | 8/10 | JWT 인증 잘 구현됨 |
| 확장성 | 8/10 | 도메인 분리로 확장 용이 |
| 문서화 | 7/10 | README 충실하나 API 문서 부족 |

### 추가 구현 권장 사항 (우선순위별)

#### 높은 우선순위 (필수)
1. 테스트 코드 작성
   - 단위 테스트 (JUnit 5 + Mockito)
   - 통합 테스트 (@SpringBootTest)
   
2. Redis 완전 통합
   - Refresh Token 저장
   - 블랙리스트 실제 동작
   - 공휴일 데이터 캐싱

3. Swagger API 문서
   - Springdoc OpenAPI 적용
   - Try it out 기능

#### 중간 우선순위 (권장)
4. 리뷰 및 평점 시스템
5. 관리자 기능 강화
6. 이메일 알림

자세한 내용은 [PROJECT_EVALUATION.md](PROJECT_EVALUATION.md)를 참조하세요.

## 학습 포인트

이 프로젝트를 통해 다음을 학습했습니다:

### 1. JWT 인증 시스템
- JWT 토큰 생성/검증 원리
- Spring Security 통합
- 무상태(Stateless) 인증

### 2. Open API 연동
- RestTemplate 사용법
- JSON 파싱 (Jackson)
- 외부 API 에러 처리

### 3. 스케줄러
- Spring Scheduler
- Cron 표현식
- 자동화 구현

### 4. 동시성 제어
- 비관적 락 (Pessimistic Lock)
- 트랜잭션 관리
- 중복 예약 방지

### 5. Clean Architecture
- 도메인 주도 설계 (DDD)
- 레이어 분리 (api, usecase, model, infra)
- 의존성 역전 원칙

## 문의

프로젝트 관련 문의사항이 있으시면 연락주세요.

## 라이선스

이 프로젝트는 학습 목적으로 제작되었습니다.

공부 기간 : 2025.12.21 ~ 2026.01.02
프로젝트 기간: 2025.01.04 ~ 2026.02.01  
개발자: 유희재  
최종 업데이트: 2026-02-01

