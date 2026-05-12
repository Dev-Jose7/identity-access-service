package io.identityaccess.infrastructure.adapter.out.cache;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.identityaccess.domain.exception.RateLimitExceededException;
import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RedisSecurityRateLimitAdapterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Test
    void shouldAllowLoginWhenCounterIsWithinThresholdAndSetExpiryOnFirstHit() {
        RedisSecurityRateLimitAdapter adapter = new RedisSecurityRateLimitAdapter(redisTemplate, 10, 30, 5);
        String key = "iam:rate:login:user@example.test:10.0.0.1";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key)).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(key, Duration.ofMinutes(1))).thenReturn(Mono.just(true));

        adapter.ensureLoginAllowed(EmailAddress.of("User@example.test"), ClientIp.of("10.0.0.1")).block();

        verify(valueOperations).increment(key);
        verify(redisTemplate).expire(key, Duration.ofMinutes(1));
    }

    @Test
    void shouldRejectLoginWhenCounterExceedsThreshold() {
        RedisSecurityRateLimitAdapter adapter = new RedisSecurityRateLimitAdapter(redisTemplate, 1, 30, 5);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("iam:rate:login:user@example.test:10.0.0.1")).thenReturn(Mono.just(2L));

        assertThrows(
                RateLimitExceededException.class,
                () -> adapter.ensureLoginAllowed(EmailAddress.of("user@example.test"), ClientIp.of("10.0.0.1")).block());
    }

    @Test
    void shouldUseFingerprintForRefreshRateLimitKey() {
        RedisSecurityRateLimitAdapter adapter = new RedisSecurityRateLimitAdapter(redisTemplate, 10, 30, 5);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(any())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(any(), any())).thenReturn(Mono.just(true));

        adapter.ensureRefreshAllowed("raw-refresh-token-value", ClientIp.of("10.0.0.1")).block();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).increment(keyCaptor.capture());
        String key = keyCaptor.getValue();

        assertTrue(key.startsWith("iam:rate:refresh:"));
        assertTrue(key.endsWith(":10.0.0.1"));
        assertFalse(key.contains("raw-refresh-token-value"));
    }

    @Test
    void shouldRejectRegisterRegistrationWhenCounterExceedsThreshold() {
        RedisSecurityRateLimitAdapter adapter = new RedisSecurityRateLimitAdapter(redisTemplate, 10, 30, 1);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("iam:rate:register:register@example.test:10.0.0.1"))
                .thenReturn(Mono.just(2L));

        assertThrows(
                RateLimitExceededException.class,
                () -> adapter.ensureRegistrationAllowed(
                                EmailAddress.of("register@example.test"),
                                ClientIp.of("10.0.0.1"))
                        .block());
    }
}
