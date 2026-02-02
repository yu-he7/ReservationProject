# 숙소 예약 시스템 - 전체 API 명세서

## 목차
1. [API 개요](#api-개요)
2. [인증 방식](#인증-방식)
3. [회원 관리 API](#회원-관리-api)
4. [공휴일 관리 API](#공휴일-관리-api)
5. [숙소 관리 API](#숙소-관리-api)
6. [객실 관리 API](#객실-관리-api)
7. [예약 관리 API](#예약-관리-api)
8. [헬스 체크 API](#헬스-체크-api)
9. [공통 응답 형식](#공통-응답-형식)
10. [에러 코드](#에러-코드)

---

## API 개요

### Base URL
```
http://localhost:8080
```

### Content-Type
```
application/json
```

### 인증 헤더 (필요 시)
```
Authorization: Bearer {accessToken}
```

---

## 인증 방식

### JWT (JSON Web Token) 기반 인증
- **Access Token**: 1시간 유효
- **Refresh Token**: 7일 유효
- 인증이 필요한 API는 `Authorization` 헤더에 `Bearer {accessToken}` 포함

---

## 회원 관리 API

### 1. 회원가입
```http
POST /api/members/signup
```

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123!",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```

**Response (201 Created)**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "role": "USER",
  "status": "ACTIVE",
  "createdAt": "2026-01-31T10:00:00",
  "updatedAt": "2026-01-31T10:00:00"
}
```

**Validation**
- email: 필수, 이메일 형식, 최대 50자
- password: 필수, 최소 8자
- name: 필수, 최대 50자
- phoneNumber: 선택, 최대 20자

---

### 2. 로그인
```http
POST /api/members/login
```

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

**Response (200 OK)**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6MSwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJzdWIiOiIxIiwiaWF0IjoxNzM4MzY4MDAwLCJleHAiOjE3MzgzNzE2MDB9.xxx",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6MSwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJzdWIiOiIxIiwiaWF0IjoxNzM4MzY4MDAwLCJleHAiOjE3Mzg5NzI4MDB9.yyy",
  "tokenType": "Bearer"
}
```

---

### 3. 토큰 갱신
```http
POST /api/members/refresh
```

**Request Body**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK)**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...(new)",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...(new)",
  "tokenType": "Bearer"
}
```

---

### 4. 로그아웃
```http
POST /api/members/logout
Authorization: Bearer {accessToken}
```

**Response (204 No Content)**

---

### 5. 내 정보 조회
```http
GET /api/members/me
Authorization: Bearer {accessToken}
```

**Response (200 OK)**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "role": "USER",
  "status": "ACTIVE",
  "createdAt": "2026-01-31T10:00:00",
  "updatedAt": "2026-01-31T10:00:00"
}
```

---

## 공휴일 관리 API

### 1. 공휴일 동기화 (특정 월)
```http
POST /api/holidays/sync?year=2026&month=1
```

**Response (200 OK)**
```json
{
  "success": true,
  "message": "공휴일 동기화가 완료되었습니다.",
  "year": 2026,
  "month": 1,
  "syncedCount": 3
}
```

---

### 2. 공휴일 동기화 (전체 연도)
```http
POST /api/holidays/sync-year?year=2026
```

**Response (200 OK)**
```json
{
  "success": true,
  "message": "2026년 전체 공휴일 동기화가 완료되었습니다.",
  "year": 2026,
  "totalSyncedCount": 15
}
```

---

### 3. 특정 날짜 공휴일 확인
```http
GET /api/holidays/check?date=2026-01-01
```

**Response (200 OK)**
```json
{
  "date": "2026-01-01",
  "isHoliday": true,
  "isBookable": false,
  "holidayName": "신정",
  "description": "1월 1일"
}
```

**Response (공휴일 아닌 경우)**
```json
{
  "date": "2026-01-02",
  "isHoliday": false,
  "isBookable": true
}
```

---

### 4. 특정 월 공휴일 목록
```http
GET /api/holidays?year=2026&month=1
```

**Response (200 OK)**
```json
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
    "description": "설날 당일"
  }
]
```

---

### 5. 날짜 범위 공휴일 목록
```http
GET /api/holidays/range?startDate=2026-01-01&endDate=2026-12-31
```

**Response (200 OK)**
```json
[
  {
    "id": 1,
    "holidayDate": "2026-01-01",
    "holidayName": "신정",
    "description": "1월 1일",
    "isHoliday": true
  },
  // ... more holidays
]
```

---

### 6. 수동 공휴일 추가 (테스트용)
```http
POST /api/holidays/manual?date=2026-03-01&name=삼일절&description=3.1절
```

**Response (200 OK)**
```json
{
  "success": true,
  "message": "공휴일이 수동으로 추가되었습니다.",
  "holiday": {
    "id": 10,
    "holidayDate": "2026-03-01",
    "holidayName": "삼일절",
    "description": "3.1절",
    "isHoliday": true
  }
}
```

---

## 숙소 관리 API

### 1. 숙소 검색 및 목록 조회
```http
GET /api/accommodations?keyword=호텔&city=서울&type=HOTEL&minRating=4&page=0&size=10
```

**Query Parameters**
- keyword (선택): 검색 키워드 (숙소명, 설명에서 검색)
- city (선택): 도시
- region (선택): 지역
- type (선택): 숙소 타입 (HOTEL, MOTEL, PENSION, RESORT, GUESTHOUSE)
- minRating (선택): 최소 평점
- page (기본값: 0): 페이지 번호
- size (기본값: 10): 페이지 크기

**Response (200 OK)**
```json
{
  "content": [
    {
      "id": 1,
      "name": "그랜드 호텔",
      "type": "HOTEL",
      "description": "도심 속 프리미엄 비즈니스 호텔",
      "address": "서울특별시 강남구 테헤란로 123",
      "city": "서울",
      "region": "강남구",
      "latitude": 37.5012,
      "longitude": 127.0396,
      "phone": "02-1234-5678",
      "mainImage": "/images/hotel1.jpg",
      "status": "ACTIVE",
      "rating": 4,
      "createdAt": "2026-01-31T10:00:00",
      "updatedAt": "2026-01-31T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    }
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### 2. 숙소 상세 조회
```http
GET /api/accommodations/{id}
```

**Response (200 OK)**
```json
{
  "id": 1,
  "name": "그랜드 호텔",
  "type": "HOTEL",
  "description": "도심 속 프리미엄 비즈니스 호텔",
  "address": "서울특별시 강남구 테헤란로 123",
  "city": "서울",
  "region": "강남구",
  "latitude": 37.5012,
  "longitude": 127.0396,
  "phone": "02-1234-5678",
  "mainImage": "/images/hotel1.jpg",
  "status": "ACTIVE",
  "rating": 4,
  "images": [
    {
      "id": 1,
      "imageUrl": "/images/hotel1_img1.jpg",
      "displayOrder": 1
    },
    {
      "id": 2,
      "imageUrl": "/images/hotel1_img2.jpg",
      "displayOrder": 2
    }
  ],
  "rooms": [
    {
      "id": 1,
      "name": "스탠다드 더블룸",
      "type": "DOUBLE",
      "capacity": 2,
      "maxCapacity": 2,
      "pricePerNight": 100000.00,
      "description": "편안한 더블 침대가 있는 스탠다드 룸",
      "size": 33,
      "mainImage": "/images/room1.jpg",
      "status": "AVAILABLE"
    }
  ],
  "createdAt": "2026-01-31T10:00:00",
  "updatedAt": "2026-01-31T10:00:00"
}
```

---

### 3. 숙소별 객실 목록 조회
```http
GET /api/accommodations/{id}/rooms?page=0&size=10
```

**Response (200 OK)**
```json
{
  "content": [
    {
      "id": 1,
      "accommodationId": 1,
      "accommodationName": "그랜드 호텔",
      "name": "스탠다드 더블룸",
      "type": "DOUBLE",
      "capacity": 2,
      "maxCapacity": 2,
      "pricePerNight": 100000.00,
      "description": "편안한 더블 침대가 있는 스탠다드 룸",
      "size": 33,
      "mainImage": "/images/room1.jpg",
      "status": "AVAILABLE"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 1,
  "totalElements": 1
}
```

---

### 4. 숙소 등록
```http
POST /api/accommodations
```

**Request Body**
```json
{
  "name": "그랜드 호텔",
  "type": "HOTEL",
  "description": "도심 속 프리미엄 비즈니스 호텔",
  "address": "서울특별시 강남구 테헤란로 123",
  "city": "서울",
  "region": "강남구",
  "latitude": 37.5012,
  "longitude": 127.0396,
  "phone": "02-1234-5678",
  "mainImage": "/images/hotel1.jpg"
}
```

**Response (201 Created)**
```json
{
  "id": 1,
  "name": "그랜드 호텔",
  "type": "HOTEL",
  "description": "도심 속 프리미엄 비즈니스 호텔",
  "address": "서울특별시 강남구 테헤란로 123",
  "city": "서울",
  "region": "강남구",
  "phone": "02-1234-5678",
  "status": "ACTIVE",
  "rating": 0
}
```

**숙소 타입 (type)**
- `HOTEL` - 호텔
- `MOTEL` - 모텔
- `PENSION` - 펜션
- `RESORT` - 리조트
- `GUESTHOUSE` - 게스트하우스

---

## 객실 관리 API

### 1. 전체 객실 목록 조회
```http
GET /api/rooms
```

**Response (200 OK)**
```json
[
  {
    "id": 1,
    "accommodationId": 1,
    "accommodationName": "그랜드 호텔",
    "name": "스탠다드 더블룸",
    "type": "DOUBLE",
    "capacity": 2,
    "maxCapacity": 2,
    "pricePerNight": 100000.00,
    "description": "편안한 더블 침대가 있는 스탠다드 룸",
    "size": 33,
    "mainImage": "/images/room1.jpg",
    "status": "AVAILABLE"
  }
]
```

---

### 2. 객실 상세 조회
```http
GET /api/rooms/{id}
```

**Response (200 OK)**
```json
{
  "id": 1,
  "accommodationId": 1,
  "accommodationName": "그랜드 호텔",
  "name": "스탠다드 더블룸",
  "type": "DOUBLE",
  "capacity": 2,
  "maxCapacity": 2,
  "pricePerNight": 100000.00,
  "description": "편안한 더블 침대가 있는 스탠다드 룸",
  "size": 33,
  "mainImage": "/images/room1.jpg",
  "status": "AVAILABLE",
  "amenities": ["Wi-Fi", "TV", "에어컨", "냉장고"],
  "images": [
    {
      "id": 1,
      "imageUrl": "/images/room1_img1.jpg",
      "displayOrder": 1
    },
    {
      "id": 2,
      "imageUrl": "/images/room1_img2.jpg",
      "displayOrder": 2
    }
  ],
  "createdAt": "2026-01-31T10:00:00",
  "updatedAt": "2026-01-31T10:00:00"
}
```

---

### 3. 객실 등록
```http
POST /api/rooms
```

**Request Body**
```json
{
  "accommodationId": 1,
  "name": "스탠다드 더블룸",
  "type": "DOUBLE",
  "capacity": 2,
  "maxCapacity": 2,
  "pricePerNight": 100000,
  "description": "편안한 더블 침대가 있는 스탠다드 룸",
  "size": 33,
  "mainImage": "/images/room1.jpg",
  "amenities": ["Wi-Fi", "TV", "에어컨", "냉장고"]
}
```

**Response (201 Created)**
```json
{
  "id": 1,
  "accommodationId": 1,
  "accommodationName": "그랜드 호텔",
  "name": "스탠다드 더블룸",
  "type": "DOUBLE",
  "capacity": 2,
  "maxCapacity": 2,
  "pricePerNight": 100000.00,
  "description": "편안한 더블 침대가 있는 스탠다드 룸",
  "size": 33,
  "mainImage": "/images/room1.jpg",
  "status": "AVAILABLE"
}
```

**객실 타입 (type)**
- `STANDARD` - 스탠다드
- `DELUXE` - 디럭스
- `SUITE` - 스위트
- `ROYAL_SUITE` - 로열 스위트
- `FAMILY` - 패밀리룸
- `TWIN` - 트윈
- `DOUBLE` - 더블
- `ONDOL` - 온돌

---

## 예약 관리 API

### 1. 예약 생성
```http
POST /api/reservations
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "roomId": 1,
  "checkInDate": "2026-02-10",
  "checkOutDate": "2026-02-12",
  "guestCount": 2,
  "specialRequests": "금연실 부탁드립니다"
}
```

**Response (201 Created)**
```json
{
  "id": 1,
  "roomId": 1,
  "roomName": "스탠다드 더블룸",
  "accommodationId": 1,
  "accommodationName": "그랜드 호텔",
  "memberId": 1,
  "memberName": "홍길동",
  "checkInDate": "2026-02-10",
  "checkOutDate": "2026-02-12",
  "guestCount": 2,
  "totalPrice": 200000.00,
  "status": "PENDING",
  "specialRequests": "금연실 부탁드립니다",
  "createdAt": "2026-01-31T10:00:00",
  "updatedAt": "2026-01-31T10:00:00"
}
```

**Validation**
- roomId: 필수
- checkInDate: 필수, 오늘 이후 날짜
- checkOutDate: 필수, checkInDate 이후 날짜
- guestCount: 필수, 1명 이상
- specialRequests: 선택

**비즈니스 규칙**
- 공휴일에는 예약 불가
- 중복 예약 불가 (비관적 락 적용)
- 최대 수용 인원 초과 불가

---

### 2. 내 예약 목록 조회
```http
GET /api/reservations/my
Authorization: Bearer {accessToken}
```

**Response (200 OK)**
```json
[
  {
    "id": 1,
    "roomId": 1,
    "roomName": "스탠다드 더블룸",
    "accommodationId": 1,
    "accommodationName": "그랜드 호텔",
    "memberId": 1,
    "memberName": "홍길동",
    "checkInDate": "2026-02-10",
    "checkOutDate": "2026-02-12",
    "guestCount": 2,
    "totalPrice": 200000.00,
    "status": "PENDING",
    "specialRequests": "금연실 부탁드립니다",
    "createdAt": "2026-01-31T10:00:00",
    "updatedAt": "2026-01-31T10:00:00"
  }
]
```

---

### 3. 예약 상세 조회
```http
GET /api/reservations/{reservationId}
Authorization: Bearer {accessToken}
```

**Response (200 OK)**
```json
{
  "id": 1,
  "roomId": 1,
  "roomName": "스탠다드 더블룸",
  "accommodationId": 1,
  "accommodationName": "그랜드 호텔",
  "memberId": 1,
  "memberName": "홍길동",
  "checkInDate": "2026-02-10",
  "checkOutDate": "2026-02-12",
  "guestCount": 2,
  "totalPrice": 200000.00,
  "status": "PENDING",
  "specialRequests": "금연실 부탁드립니다",
  "createdAt": "2026-01-31T10:00:00",
  "updatedAt": "2026-01-31T10:00:00"
}
```

---

### 4. 예약 취소
```http
POST /api/reservations/cancel/{reservationId}
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "cancelReason": "일정 변경으로 인한 취소"
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "message": "예약이 취소되었습니다.",
  "reservationId": "1",
  "cancelReason": "일정 변경으로 인한 취소",
  "roomName": "스탠다드 더블룸"
}
```

**비즈니스 규칙**
- 본인 예약만 취소 가능
- 이미 취소된 예약은 취소 불가
- 이미 완료된 예약은 취소 불가

---

### 5. 객실 가용성 확인
```http
GET /api/reservations/check-availability?accommodationId=1&checkInDate=2026-02-10&checkOutDate=2026-02-12&guestCount=2
```

**Query Parameters**
- accommodationId (선택): 특정 숙소의 객실만 조회
- checkInDate (필수): 체크인 날짜
- checkOutDate (필수): 체크아웃 날짜
- guestCount (선택): 투숙 인원

**Response (200 OK)**
```json
[
  {
    "roomId": 1,
    "roomName": "스탠다드 더블룸",
    "roomType": "DOUBLE",
    "availableCount": 1,
    "pricePerNight": 100000.00,
    "maxOccupancy": 2,
    "available": true,
    "amenities": ["Wi-Fi", "TV", "에어컨", "냉장고"],
    "totalPrice": 200000.00,
    "nights": 2
  },
  {
    "roomId": 2,
    "roomName": "디럭스 트윈룸",
    "roomType": "TWIN",
    "availableCount": 0,
    "pricePerNight": 150000.00,
    "maxOccupancy": 3,
    "available": false,
    "amenities": ["Wi-Fi", "TV", "에어컨", "냉장고", "미니바"],
    "totalPrice": 300000.00,
    "nights": 2
  }
]
```

---

## 헬스 체크 API

### 1. 서버 상태 확인
```http
GET /api/health
```

**Response (200 OK)**
```json
{
  "status": "UP",
  "timestamp": "2026-01-31T10:00:00"
}
```

---

## 공통 응답 형식

### 성공 응답
```json
{
  "data": { /* 실제 데이터 */ },
  "message": "성공 메시지",
  "timestamp": "2026-01-31T10:00:00"
}
```

### 에러 응답
```json
{
  "timestamp": "2026-01-31T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "에러 메시지",
  "path": "/api/reservations"
}
```

### Validation 에러 응답
```json
{
  "timestamp": "2026-01-31T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "입력값 검증에 실패했습니다",
  "validationErrors": {
    "email": "이메일 형식이 올바르지 않습니다",
    "password": "비밀번호는 최소 8자 이상이어야 합니다"
  }
}
```

---

## 에러 코드

| HTTP 상태 코드 | 설명 | 예시 |
|---------------|------|------|
| 200 OK | 요청 성공 | 데이터 조회 성공 |
| 201 Created | 리소스 생성 성공 | 회원가입, 예약 생성 |
| 204 No Content | 요청 성공, 응답 본문 없음 | 로그아웃 |
| 400 Bad Request | 잘못된 요청 | Validation 실패, 비즈니스 규칙 위반 |
| 401 Unauthorized | 인증 실패 | 토큰 없음, 토큰 만료 |
| 403 Forbidden | 권한 없음 | 관리자 권한 필요 |
| 404 Not Found | 리소스를 찾을 수 없음 | 존재하지 않는 ID |
| 409 Conflict | 충돌 | 이미 존재하는 이메일 |
| 500 Internal Server Error | 서버 오류 | 예상치 못한 오류 |

---

## 예약 상태 (Reservation Status)

| 상태 | 설명 |
|------|------|
| PENDING | 예약 대기 (결제 전) |
| CONFIRMED | 예약 확정 (결제 완료) |
| CANCELLED | 예약 취소 |
| COMPLETED | 예약 완료 (체크아웃 완료) |

---

## 숙소 상태 (Accommodation Status)

| 상태 | 설명 |
|------|------|
| ACTIVE | 활성 (예약 가능) |
| INACTIVE | 비활성 (일시 중지) |
| CLOSED | 폐업 |

---

## 객실 상태 (Room Status)

| 상태 | 설명 |
|------|------|
| AVAILABLE | 예약 가능 |
| UNAVAILABLE | 예약 불가 |
| MAINTENANCE | 유지보수 중 |

---

## 회원 권한 (Member Role)

| 권한 | 설명 |
|------|------|
| USER | 일반 사용자 |
| ADMIN | 관리자 |

---

## 회원 상태 (Member Status)

| 상태 | 설명 |
|------|------|
| ACTIVE | 활성 |
| INACTIVE | 비활성 |
| DELETED | 탈퇴 |

---

## API 호출 예시 (cURL)

### 회원가입
```bash
curl -X POST http://localhost:8080/api/members/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123!",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678"
  }'
```

### 로그인
```bash
curl -X POST http://localhost:8080/api/members/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123!"
  }'
```

### 예약 생성 (인증 필요)
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "roomId": 1,
    "checkInDate": "2026-02-10",
    "checkOutDate": "2026-02-12",
    "guestCount": 2,
    "specialRequests": "금연실 부탁드립니다"
  }'
```

### 공휴일 확인
```bash
curl -X GET "http://localhost:8080/api/holidays/check?date=2026-01-01"
```

---

## 페이징 응답 구조

모든 페이징 API는 Spring Data JPA의 Page 객체를 반환합니다.

```json
{
  "content": [ /* 실제 데이터 배열 */ ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 50,
  "last": false,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": false,
    "unsorted": true,
    "empty": true
  },
  "first": true,
  "numberOfElements": 10,
  "empty": false
}
```

**주요 필드**
- `content`: 실제 데이터 배열
- `totalPages`: 전체 페이지 수
- `totalElements`: 전체 데이터 개수
- `number`: 현재 페이지 번호 (0부터 시작)
- `size`: 페이지 크기
- `first`: 첫 페이지 여부
- `last`: 마지막 페이지 여부

---

## 참고사항

### 날짜 형식
- 모든 날짜는 ISO 8601 형식을 따릅니다
- 날짜: `YYYY-MM-DD` (예: 2026-01-31)
- 날짜+시간: `YYYY-MM-DDTHH:mm:ss` (예: 2026-01-31T10:00:00)

### 금액 형식
- 모든 금액은 원화(KRW) 단위입니다
- BigDecimal 타입으로 처리되어 소수점 2자리까지 표시됩니다

### 타임존
- 모든 시간은 Asia/Seoul (한국 시간) 기준입니다

---

**문서 버전**: 1.0  
**최종 업데이트**: 2026-01-31  
**작성자**: 유희재

