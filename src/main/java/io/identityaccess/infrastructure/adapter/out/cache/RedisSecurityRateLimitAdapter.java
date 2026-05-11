package io.identityaccess.infrastructure.adapter.out.cache;

import io.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import io.identityaccess.domain.exception.RateLimitExceededException;
import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RedisSecurityRateLimitAdapter implements SecurityRateLimitPort {

    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);
    private static final String LOGIN_PREFIX = "iam:rate:login";
    private static final String REFRESH_PREFIX = "iam:rate:refresh";
    private static final String REGISTRATION_PREFIX = "iam:rate:register";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final long loginRequestsPerMinute;
    private final long refreshRequestsPerMinute;
    private final long registrationRequestsPerMinute;

    public RedisSecurityRateLimitAdapter(
            ReactiveStringRedisTemplate redisTemplate,
            @Value("${app.redis.rate-limit.login-requests-per-minute:10}") long loginRequestsPerMinute,
            @Value("${app.redis.rate-limit.refresh-requests-per-minute:30}") long refreshRequestsPerMinute,
            @Value("${app.redis.rate-limit.registration-requests-per-minute:5}") long registrationRequestsPerMinute) {
        this.redisTemplate = redisTemplate;
        this.loginRequestsPerMinute = loginRequestsPerMinute;
        this.refreshRequestsPerMinute = refreshRequestsPerMinute;
        this.registrationRequestsPerMinute = registrationRequestsPerMinute;
    }

    @Override
    public Mono<Void> ensureLoginAllowed(EmailAddress email, ClientIp clientIp) {
        String key = LOGIN_PREFIX + ":" + normalizeEmail(email) + ":" + normalizeIp(clientIp);
        return enforceLimit(key, loginRequestsPerMinute, "Login rate limit exceeded");
    }

    @Override
    public Mono<Void> ensureRefreshAllowed(String refreshToken, ClientIp clientIp) {
        String key = REFRESH_PREFIX + ":" + fingerprint(refreshToken) + ":" + normalizeIp(clientIp);
        return enforceLimit(key, refreshRequestsPerMinute, "Refresh rate limit exceeded");
    }

    @Override
    public Mono<Void> ensureRegistrationAllowed(EmailAddress email, ClientIp clientIp) {
        String key = REGISTRATION_PREFIX + ":" + normalizeEmail(email) + ":" + normalizeIp(clientIp);
        return enforceLimit(key, registrationRequestsPerMinute, "Registration rate limit exceeded");
    }

    private Mono<Void> enforceLimit(String key, long threshold, String message) {
        if (threshold <= 0) {
            return Mono.empty();
        }

        return redisTemplate
                .opsForValue()
                .increment(key)
                .flatMap(counter -> {
                    Mono<Long> counterMono = Mono.just(counter == null ? 0L : counter);
                    if (counter != null && counter == 1L) {
                        counterMono = redisTemplate.expire(key, LOGIN_WINDOW).thenReturn(counter);
                    }
                    return counterMono;
                })
                .flatMap(counter -> {
                    if (counter > threshold) {
                        return Mono.error(new RateLimitExceededException(message));
                    }
                    return Mono.empty();
                });
    }

    private String normalizeEmail(EmailAddress email) {
        return email.normalized().toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(ClientIp clientIp) {
        return clientIp.value().trim();
    }

    private String fingerprint(String rawValue) {
        if (rawValue == null) {
            return "null";
        }
        String normalized = rawValue.trim();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString(normalized.hashCode());
        }
    }
}
