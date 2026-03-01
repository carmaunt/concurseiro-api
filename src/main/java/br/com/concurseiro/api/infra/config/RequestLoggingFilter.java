package br.com.concurseiro.api.infra.config;

import br.com.concurseiro.api.infra.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";
    private static final String X_REQUEST_ID_HEADER = "X-Request-Id";

    private final JwtService jwtService;

    public RequestLoggingFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();

        try {
            MDC.put(REQUEST_ID_KEY, requestId);

            extractUserFromToken(request);

            response.setHeader(X_REQUEST_ID_HEADER, requestId);

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} {} {} {}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            MDC.clear();
        }
    }

    private void extractUserFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring("Bearer ".length()).trim();
                String email = jwtService.extractEmail(token);
                if (email != null) {
                    MDC.put(USER_ID_KEY, email);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
