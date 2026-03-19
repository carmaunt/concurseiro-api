package br.com.concurseiro.api.infra.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginRateLimitService {

    private static final int MAX_ATTEMPTS_IP = 10;
    private static final int MAX_ATTEMPTS_EMAIL = 5;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    public LoginRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String ip, String email) {
        boolean ipAllowed = incrementWithinWindow("rl:login:ip:" + normalize(ip), MAX_ATTEMPTS_IP);
        boolean emailAllowed = incrementWithinWindow("rl:login:email:" + normalize(email), MAX_ATTEMPTS_EMAIL);
        return ipAllowed && emailAllowed;
    }

    private boolean incrementWithinWindow(String key, int maxAttempts) {
        Long current = redisTemplate.opsForValue().increment(key);

        if (current == null) {
            return false;
        }

        if (current == 1L) {
            redisTemplate.expire(key, WINDOW);
        }

        return current <= maxAttempts;
    }

    private String normalize(String value) {
        return value == null ? "unknown" : value.trim().toLowerCase();
    }
}