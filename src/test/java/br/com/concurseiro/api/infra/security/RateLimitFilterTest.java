package br.com.concurseiro.api.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {

        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        filterChain = mock(FilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        filter = new RateLimitFilter(redisTemplate, new ObjectMapper());
    }

    @Test
    void shouldAllowRequestWhenUnderLimit() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(valueOperations.increment("rate_limit:login:127.0.0.1")).thenReturn(1L);
        when(redisTemplate.expire("rate_limit:login:127.0.0.1", Duration.ofMinutes(1))).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void shouldBlockRequestWhenLimitExceeded() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(valueOperations.increment("rate_limit:login:127.0.0.1")).thenReturn(11L);

        filter.doFilter(request, response, filterChain);

        assertEquals(429, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldSetExpirationOnFirstAttempt() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(valueOperations.increment("rate_limit:login:127.0.0.1")).thenReturn(1L);
        when(redisTemplate.expire("rate_limit:login:127.0.0.1", Duration.ofMinutes(1))).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(redisTemplate).expire(eq("rate_limit:login:127.0.0.1"), eq(Duration.ofMinutes(1)));
    }
}