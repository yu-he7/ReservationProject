# 데이터베이스 ERD (Entity Relationship Diagram)

## 목차
1. [전체 ERD 다이어그램](#전체-erd-다이어그램)
2. [테이블 상세 분석](#테이블-상세-분석)
3. [관계 분석](#관계-분석)
4. [인덱스 전략](#인덱스-전략)
5. [데이터 무결성](#데이터-무결성)

## 전체 ERD 다이어그램

### DBML 다이어그램
```dbml
// Use DBML to define your database structure
// Docs: https://dbml.dbdiagram.io/docs

Table members {
  id bigint [primary key, increment]
  email varchar(50) [unique, not null, note: '이메일(유니크)']
  password varchar(255) [not null, note: '암호화된 비밀번호']
  name varchar(50) [not null, note: '이름']
  phone varchar(20) [note: '전화번호']
  role varchar(20) [not null, note: 'USER, ADMIN']
  status varchar(20) [not null, note: 'ACTIVE, INACTIVE, DELETED']
  created_at datetime [not null, default: `CURRENT_TIMESTAMP`]
  updated_at datetime [default: `CURRENT_TIMESTAMP`]
  
  indexes {
    email
    status
  }
}

Table accommodations {
  id bigint [primary key, increment]
  name varchar(100) [not null, note: '숙소 이름']
  type varchar(20) [not null, note: 'HOTEL, MOTEL, PENSION, RESORT, GUESTHOUSE']
  description varchar(500) [not null, note: '설명']
  address varchar(200) [not null, note: '주소']
  city varchar(100) [not null, note: '도시']
  region varchar(50) [not null, note: '지역']
  latitude decimal(10,7) [note: '위도']
  longitude decimal(10,7) [note: '경도']
  phone varchar(20) [note: '전화번호']
  main_image varchar(200) [note: '대표 이미지']
  status varchar(20) [not null, note: 'ACTIVE, INACTIVE, CLOSED']
  rating int [not null, note: '평점']
  created_at datetime [not null, default: `CURRENT_TIMESTAMP`]
  updated_at datetime [default: `CURRENT_TIMESTAMP`]
  
  indexes {
    city
    region
    type
    rating
    status
  }
}

Table rooms {
  id bigint [primary key, increment]
  accommodation_id bigint [not null, ref: > accommodations.id, note: '숙소 ID (FK)']
  name varchar(100) [not null, note: '객실 이름']
  type varchar(20) [not null, note: 'STANDARD, DELUXE, SUITE, FAMILY, TWIN, DOUBLE, ONDOL']
  capacity int [not null, note: '기본 수용 인원']
  max_capacity int [not null, note: '최대 수용 인원']
  price_per_night decimal(10,2) [not null, note: '1박 가격']
  description varchar(500) [note: '설명']
  size int [not null, note: '객실 크기']
  main_image varchar(200) [note: '대표 이미지']
  status varchar(20) [not null, note: 'AVAILABLE, UNAVAILABLE']
  amenities varchar(500) [note: '어메니티 JSON']
  created_at datetime [not null, default: `CURRENT_TIMESTAMP`]
  updated_at datetime [default: `CURRENT_TIMESTAMP`]
  
  indexes {
    accommodation_id
    type
    status
    price_per_night
  }
}

Table accommodation_images {
  id bigint [primary key, increment]
  accommodation_id bigint [not null, ref: > accommodations.id, note: '숙소 ID (FK)']
  image_url varchar(200) [not null, note: '이미지 URL']
  display_order int [not null, note: '표시 순서']
  created_at datetime [not null, default: `CURRENT_TIMESTAMP`]
  
  indexes {
    accommodation_id
    display_order
  }
}

Table room_images {
  id bigint [primary key, increment]
  room_id bigint [not null, ref: > rooms.id, note: '객실 ID (FK)']
  image_url varchar(200) [not null, note: '이미지 URL']
  display_order int [not null, note: '표시 순서']
  created_at datetime [not null, default: `CURRENT_TIMESTAMP`]
  
  indexes {
    room_id
    display_order
  }
}

Table reservations {
  id bigint [primary key, increment]
  member_id bigint [not null, ref: > members.id, note: '회원 ID (FK)']
  room_id bigint [not null, ref: > rooms.id, note: '객실 ID (FK)']
  check_in_date date [not null, note: '체크인 날짜']
  check_out_date date [not null, note: '체크아웃 날짜']
  guest_count int [not null, note: '투숙 인원']
  total_price decimal(10,2) [not null, note: '총 금액']
  status varchar(20) [not null, note: 'PENDING, CONFIRMED, CANCELLED']
  special_requests varchar(500) [note: '특별 요청사항']
  cancel_reason varchar(500) [note: '취소 사유']
  cancelled_at datetime [note: '취소 일시']
  created_at datetime [not null, default: `CURRENT_TIMESTAMP`]
  updated_at datetime [default: `CURRENT_TIMESTAMP`]
  
  indexes {
    member_id
    room_id
    check_in_date
    status
  }
}

Table holidays {
  id bigint [primary key, increment]
  holiday_date date [unique, not null, note: '공휴일 날짜(유니크)']
  holiday_name varchar(100) [not null, note: '공휴일 이름']
  description varchar(200) [note: '설명']
  is_holiday boolean [not null, note: '공휴일 여부']
  created_at datetime [not null, default: `CURRENT_TIMESTAMP`]
  updated_at datetime [default: `CURRENT_TIMESTAMP`]
  
  indexes {
    holiday_date
  }
}
```

## 테이블 상세 분석

### 1. members (회원)
```sql
CREATE TABLE members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '회원 ID',
    email VARCHAR(50) NOT NULL UNIQUE COMMENT '이메일',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호 (BCrypt 암호화)',
    name VARCHAR(50) NOT NULL COMMENT '이름',
    phone VARCHAR(20) COMMENT '전화번호',
    role VARCHAR(20) NOT NULL COMMENT '권한 (USER, ADMIN)',
    status VARCHAR(20) NOT NULL COMMENT '상태 (ACTIVE, INACTIVE, DELETED)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 정보';
```

**주요 특징:**
- email UNIQUE: 중복 가입 방지
- password: BCrypt 암호화 (60자)
- role: Enum (USER, ADMIN)
- status: Enum (ACTIVE, INACTIVE, DELETED)
- Audit: created_at, updated_at 자동 관리

**샘플 데이터:**
| id | email | name | role | status |
|----|-------|------|------|--------|
| 1 | user@example.com | 홍길동 | USER | ACTIVE |
| 2 | admin@example.com | 관리자 | ADMIN | ACTIVE |

### 2. accommodations (숙소)
```sql
CREATE TABLE accommodations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '숙소 ID',
    name VARCHAR(100) NOT NULL COMMENT '숙소 이름',
    type VARCHAR(20) NOT NULL COMMENT '숙소 타입',
    description VARCHAR(500) NOT NULL COMMENT '숙소 설명',
    address VARCHAR(200) NOT NULL COMMENT '주소',
    city VARCHAR(100) NOT NULL COMMENT '도시',
    region VARCHAR(50) NOT NULL COMMENT '지역',
    latitude DECIMAL(10, 7) COMMENT '위도',
    longitude DECIMAL(10, 7) COMMENT '경도',
    phone VARCHAR(20) COMMENT '전화번호',
    main_image VARCHAR(200) COMMENT '대표 이미지 URL',
    status VARCHAR(20) NOT NULL COMMENT '상태',
    rating INT NOT NULL COMMENT '평점',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_city (city),
    INDEX idx_region (region),
    INDEX idx_type (type),
    INDEX idx_rating (rating),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='숙소 정보';
```

**주요 특징:**
- type: HOTEL, MOTEL, PENSION, RESORT, GUESTHOUSE
- 위치 정보: latitude, longitude (지도 연동 가능)
- 검색 최적화: city, region, type, rating에 인덱스
- status: ACTIVE, INACTIVE, CLOSED

**샘플 데이터:**
| id | name | type | city | region | rating |
|----|------|------|------|--------|--------|
| 1 | 그랜드 호텔 | HOTEL | 서울 | 강남구 | 5 |
| 2 | 제주 리조트 | RESORT | 제주 | 서귀포시 | 4 |

### 3. rooms (객실)
```sql
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '객실 ID',
    accommodation_id BIGINT NOT NULL COMMENT '숙소 ID (FK)',
    name VARCHAR(100) NOT NULL COMMENT '객실 이름',
    type VARCHAR(20) NOT NULL COMMENT '객실 타입',
    capacity INT NOT NULL COMMENT '기본 수용 인원',
    max_capacity INT NOT NULL COMMENT '최대 수용 인원',
    price_per_night DECIMAL(10, 2) NOT NULL COMMENT '1박 가격',
    description VARCHAR(500) COMMENT '객실 설명',
    size INT NOT NULL COMMENT '객실 크기 (㎡)',
    main_image VARCHAR(200) COMMENT '대표 이미지 URL',
    status VARCHAR(20) NOT NULL COMMENT '상태',
    amenities VARCHAR(500) COMMENT '어메니티 (JSON)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(id) ON DELETE CASCADE,
    INDEX idx_accommodation_id (accommodation_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_price (price_per_night)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='객실 정보';
```

**주요 특징:**
- FK CASCADE: 숙소 삭제 시 객실도 함께 삭제
- type: STANDARD, DELUXE, SUITE, FAMILY, TWIN, DOUBLE, ONDOL
- 가격: DECIMAL(10,2)로 정확한 금액 관리
- amenities: JSON 형태로 다양한 편의시설 저장
- 비관적 락: 예약 시 SELECT FOR UPDATE 사용

**샘플 데이터:**
| id | accommodation_id | name | type | price_per_night | max_capacity |
|----|-----------------|------|------|----------------|--------------|
| 1 | 1 | 스탠다드 더블룸 | DOUBLE | 100000.00 | 2 |
| 2 | 1 | 디럭스 스위트 | SUITE | 250000.00 | 4 |

### 4. accommodation_images (숙소 이미지)
```sql
CREATE TABLE accommodation_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이미지 ID',
    accommodation_id BIGINT NOT NULL COMMENT '숙소 ID (FK)',
    image_url VARCHAR(200) NOT NULL COMMENT '이미지 URL',
    display_order INT NOT NULL COMMENT '표시 순서',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(id) ON DELETE CASCADE,
    INDEX idx_accommodation_id (accommodation_id),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='숙소 이미지';
```

**주요 특징:**
- 1:N 관계: 숙소당 여러 이미지 가능
- display_order: 이미지 표시 순서 관리
- CASCADE: 숙소 삭제 시 이미지도 함께 삭제

### 5. room_images (객실 이미지)
```sql
CREATE TABLE room_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이미지 ID',
    room_id BIGINT NOT NULL COMMENT '객실 ID (FK)',
    image_url VARCHAR(200) NOT NULL COMMENT '이미지 URL',
    display_order INT NOT NULL COMMENT '표시 순서',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    INDEX idx_room_id (room_id),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='객실 이미지';
```

**주요 특징:**
- 1:N 관계: 객실당 여러 이미지 가능
- display_order: 이미지 표시 순서 관리

### 6. reservations (예약)
```sql
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '예약 ID',
    member_id BIGINT NOT NULL COMMENT '회원 ID (FK)',
    room_id BIGINT NOT NULL COMMENT '객실 ID (FK)',
    check_in_date DATE NOT NULL COMMENT '체크인 날짜',
    check_out_date DATE NOT NULL COMMENT '체크아웃 날짜',
    guest_count INT NOT NULL COMMENT '투숙 인원',
    total_price DECIMAL(10, 2) NOT NULL COMMENT '총 금액',
    status VARCHAR(20) NOT NULL COMMENT '예약 상태',
    special_requests VARCHAR(500) COMMENT '특별 요청사항',
    cancel_reason VARCHAR(500) COMMENT '취소 사유',
    cancelled_at DATETIME COMMENT '취소 일시',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    INDEX idx_member_id (member_id),
    INDEX idx_room_id (room_id),
    INDEX idx_check_in_date (check_in_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='예약 정보';
```

**주요 특징:**
- status: PENDING, CONFIRMED, CANCELLED, COMPLETED
- total_price: 자동 계산 (price_per_night × 숙박일수)
- 취소 정보: cancel_reason, cancelled_at 저장
- 비관적 락: 중복 예약 방지를 위해 SELECT FOR UPDATE 사용
- 검색 최적화: member_id, room_id, check_in_date에 인덱스

**샘플 데이터:**
| id | member_id | room_id | check_in | check_out | total_price | status |
|----|-----------|---------|----------|-----------|-------------|--------|
| 1 | 1 | 1 | 2026-02-10 | 2026-02-12 | 200000.00 | CONFIRMED |
| 2 | 1 | 2 | 2026-03-15 | 2026-03-17 | 500000.00 | PENDING |

### 7. holidays (공휴일)
```sql
CREATE TABLE holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '공휴일 ID',
    holiday_date DATE NOT NULL UNIQUE COMMENT '공휴일 날짜',
    holiday_name VARCHAR(100) NOT NULL COMMENT '공휴일 이름',
    description VARCHAR(200) COMMENT '설명',
    is_holiday BOOLEAN NOT NULL COMMENT '공휴일 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_holiday_date (holiday_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='공휴일 정보';
```

**주요 특징:**
- holiday_date UNIQUE: 중복 방지
- Open API 연동: 공공데이터포털에서 자동 동기화
- 예약 차단: 공휴일에는 예약 불가
- 스케줄러: 애플리케이션 시작 시 + 정기적으로 동기화

**샘플 데이터:**
| id | holiday_date | holiday_name | is_holiday |
|----|--------------|--------------|------------|
| 1 | 2026-01-01 | 신정 | true |
| 2 | 2026-01-29 | 설날 | true |
| 3 | 2026-03-01 | 삼일절 | true |

## 관계 분석

### 1:N 관계
```
accommodations (1) ──→ (N) rooms
accommodations (1) ──→ (N) accommodation_images
rooms (1) ──→ (N) room_images
members (1) ──→ (N) reservations
rooms (1) ──→ (N) reservations
```

### 관계 설명

#### 1. accommodations ↔ rooms (1:N)
- 관계: 숙소는 여러 객실을 가질 수 있음
- FK: rooms.accommodation_id → accommodations.id
- CASCADE: 숙소 삭제 시 모든 객실도 삭제
- 비즈니스 규칙: 
  - 숙소는 최소 1개의 객실 필요
  - 객실은 반드시 숙소에 소속

#### 2. accommodations ↔ accommodation_images (1:N)
- 관계: 숙소는 여러 이미지를 가질 수 있음
- FK: accommodation_images.accommodation_id → accommodations.id
- CASCADE: 숙소 삭제 시 모든 이미지도 삭제
- 정렬: display_order로 이미지 순서 관리

#### 3. rooms ↔ room_images (1:N)
- 관계: 객실은 여러 이미지를 가질 수 있음
- FK: room_images.room_id → rooms.id
- CASCADE: 객실 삭제 시 모든 이미지도 삭제

#### 4. members ↔ reservations (1:N)
- 관계: 회원은 여러 예약을 할 수 있음
- FK: reservations.member_id → members.id
- NO CASCADE: 회원 삭제 시 예약 기록은 유지 (데이터 보존)

#### 5. rooms ↔ reservations (1:N)
- 관계: 객실은 여러 예약을 받을 수 있음
- FK: reservations.room_id → rooms.id
- NO CASCADE: 객실 삭제 시 예약 기록은 유지

## 인덱스 전략

### 1. 기본키 (Primary Key)
```sql
-- 모든 테이블의 id 컬럼에 자동 생성
-- 클러스터드 인덱스 (데이터 물리적 정렬)
PRIMARY KEY (id)
```

### 2. 유니크 인덱스 (Unique Index)
```sql
-- 중복 방지 + 빠른 검색
UNIQUE (email)           -- members.email
UNIQUE (holiday_date)    -- holidays.holiday_date
```

### 3. 외래키 인덱스 (Foreign Key Index)
```sql
-- JOIN 성능 최적화
INDEX idx_accommodation_id (accommodation_id)  -- rooms
INDEX idx_room_id (room_id)                    -- room_images, reservations
INDEX idx_member_id (member_id)                -- reservations
```

### 4. 검색 최적화 인덱스
```sql
-- 자주 검색되는 컬럼
INDEX idx_city (city)                -- accommodations (도시별 검색)
INDEX idx_region (region)            -- accommodations (지역별 검색)
INDEX idx_type (type)                -- accommodations, rooms (타입별 검색)
INDEX idx_rating (rating)            -- accommodations (평점순 정렬)
INDEX idx_status (status)            -- accommodations, rooms (상태 필터)
INDEX idx_check_in_date (check_in_date)  -- reservations (날짜 검색)
```

### 5. 복합 인덱스 (추가 가능)
```sql
-- 자주 함께 사용되는 조건
CREATE INDEX idx_room_availability 
ON reservations(room_id, check_in_date, check_out_date, status);

-- 숙소별 객실 목록 조회
CREATE INDEX idx_accommodation_room 
ON rooms(accommodation_id, status);
```

## 데이터 무결성

### 1. 외래키 제약조건 (Foreign Key Constraint)
```sql
-- 참조 무결성 보장
FOREIGN KEY (accommodation_id) REFERENCES accommodations(id) ON DELETE CASCADE
FOREIGN KEY (room_id) REFERENCES rooms(id)
FOREIGN KEY (member_id) REFERENCES members(id)
```

### 2. NOT NULL 제약조건
```sql
-- 필수 입력 필드
name VARCHAR(100) NOT NULL
email VARCHAR(50) NOT NULL
price_per_night DECIMAL(10, 2) NOT NULL
```

### 3. UNIQUE 제약조건
```sql
-- 중복 방지
email VARCHAR(50) NOT NULL UNIQUE
holiday_date DATE NOT NULL UNIQUE
```

### 4. CHECK 제약조건 (애플리케이션 레벨)
```java
// Java Validation으로 구현
@Min(0)
private Integer capacity;

@Positive
private BigDecimal pricePerNight;

@FutureOrPresent
private LocalDate checkInDate;
```

### 5. 비관적 락 (Pessimistic Lock)
```java
// 중복 예약 방지
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM Room r WHERE r.id = :roomId")
Optional<Room> findByIdWithLock(@Param("roomId") Long roomId);
```


## 테이블 요약

| 테이블 | 역할 | 관계 | 주요 기능 |
|--------|------|------|-----------|
| members | 회원 관리 | - | 회원가입, 로그인, JWT 인증 |
| accommodations | 숙소 정보 | rooms (1:N) | 숙소 등록, 검색, 필터링 |
| rooms | 객실 정보 | accommodations (N:1), reservations (1:N) | 객실 등록, 가격 관리 |
| accommodation_images | 숙소 이미지 | accommodations (N:1) | 이미지 목록, 순서 관리 |
| room_images | 객실 이미지 | rooms (N:1) | 이미지 목록, 순서 관리 |
| reservations | 예약 정보 | members (N:1), rooms (N:1) | 예약 생성, 취소, 조회 |
| holidays | 공휴일 정보 | - | Open API 동기화, 예약 차단 |

## 성능 최적화 

### 1. 인덱스 활용
```sql
-- 좋은 예: 인덱스 활용
SELECT * FROM rooms WHERE accommodation_id = 1;  -- idx_accommodation_id 사용

-- 나쁜 예: 인덱스 미사용
SELECT * FROM rooms WHERE LOWER(name) LIKE '%호텔%';  -- 풀 스캔
```

### 2. JOIN 최적화
```sql
-- 좋은 예: 필요한 컬럼만 조회
SELECT r.id, r.name, a.name AS accommodation_name
FROM rooms r
INNER JOIN accommodations a ON r.accommodation_id = a.id
WHERE a.city = '서울';

-- 나쁜 예: SELECT *
SELECT *
FROM rooms r
INNER JOIN accommodations a ON r.accommodation_id = a.id;
```

### 3. 페이징 쿼리
```sql
-- 좋은 예: LIMIT + OFFSET
SELECT * FROM reservations
WHERE member_id = 1
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;
```

### 4. 비관적 락 사용
```java
-- 중복 예약 방지
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Room> findByIdWithLock(Long roomId);
```

**문서 버전**: 1.0  
**최종 업데이트**: 2026-02-01  
**작성자**: 유희재

