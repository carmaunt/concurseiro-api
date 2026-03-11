package br.com.concurseiro.api.infra.security;

import com.bucket4j.Bandwidth;
import com.bucket4j.Bucket;
import com.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> localCache = new ConcurrentHashMap<>();
    private final ProxyManager<String> proxyManager;

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    public RateLimitFilter(@Value("${redis.url:}") String redisUrl) {
        if (redisUrl != null && !redisUrl.isBlank()) {
            RedisClient redisClient = RedisClient.create(redisUrl);
            StatefulRedisConnection<String, byte[]> connection = redisClient.connect();
            this.proxyManager = Bucket4jLettuce.casBasedBuilder(connection).build();
        } else {
            this.proxyManager = null; // Fallback to local cache
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = getRateLimitKey(request);
        Bucket bucket = getBucket(key);

        if (bucket.tryConsume(1)) {
            // Adicionar headers de rate limit
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            response.setHeader("X-Rate-Limit-Retry-After-Seconds",
                    String.valueOf(bucket.getAvailableTokens() == 0 ?
                            Duration.ofSeconds(60).getSeconds() : 0));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                    "{\"type\":\"https://concurseiro.dev/errors/rate-limit\","
                    + "\"title\":\"Muitas tentativas\","
                    + "\"status\":429,"
                    + "\"detail\":\"Limite de requisições excedido. Tente novamente em 60 segundos.\"}"
            );
        }
    }

    private String getRateLimitKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Rate limit por IP para auth endpoints
        if (uri.startsWith("/api/v1/auth")) {
            return "auth:" + getClientIp(request);
        }

        // Rate limit por usuário autenticado para outros endpoints
        String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : getClientIp(request);
        return "api:" + user + ":" + method + ":" + uri;
    }

    private Bucket getBucket(String key) {
        if (proxyManager != null) {
            return proxyManager.builder()
                    .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                    .build(key, () -> Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))));
        } else {
            return localCache.computeIfAbsent(key, k ->
                    Bucket.builder()
                            .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                            .build());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
