package com.arka.identityaccess.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.entity.AccountCredential;
import com.arka.identityaccess.domain.identity.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SessionAggregateTest {

    @Test
    void shouldOpenActiveSessionAndEmitDomainEvent() {
        AccountAggregate user = AccountAggregate.rehydrate(
                AccountId.of("usr-1"),
                EmailAddress.of("user@arka.com"),
                true,
                AccountStatus.ACTIVE,
                new AccountCredential("cred-1", AccountId.of("usr-1"), EmailAddress.of("user@arka.com"), "hash", CredentialStatus.ACTIVE),
                0);

        SessionAggregate session = SessionAggregate.open(
                user.id(),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("10.0.0.1"),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:15:00Z"),
                Instant.parse("2026-01-08T00:00:00Z"));

        assertNotNull(session.id());
        assertEquals(SessionStatus.ACTIVE, session.status());
        var events = session.pullDomainEvents();
        assertEquals(1, events.size());
        assertEquals("SessionOpened", events.get(0).eventType());
        assertTrue(session.pullDomainEvents().isEmpty());
        assertTrue(session.timestamps().refreshTokenExpiresAt().isAfter(session.timestamps().accessTokenExpiresAt()));
    }
}
