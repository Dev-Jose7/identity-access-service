package com.arka.identityaccess.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import com.arka.identityaccess.domain.identity.valueobject.AuthenticationResult;
import com.arka.identityaccess.domain.identity.valueobject.AuthenticationSecurityRules;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IdentitySubmodelTest {

    @Test
    void shouldRegisterThenVerifyActivateAndAuthenticate() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        AccountAggregate account = AccountAggregate.register(
                EmailAddress.of("owner@arka.com"),
                "$2a$10$hash",
                true,
                now);

        assertFalse(account.emailVerified());
        assertEquals("PENDING_VERIFICATION", account.status().name());
        assertEquals("AccountRegistered", account.pullDomainEvents().get(0).eventType());

        account.markEmailVerified(now.plusSeconds(10));
        account.activate(now.plusSeconds(11));

        AuthenticationSecurityRules securityRules = new AuthenticationSecurityRules(
                5,
                true,
                "FAILED_AUTHENTICATION_THRESHOLD",
                Set.of("invalid_credentials"));

        AuthenticationResult failed = account.authenticate(false, securityRules, now.plusSeconds(20), "10.10.10.10");
        assertFalse(failed.success());
        assertEquals("invalid_credentials", failed.failureCode());

        AuthenticationResult success = account.authenticate(true, securityRules, now.plusSeconds(30), "10.10.10.10");
        assertTrue(success.success());
        assertEquals(0, account.failedAuthenticationCount());
    }

    @Test
    void shouldBlockAccountWhenInvalidCredentialsReachThreshold() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AccountAggregate account = AccountAggregate.register(
                EmailAddress.of("owner@arka.com"),
                "$2a$10$hash",
                false,
                now);
        account.pullDomainEvents();

        AuthenticationSecurityRules securityRules = new AuthenticationSecurityRules(
                2,
                true,
                "FAILED_AUTHENTICATION_THRESHOLD",
                Set.of("invalid_credentials"));

        AuthenticationResult firstFailure =
                account.authenticate(false, securityRules, now.plusSeconds(10), "10.10.10.10");
        assertFalse(firstFailure.success());
        assertEquals(AccountStatus.ACTIVE, account.status());

        AuthenticationResult secondFailure =
                account.authenticate(false, securityRules, now.plusSeconds(20), "10.10.10.10");
        assertFalse(secondFailure.success());
        assertEquals(AccountStatus.BLOCKED, account.status());
        assertEquals(2, account.failedAuthenticationCount());
        assertTrue(account.domainEvents().stream().anyMatch(event -> "AccountBlocked".equals(event.eventType())));
    }
}
