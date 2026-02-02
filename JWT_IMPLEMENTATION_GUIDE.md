# 🔐 JWT 인증 시스템 구현 가이드

## 목차
1. [JWT란?](#jwt란)
2. [시스템 아키텍처](#시스템-아키텍처)
3. [구현 구조](#구현-구조)
4. [JWT 토큰 생성 과정](#jwt-토큰-생성-과정)
5. [JWT 토큰 검증 과정](#jwt-토큰-검증-과정)
6. [Spring Security 통합](#spring-security-통합)
7. [인증 플로우](#인증-플로우)
8. [코드 상세 설명](#코드-상세-설명)
9. [보안 고려사항](#보안-고려사항)

---

## JWT란?

### JWT (JSON Web Token)
- 웹 표준 (RFC 7519)으로 정의된 JSON 기반의 토큰
- 사용자 인증 정보를 안전하게 전송하기 위한 토큰
- 서버의 세션 저장소 없이 인증을 처리할 수 있는 **무상태(Stateless)** 방식

### JWT 구조
```
Header.Payload.Signature
```

#### 1. Header (헤더)
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
- `alg`: 서명 알고리즘 (HS256, RS256 등)
- `typ`: 토큰 타입 (JWT)

#### 2. Payload (페이로드)
```json
{
  "memberId": 1,
  "email": "user@example.com",
  "role": "USER",
  "sub": "1",
  "iat": 1738368000,
  "exp": 1738371600
}
```
- `memberId`: 회원 ID (커스텀 클레임)
- `email`: 이메일 (커스텀 클레임)
- `role`: 권한 (커스텀 클레임)
- `sub`: Subject (표준 클레임)
- `iat`: Issued At - 발급 시간 (표준 클레임)
- `exp`: Expiration Time - 만료 시간 (표준 클레임)

#### 3. Signature (서명)
```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

### 실제 JWT 토큰 예시
```
eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6MSwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJzdWIiOiIxIiwiaWF0IjoxNzM4MzY4MDAwLCJleHAiOjE3MzgzNzE2MDB9.abc123def456ghi789
```

---

## 시스템 아키텍처

### 인증 흐름 개요
```
Client → Login → Server (JwtTokenProvider) → JWT Token 생성
↓
Client → API 요청 (Authorization: Bearer {token})
↓
Server (JwtAuthenticationFilter) → 토큰 검증 → SecurityContext 설정
↓
Controller → SecurityUtil → 사용자 정보 추출
```

### 토큰 종류

#### 1. Access Token (액세스 토큰)
- **유효기간**: 1시간 (3600초)
- **용도**: API 요청 시 인증
- **저장 위치**: 클라이언트 메모리 (보안상 LocalStorage 지양)

#### 2. Refresh Token (리프레시 토큰)
- **유효기간**: 7일 (604800초)
- **용도**: Access Token 재발급
- **저장 위치**: 클라이언트 보안 저장소 (HttpOnly Cookie 권장, 현재는 응답 본문)

---

## 구현 구조

### 프로젝트 구조
```
src/main/java/heej/net/
├── domain/
│   └── member/
│       ├── api/
│       │   ├── MemberEndpoint.java         # 회원 API 컨트롤러
│       │   └── dto/
│       │       ├── LoginRequest.java       # 로그인 요청 DTO
│       │       ├── LoginResponse.java      # 로그인 응답 DTO (토큰 포함)
│       │       └── RefreshTokenRequest.java
│       ├── usecase/
│       │   ├── MemberUseCase.java          # 회원 비즈니스 로직 인터페이스
│       │   └── MemberUseCaseImpl.java      # 회원 비즈니스 로직 구현
│       ├── model/
│       │   └── Member.java                 # 회원 엔티티
│       └── util/
│           ├── JwtTokenProvider.java       # JWT 토큰 생성/검증 (핵심)
│           └── RedisTokenManager.java      # Redis 토큰 관리 (선택)
├── security/
│   ├── JwtAuthenticationFilter.java        # JWT 인증 필터 (핵심)
│   └── util/
│       └── SecurityUtil.java               # 인증 정보 유틸리티
└── config/
    └── SecurityConfig.java                 # Spring Security 설정 (핵심)
```

---

## JWT 토큰 생성 과정

### 1. 의존성 설정 (build.gradle)
```gradle
dependencies {
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // Spring Security
    implementation 'org.springframework.boot:spring-boot-starter-security'
}
```

### 2. application.yml 설정
```yaml
jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation1234567890
  access-token-expiration: 3600000    # 1시간 (밀리초)
  refresh-token-expiration: 604800000  # 7일 (밀리초)
```

### 3. JwtTokenProvider 구현
```java
@Slf4j
@Component
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        // HS256 알고리즘을 위한 SecretKey 생성
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성
    public String generateAccessToken(Long memberId, String email, String role) {
        return generateToken(memberId, email, role, accessTokenExpiration);
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long memberId, String email, String role) {
        return generateToken(memberId, email, role, refreshTokenExpiration);
    }

    // 공통 토큰 생성 로직
    private String generateToken(Long memberId, String email, String role, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId);
        claims.put("email", email);
        claims.put("role", role);
        
        return Jwts.builder()
                .claims(claims)                    // 커스텀 클레임 추가
                .subject(String.valueOf(memberId)) // Subject에 memberId 저장
                .issuedAt(now)                     // 발급 시간
                .expiration(expiryDate)            // 만료 시간
                .signWith(secretKey)               // 서명
                .compact();                        // JWT 문자열 생성
    }
}
```

### 4. 로그인 시 토큰 발급 (MemberUseCaseImpl)
```java
@Override
@Transactional
public LoginResponse login(LoginRequest request) {
    // 1. 이메일로 회원 조회
    Member member = memberInfra.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

    // 2. 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
        throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    // 3. 회원 상태 확인
    if (member.getStatus() != MemberStatus.ACTIVE) {
        throw new IllegalArgumentException("비활성화된 계정입니다.");
    }

    // 4. JWT 토큰 생성
    String accessToken = jwtTokenProvider.generateAccessToken(
            member.getId(),
            member.getEmail(),
            member.getRole().name()
    );
    
    String refreshToken = jwtTokenProvider.generateRefreshToken(
            member.getId(),
            member.getEmail(),
            member.getRole().name()
    );

    // 5. 응답 반환
    return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .build();
}
```

---

## JWT 토큰 검증 과정

### 1. JwtTokenProvider 검증 메서드
```java
// 토큰 파싱 및 검증
public Claims parseToken(String token) {
    try {
        return Jwts.parser()
                .verifyWith(secretKey)         // 서명 검증
                .build()
                .parseSignedClaims(token)
                .getPayload();
    } catch (ExpiredJwtException e) {
        log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
        throw e;
    } catch (UnsupportedJwtException e) {
        log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        throw e;
    } catch (MalformedJwtException e) {
        log.warn("잘못된 JWT 토큰입니다: {}", e.getMessage());
        throw e;
    } catch (SecurityException e) {
        log.warn("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
        throw e;
    } catch (IllegalArgumentException e) {
        log.warn("JWT 토큰이 비어있습니다: {}", e.getMessage());
        throw e;
    }
}

// 토큰에서 회원 ID 추출
public Long getMemberId(String token) {
    Claims claims = parseToken(token);
    return claims.get("memberId", Long.class);
}

// 토큰에서 이메일 추출
public String getEmail(String token) {
    Claims claims = parseToken(token);
    return claims.get("email", String.class);
}

// 토큰에서 권한 추출
public String getRole(String token) {
    Claims claims = parseToken(token);
    return claims.get("role", String.class);
}

// 토큰 유효성 검증
public boolean validateToken(String token) {
    try {
        parseToken(token);
        return true;
    } catch (JwtException | IllegalArgumentException e) {
        return false;
    }
}
```

### 2. JwtAuthenticationFilter 구현
```java
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenManager redisTokenManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        try {
            // 1. Request Header에서 토큰 추출
            String token = extractToken(request);
            
            if (StringUtils.hasText(token)) {
                // 2. 블랙리스트 확인 (로그아웃된 토큰)
                if (redisTokenManager != null && redisTokenManager.isBlacklisted(token)) {
                    log.warn("블랙리스트에 등록된 토큰입니다");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // 3. 토큰 검증
                if (jwtTokenProvider.validateToken(token)) {
                    // 4. 토큰에서 사용자 정보 추출
                    Long memberId = jwtTokenProvider.getMemberId(token);
                    String email = jwtTokenProvider.getEmail(token);
                    String role = jwtTokenProvider.getRole(token);
                    
                    // 5. Spring Security Authentication 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    memberId,  // Principal (주체)
                                    null,      // Credentials (자격증명)
                                    Collections.singletonList(
                                        new SimpleGrantedAuthority(role)
                                    )          // Authorities (권한)
                            );
                    
                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // 6. SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("인증 성공: memberId={}, email={}, role={}", 
                             memberId, email, role);
                }
            }
        } catch (JwtException e) {
            log.error("JWT 처리 중 오류 발생: {}", e.getMessage());
        } catch (Exception e) {
            log.error("인증 필터 처리 중 오류 발생: {}", e.getMessage(), e);
        }
        
        // 7. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
    
    // Authorization 헤더에서 토큰 추출
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 제거
        }
        return null;
    }
}
```

---

## Spring Security 통합

### 1. SecurityConfig 설정
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용 시 불필요)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 세션 사용 안함 (무상태)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // URL별 인증 설정
            .authorizeHttpRequests(auth -> auth
                // 헬스 체크는 인증 불필요
                .requestMatchers("/api/health/**").permitAll()
                
                // 회원가입, 로그인, 토큰 갱신은 인증 불필요
                .requestMatchers("/api/members/signup", 
                               "/api/members/login", 
                               "/api/members/refresh").permitAll()
                
                // 공휴일 조회는 인증 불필요
                .requestMatchers("/api/holidays/**").permitAll()
                
                // 예약 API는 인증 필요
                .requestMatchers("/api/reservations/**").authenticated()
                
                // 회원 정보 API는 인증 필요
                .requestMatchers("/api/members/**").authenticated()
                
                // 나머지는 모두 허용 (숙소, 객실 조회)
                .anyRequest().permitAll())
            
            // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, null),
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 2. SecurityUtil 유틸리티
```java
public class SecurityUtil {

    // 현재 인증된 회원 ID 가져오기
    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext()
                                            .getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("유효하지 않은 사용자 ID입니다.");
        }
    }
    
    // 인증 정보 가져오기
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    // 인증 여부 확인
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext()
                                            .getAuthentication();
        return authentication != null 
            && authentication.isAuthenticated()
            && !(authentication.getPrincipal() instanceof String
                && "anonymousUser".equals(authentication.getPrincipal()));
    }
}
```

---

## 인증 플로우

### 전체 플로우 다이어그램
```
┌─────────┐                           ┌──────────────────┐
│ Client  │                           │  Spring Boot     │
└────┬────┘                           └────────┬─────────┘
     │                                         │
     │  1. POST /api/members/login             │
     │     { email, password }                 │
     ├────────────────────────────────────────>│
     │                                         │
     │                         2. MemberEndpoint
     │                                ↓
     │                         3. MemberUseCase
     │                                ↓
     │                         4. 비밀번호 검증
     │                                ↓
     │                         5. JwtTokenProvider
     │                            .generateAccessToken()
     │                            .generateRefreshToken()
     │                                ↓
     │  6. { accessToken, refreshToken }      │
     │<────────────────────────────────────────┤
     │                                         │
     │  7. GET /api/reservations/my            │
     │     Authorization: Bearer {accessToken} │
     ├────────────────────────────────────────>│
     │                                         │
     │                         8. JwtAuthenticationFilter
     │                                ↓
     │                         9. extractToken()
     │                                ↓
     │                         10. validateToken()
     │                                ↓
     │                         11. SecurityContext 설정
     │                                ↓
     │                         12. ReservationEndpoint
     │                                ↓
     │                         13. SecurityUtil.getCurrentMemberId()
     │                                ↓
     │                         14. ReservationUseCase
     │                                ↓
     │  15. 예약 목록 응답                      │
     │<────────────────────────────────────────┤
     │                                         │
```

### 세부 단계 설명

#### 로그인 플로우
1. **클라이언트**: 이메일/비밀번호로 로그인 요청
2. **MemberEndpoint**: 요청 수신 및 Validation
3. **MemberUseCase**: 비즈니스 로직 처리
4. **비밀번호 검증**: BCrypt로 암호화된 비밀번호 비교
5. **JwtTokenProvider**: JWT 토큰 생성
6. **응답**: Access Token + Refresh Token 반환

#### 인증이 필요한 API 호출 플로우
7. **클라이언트**: Authorization 헤더에 Access Token 포함하여 요청
8. **JwtAuthenticationFilter**: 모든 요청에 대해 실행 (OncePerRequestFilter)
9. **토큰 추출**: Authorization 헤더에서 "Bearer " 제거 후 토큰 추출
10. **토큰 검증**: JwtTokenProvider로 서명 및 만료 시간 검증
11. **SecurityContext 설정**: 인증 정보를 SecurityContext에 저장
12. **Controller**: 정상적으로 요청 처리
13. **SecurityUtil**: SecurityContext에서 회원 ID 추출
14. **UseCase**: 비즈니스 로직 처리
15. **응답**: 요청에 대한 응답 반환

---

## 코드 상세 설명

### 1. 토큰 생성 시 Claims 구조
```java
Map<String, Object> claims = new HashMap<>();
claims.put("memberId", 1L);              // 회원 ID
claims.put("email", "user@example.com"); // 이메일
claims.put("role", "USER");              // 권한

// Jwts.builder()로 전달되면 자동으로 추가되는 클레임들:
// - sub: "1" (Subject, memberId를 문자열로 저장)
// - iat: 1738368000 (Issued At, 발급 시간)
// - exp: 1738371600 (Expiration, 만료 시간)
```

### 2. 토큰 서명 과정
```java
// 1. SecretKey 생성 (HS256 알고리즘)
SecretKey secretKey = Keys.hmacShaKeyFor(
    secret.getBytes(StandardCharsets.UTF_8)
);

// 2. 서명 생성
String jwt = Jwts.builder()
    .claims(claims)
    .subject(String.valueOf(memberId))
    .issuedAt(now)
    .expiration(expiryDate)
    .signWith(secretKey)  // HMACSHA256로 서명
    .compact();           // Base64 인코딩 후 문자열로 반환
```

### 3. 토큰 검증 과정
```java
// 1. 파서 생성 및 서명 검증
Claims claims = Jwts.parser()
    .verifyWith(secretKey)  // 서명 검증 (중요!)
    .build()
    .parseSignedClaims(token)
    .getPayload();

// 2. 만료 시간 자동 검증
// - exp 클레임이 현재 시간보다 이전이면 ExpiredJwtException 발생

// 3. 서명 검증 실패 시 예외
// - 서명이 일치하지 않으면 SignatureException 발생
// - 토큰이 변조되었을 경우 감지 가능
```

### 4. SecurityContext 설정
```java
// 1. Authentication 객체 생성
UsernamePasswordAuthenticationToken authentication =
    new UsernamePasswordAuthenticationToken(
        memberId,  // Principal: 인증 주체 (회원 ID)
        null,      // Credentials: 자격증명 (비밀번호, JWT에서는 불필요)
        Collections.singletonList(
            new SimpleGrantedAuthority(role)  // Authorities: 권한 목록
        )
    );

// 2. SecurityContext에 저장
SecurityContextHolder.getContext().setAuthentication(authentication);

// 3. 이후 컨트롤러에서 사용
Long memberId = SecurityUtil.getCurrentMemberId();
// → SecurityContextHolder에서 Authentication 객체를 가져와
//   getName()을 호출하면 Principal(memberId)을 반환
```

---

## 보안 고려사항

### 1. Secret Key 보안
```yaml
# 주의: 실제 운영 환경에서는 절대 application.yml에 직접 저장하지 말 것
# 환경 변수나 AWS Secrets Manager 등 사용 권장

# 개발 환경 (application-dev.yml)
jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation1234567890

# 운영 환경 (환경 변수 사용)
jwt:
  secret: ${JWT_SECRET}  # 환경 변수에서 로드
```

### 2. HTTPS 사용 필수
```
HTTP에서는 JWT 토큰이 평문으로 전송되어 탈취 위험
운영 환경에서는 반드시 HTTPS 사용
```

### 3. Access Token 만료 시간
```
짧은 유효기간 (1시간) 설정
Refresh Token으로 재발급 가능하므로 UX 저하 없음
토큰 탈취 시 피해 최소화
```

### 4. Refresh Token 저장
```
현재: 응답 본문에 포함 (클라이언트가 어디에 저장할지 선택)
권장: HttpOnly Cookie에 저장 (XSS 공격 방어)
권장: Secure 플래그 설정 (HTTPS에서만 전송)
```

### 5. 블랙리스트 (로그아웃)
```java
// 현재: Redis 주석 처리 (실제 동작 안함)
// 권장: Redis를 활성화하여 로그아웃된 토큰을 블랙리스트에 저장

// RedisTokenManager 예시
public void addToBlacklist(String token, long expiration) {
    redisTemplate.opsForValue().set(
        "blacklist:" + token,
        "true",
        expiration,
        TimeUnit.MILLISECONDS
    );
}

public boolean isBlacklisted(String token) {
    return Boolean.TRUE.equals(
        redisTemplate.hasKey("blacklist:" + token)
    );
}
```

### 6. CORS 설정
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
    configuration.setExposedHeaders(Arrays.asList("Authorization"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 7. Rate Limiting
```
권장: 로그인 API에 Rate Limiting 적용
권장: Bucket4j 또는 Spring Cloud Gateway 사용
목적: 무차별 대입 공격(Brute Force) 방어
```

---

## 테스트 시나리오

### 1. 로그인 및 토큰 발급
```bash
# 1. 로그인
curl -X POST http://localhost:8080/api/members/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123!"
  }'

# 응답
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

### 2. Access Token으로 API 호출
```bash
# 2. 내 정보 조회 (인증 필요)
curl -X GET http://localhost:8080/api/members/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# 응답
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

### 3. Access Token 만료 후 갱신
```bash
# 3. 토큰 갱신 (Refresh Token 사용)
curl -X POST http://localhost:8080/api/members/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }'

# 응답 (새로운 토큰)
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...(new)",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...(new)",
  "tokenType": "Bearer"
}
```

### 4. 로그아웃
```bash
# 4. 로그아웃 (토큰 블랙리스트 등록)
curl -X POST http://localhost:8080/api/members/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# 응답 (204 No Content)
```

---

## 요약

### ✅ 구현된 내용
1. **JWT 토큰 생성**: HS256 알고리즘, Access Token + Refresh Token
2. **JWT 토큰 검증**: 서명 검증, 만료 시간 검증
3. **Spring Security 통합**: JwtAuthenticationFilter, SecurityContext
4. **인증 정보 추출**: SecurityUtil을 통한 편리한 사용자 정보 접근
5. **예외 처리**: 만료, 서명 오류, 잘못된 형식 등 다양한 예외 처리

### 🎯 핵심 포인트
- **무상태(Stateless)**: 서버에 세션 저장 없이 토큰만으로 인증
- **확장성**: 분산 환경에서도 동일한 Secret Key만 있으면 인증 가능
- **보안**: HMAC-SHA256 서명으로 토큰 변조 방지
- **자동화**: JwtAuthenticationFilter가 모든 요청에 대해 자동으로 인증 처리

---

**문서 버전**: 1.0  
**최종 업데이트**: 2026-01-31  
**작성자**: Accommodation Reservation System Team

