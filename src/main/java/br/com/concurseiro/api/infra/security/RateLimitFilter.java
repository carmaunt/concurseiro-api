package br.com.concurseiro.api.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String LOGIN_PATH = "/auth/login";
    private static final String KEY_PREFIX = "rate_limit:login:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !LOGIN_PATH.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = extractClientIp(request);
        String key = KEY_PREFIX + clientIp;

        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(key, WINDOW);
        }

        if (attempts != null && attempts > MAX_ATTEMPTS) {
            writeTooManyRequests(response, request);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {

        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");

        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(
            HttpServletResponse response,
            HttpServletRequest request
    ) throws IOException {

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(WINDOW.toSeconds()));

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("type", URI.create("https://httpstatuses.com/429").toString());
        body.put("title", "Too Many Requests");
        body.put("status", 429);
        body.put("detail", "Muitas tentativas de login. Tente novamente em instantes.");
        body.put("instance", request.getRequestURI());

        objectMapper.writeValue(response.getWriter(), body);
    }
}