package heej.net.domain.member.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
    public String generateAccessToken(Long memberId, String email, String role) {
        return generateToken(memberId, email, role, accessTokenExpiration);
    }
    public String generateRefreshToken(Long memberId, String email, String role) {
        return generateToken(memberId, email, role, refreshTokenExpiration);
    }
    private String generateToken(Long memberId, String email, String role, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId);
        claims.put("email", email);
        claims.put("role", role);
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
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
    public Long getMemberId(String token) {
        Claims claims = parseToken(token);
        return claims.get("memberId", Long.class);
    }
    public String getEmail(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}