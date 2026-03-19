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
    private LoginRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = org.mockito.Mockito.mock(LoginRateLimitService.class);
        filter = new RateLimitFilter(rateLimitService);
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
        org.mockito.Mockito.when(
                rateLimitService.isAllowed(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString()
                )
        ).thenReturn(true, true, true, true, true);

        for (int i = 1; i <= 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setContentType("application/json");
            request.setContent("""
                    {"email":"teste@teste.com","senha":"123456"}
                    """.getBytes());

            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertNotEquals(429, response.getStatus());
        }
    }

    @Test
    void deveBloquear_aposLimite() throws Exception {
        org.mockito.Mockito.when(
                rateLimitService.isAllowed(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString()
                )
        ).thenReturn(true, true, true, true, true, false);

        for (int i = 1; i <= 6; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setContentType("application/json");
            request.setContent("""
                    {"email":"teste@teste.com","senha":"123456"}
                    """.getBytes());

            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            if (i <= 5) {
                assertNotEquals(429, response.getStatus());
            } else {
                assertEquals(429, response.getStatus());
            }
        }
    }

    @Test
    void deveUsarXForwardedFor_quandoPresente() throws Exception {
        org.mockito.Mockito.when(
                rateLimitService.isAllowed(
                        org.mockito.ArgumentMatchers.eq("203.0.113.10"),
                        org.mockito.ArgumentMatchers.anyString()
                )
        ).thenReturn(true, true, true, true, true, false);

        for (int i = 1; i <= 6; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.addHeader("X-Forwarded-For", "203.0.113.10");
            request.setRemoteAddr("127.0.0.1");
            request.setContentType("application/json");
            request.setContent("""
                    {"email":"teste@teste.com","senha":"123456"}
                    """.getBytes());

            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            if (i <= 5) {
                assertNotEquals(429, response.getStatus());
            } else {
                assertEquals(429, response.getStatus(), "Request " + i + " should be blocked");
            }
        }

        org.mockito.Mockito.verify(rateLimitService, org.mockito.Mockito.atLeastOnce())
                .isAllowed(org.mockito.ArgumentMatchers.eq("203.0.113.10"), org.mockito.ArgumentMatchers.anyString());
    }
}