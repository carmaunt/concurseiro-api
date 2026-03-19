package br.com.concurseiro.api.infra.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        LoginRateLimitService rateLimitService = org.mockito.Mockito.mock(LoginRateLimitService.class);

        org.mockito.Mockito.when(rateLimitService.isAllowed(
                org.mockito.Mockito.anyString(),
                org.mockito.Mockito.anyString()
        )).thenReturn(true);

        filter = new RateLimitFilter(rateLimitService);
    }

    @Test
    void devePermitir_quandoNaoEhLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/questoes");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertNotEquals(429, response.getStatus());
        assertEquals(200, response.getStatus());
    }

    @Test
    void devePermitir_primeirasNTentativas() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setRemoteAddr("192.168.1.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertNotEquals(429, response.getStatus(), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void deveBloquear_aposLimite() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setRemoteAddr("10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertEquals(429, response.getStatus());
    }

    @Test
    void deveUsarXForwardedFor_quandoPresente() throws Exception {
        for (int i = 0; i < 11; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setRemoteAddr("127.0.0.1");
            request.addHeader("X-Forwarded-For", "203.0.113.50");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            if (i < 10) {
                assertNotEquals(429, response.getStatus(), "Request " + (i + 1) + " should be allowed");
            } else {
                assertEquals(429, response.getStatus(), "Request 11 should be blocked");
            }
        }
    }
}
