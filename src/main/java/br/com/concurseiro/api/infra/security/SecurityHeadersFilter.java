package br.com.concurseiro.api.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // HSTS - Força HTTPS por 1 ano, incluindo subdomínios
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Previne clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Previne MIME sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Previne XSS via reflected
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Content Security Policy básica
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data:; " +
                "font-src 'self'; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none';");

        filterChain.doFilter(request, response);
    }
}