package br.com.concurseiro.api.infra.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final boolean secure;
    private final String sameSite;
    private final long accessTokenMaxAgeSeconds;
    private final long refreshTokenMaxAgeSeconds;

    public AuthCookieService(
            @Value("${security.auth.cookie.secure:false}") boolean secure,
            @Value("${security.auth.cookie.same-site:Lax}") String sameSite,
            @Value("${jwt.expiration-ms}") long accessTokenExpirationMs,
            @Value("${security.jwt.refresh-token-expiration:2592000}") long refreshTokenMaxAgeSeconds
    ) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.accessTokenMaxAgeSeconds = Math.max(1, accessTokenExpirationMs / 1000);
        this.refreshTokenMaxAgeSeconds = refreshTokenMaxAgeSeconds;
    }

    public void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader("Set-Cookie", buildCookie(ACCESS_TOKEN_COOKIE, accessToken, "/", accessTokenMaxAgeSeconds).toString());
        response.addHeader("Set-Cookie", buildCookie(REFRESH_TOKEN_COOKIE, refreshToken, "/api/v1/auth/refresh", refreshTokenMaxAgeSeconds).toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie(ACCESS_TOKEN_COOKIE, "", "/", 0).toString());
        response.addHeader("Set-Cookie", buildCookie(REFRESH_TOKEN_COOKIE, "", "/api/v1/auth/refresh", 0).toString());
    }

    public Optional<String> getAccessToken(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private ResponseCookie buildCookie(String name, String value, String path, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }
}
