package com.arka.identityaccess.domain.session.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TokenPolicyTest {

    @Test
    void shouldRejectInvalidTtls() {
        assertThrows(
                DomainInvariantViolationException.class,
                () -> new TokenPolicy(Duration.ofMinutes(15), Duration.ofMinutes(15)));
        assertThrows(
                DomainInvariantViolationException.class,
                () -> new TokenPolicy(Duration.ZERO, Duration.ofDays(1)));
    }

    @Test
    void shouldCalculateTokenExpiries() {
        TokenPolicy tokenPolicy = new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7));
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        assertEquals(now.plus(Duration.ofMinutes(15)), tokenPolicy.calculateAccessExpiry(now));
        assertEquals(now.plus(Duration.ofDays(7)), tokenPolicy.calculateRefreshExpiry(now));
    }
}
