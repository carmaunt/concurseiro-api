package br.com.concurseiro.api.infra.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginRateLimitServiceTest {

    @Test
    void deveUsarFallbackLocalQuandoRedisEstiverIndisponivel() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> operations = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(operations);
        when(operations.increment(anyString())).thenThrow(new RedisConnectionFailureException("offline"));

        LoginRateLimitService service = new LoginRateLimitService(redis);

        for (int attempt = 1; attempt <= 5; attempt++) {
            assertThat(service.isAllowed("127.0.0.1", "user@example.com")).isTrue();
        }
        assertThat(service.isAllowed("127.0.0.1", "user@example.com")).isFalse();
    }
}
