package heej.net.security;
import heej.net.domain.member.util.JwtTokenProvider;
import heej.net.domain.member.util.RedisTokenManager;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenManager redisTokenManager;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisTokenManager redisTokenManager) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTokenManager = redisTokenManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (StringUtils.hasText(token)) {
                // Redis가 비활성화된 경우 블랙리스트 체크 생략
                if (redisTokenManager != null && redisTokenManager.isBlacklisted(token)) {
                    log.warn("블랙리스트에 등록된 토큰입니다");
                    filterChain.doFilter(request, response);
                    return;
                }
                if (jwtTokenProvider.validateToken(token)) {
                    Long memberId = jwtTokenProvider.getMemberId(token);
                    String email = jwtTokenProvider.getEmail(token);
                    String role = jwtTokenProvider.getRole(token);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    memberId,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority(role))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("인증 성공: memberId={}, email={}, role={}", memberId, email, role);
                }
            }
        } catch (JwtException e) {
            log.error("JWT 처리 중 오류 발생: {}", e.getMessage());
        } catch (Exception e) {
            log.error("인증 필터 처리 중 오류 발생: {}", e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}