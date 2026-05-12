package io.identityaccess.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.session.enumtype.SessionStatus;
import io.identityaccess.domain.model.session.valueobject.ClientDevice;
import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.model.user.UserAggregate;
import io.identityaccess.domain.model.user.entity.UserCredential;
import io.identityaccess.domain.model.user.enumtype.CredentialStatus;
import io.identityaccess.domain.model.user.enumtype.UserStatus;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import io.identityaccess.domain.model.user.valueobject.UserId;
import io.identityaccess.domain.service.SessionPolicy;
import io.identityaccess.domain.service.TokenPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SessionAggregateTest {

    @Test
    void shouldOpenActiveSessionAndEmitDomainEvent() {
        UserAggregate user = new UserAggregate(
                UserId.of("usr-1"),
                EmailAddress.of("user@example.test"),
                UserStatus.ACTIVE,
                new UserCredential("cred-1", UserId.of("usr-1"), EmailAddress.of("user@example.test"), "hash", CredentialStatus.ACTIVE),
                List.of(),
                Set.of("SYSTEM_ADMIN"));

        SessionAggregate session = SessionAggregate.open(
                user,
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("10.0.0.1"),
                Instant.parse("2026-01-01T00:00:00Z"),
                new SessionPolicy(),
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7)));

        assertNotNull(session.id());
        assertEquals(SessionStatus.ACTIVE, session.status());
        assertNotNull(session.domainEvent());
        assertEquals("SessionOpened", session.domainEvent().eventType());
        assertTrue(session.timestamps().refreshTokenExpiresAt().isAfter(session.timestamps().accessTokenExpiresAt()));
    }
}
