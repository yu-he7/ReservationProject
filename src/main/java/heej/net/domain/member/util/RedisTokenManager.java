package heej.net.domain.member.util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisTokenManager {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String BLACKLIST_PREFIX = "BL:";
    public void saveRefreshToken(Long memberId, String refreshToken, long expiration) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
        log.debug("Refresh token saved for member: {}", memberId);
    }
    public String getRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        return redisTemplate.opsForValue().get(key);
    }
    public void deleteRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.delete(key);
        log.debug("Refresh token deleted for member: {}", memberId);
    }
    public void addToBlacklist(String token, long expiration) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expiration, TimeUnit.MILLISECONDS);
        log.debug("Token added to blacklist");
    }
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}