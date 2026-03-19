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
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"");

    private final LoginRateLimitService rateLimitService;

    public RateLimitFilter(LoginRateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!"/api/v1/auth/login".equals(request.getRequestURI())
                || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

        String ip = getClientIp(wrappedRequest);
        String email = extractEmail(wrappedRequest);

        if (!rateLimitService.isAllowed(ip, email)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"type\":\"https://concurseiro.dev/errors/rate-limit\","
                            + "\"title\":\"Muitas tentativas\","
                            + "\"status\":429,"
                            + "\"detail\":\"Limite de tentativas excedido. Tente novamente em breve.\"}"
            );
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private String extractEmail(CachedBodyHttpServletRequest request) {
        try {
            String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = EMAIL_PATTERN.matcher(body);
            return matcher.find() ? matcher.group(1).trim().toLowerCase() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}