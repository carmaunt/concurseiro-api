package br.com.concurseiro.api.infra.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoginRateLimitService {

    private static final int MAX_ATTEMPTS_IP = 10;
    private static final int MAX_ATTEMPTS_EMAIL = 5;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, LocalWindow> localWindows = new ConcurrentHashMap<>();
    private final AtomicInteger localOperations = new AtomicInteger();

    public LoginRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String ip, String email) {
        boolean ipAllowed = incrementWithinWindow("rl:login:ip:" + normalize(ip), MAX_ATTEMPTS_IP);
        boolean emailAllowed = incrementWithinWindow("rl:login:email:" + normalize(email), MAX_ATTEMPTS_EMAIL);
        return ipAllowed && emailAllowed;
    }

    private boolean incrementWithinWindow(String key, int maxAttempts) {
        try {
            Long current = redisTemplate.opsForValue().increment(key);

            if (current == null) {
                return false;
            }

            if (current == 1L) {
                redisTemplate.expire(key, WINDOW);
            }

            return current <= maxAttempts;
        } catch (DataAccessException ex) {
            return incrementLocally(key, maxAttempts);
        }
    }

    private boolean incrementLocally(String key, int maxAttempts) {
        long now = System.nanoTime();
        long expiresAt = now + WINDOW.toNanos();

        LocalWindow window = localWindows.compute(key, (ignored, current) -> {
            if (current == null || current.expiresAtNanos() <= now) {
                return new LocalWindow(1, expiresAt);
            }
            return new LocalWindow(current.attempts() + 1, current.expiresAtNanos());
        });

        if (localOperations.incrementAndGet() % 100 == 0) {
            localWindows.entrySet().removeIf(entry -> entry.getValue().expiresAtNanos() <= now);
        }

        return window.attempts() <= maxAttempts;
    }

    private String normalize(String value) {
        return value == null ? "unknown" : value.trim().toLowerCase();
    }

    private record LocalWindow(int attempts, long expiresAtNanos) {
    }
}
