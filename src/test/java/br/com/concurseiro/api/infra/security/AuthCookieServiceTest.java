package br.com.concurseiro.api.infra.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCookieServiceTest {

    @Test
    void deveCriarCookiesHttpOnlyParaAccessERefreshToken() {
        AuthCookieService service = new AuthCookieService(true, "None", 14_400_000, 2_592_000);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.addAuthCookies(response, "access.jwt", "refresh-token");

        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("access_token=access.jwt")
                        .contains("Path=/")
                        .contains("HttpOnly")
                        .contains("Secure")
                        .contains("SameSite=None"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("refresh_token=refresh-token")
                        .contains("Path=/api/v1/auth/refresh")
                        .contains("HttpOnly")
                        .contains("Secure")
                        .contains("SameSite=None"));
    }

    @Test
    void deveLerTokensDosCookiesDaRequisicao() {
        AuthCookieService service = new AuthCookieService(false, "Lax", 14_400_000, 2_592_000);
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setCookies(
                new Cookie("access_token", "access.jwt"),
                new Cookie("refresh_token", "refresh-token")
        );

        assertThat(service.getAccessToken(request)).contains("access.jwt");
        assertThat(service.getRefreshToken(request)).contains("refresh-token");
    }

    @Test
    void deveLimparCookiesDeAutenticacao() {
        AuthCookieService service = new AuthCookieService(false, "Lax", 14_400_000, 2_592_000);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.clearAuthCookies(response);

        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("access_token=")
                        .contains("Max-Age=0"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("refresh_token=")
                        .contains("Max-Age=0"));
    }
}
