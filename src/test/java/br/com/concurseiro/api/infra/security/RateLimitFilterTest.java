package br.com.concurseiro.api.infra.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    @Test
    void devePermitir_quandoNaoEhLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/questoes");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertNotEquals(429, response.getStatus());
        assertEquals(200, response.getStatus());
    }

    @Test
    void devePermitir_login_ateOLimite() throws Exception {
        for (int i = 1; i <= 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.addHeader("X-Forwarded-For", "203.0.113.10");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertNotEquals(429, response.getStatus());
        }
    }

    @Test
    void deveBloquear_aposLimite() throws Exception {
        for (int i = 1; i <= 11; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.addHeader("X-Forwarded-For", "198.51.100.20");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            if (i <= 10) {
                assertNotEquals(429, response.getStatus());
            } else {
                assertEquals(429, response.getStatus());
            }
        }
    }

    @Test
    void deveUsarXForwardedFor_quandoPresente() throws Exception {
        for (int i = 1; i <= 11; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.addHeader("X-Forwarded-For", "203.0.113.10");
            request.setRemoteAddr("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            if (i <= 10) {
                assertNotEquals(429, response.getStatus());
            } else {
                assertEquals(429, response.getStatus());
            }
        }
    }
}