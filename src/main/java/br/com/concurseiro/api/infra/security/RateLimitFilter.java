package br.com.concurseiro.api.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;
    private static final int MAX_ENTRIES = 10_000;
    private static final long CLEANUP_INTERVAL_MS = 60_000;

    private final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanup = new AtomicLong(System.currentTimeMillis());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!"/api/v1/auth/login".equals(request.getRequestURI()) || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        evictExpiredEntries();

        String clientIp = getClientIp(request);
        RequestCounter counter = counters.compute(clientIp, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new RequestCounter();
            }
            return existing;
        });

        if (counter.incrementAndCheck()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"type\":\"https://concurseiro.dev/errors/rate-limit\","
                    + "\"title\":\"Muitas tentativas\","
                    + "\"status\":429,"
                    + "\"detail\":\"Limite de tentativas de login excedido. Tente novamente em breve.\"}"
            );
        }
    }

    private void evictExpiredEntries() {
        long now = System.currentTimeMillis();
        long last = lastCleanup.get();
        if (now - last > CLEANUP_INTERVAL_MS || counters.size() > MAX_ENTRIES) {
            if (lastCleanup.compareAndSet(last, now)) {
                counters.entrySet().removeIf(e -> e.getValue().isExpired());
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestCounter {
        private final Instant windowStart = Instant.now();
        private int count = 0;

        boolean isExpired() {
            return Instant.now().isAfter(windowStart.plusSeconds(WINDOW_SECONDS));
        }

        synchronized boolean incrementAndCheck() {
            count++;
            return count <= MAX_ATTEMPTS;
        }
    }
}
