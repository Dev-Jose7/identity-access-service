package com.arka.identityaccess.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.model.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.entity.UserCredential;
import com.arka.identityaccess.domain.model.user.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.model.user.enumtype.UserStatus;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.domain.service.SessionPolicy;
import com.arka.identityaccess.domain.service.TokenPolicy;
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
                EmailAddress.of("user@arka.com"),
                UserStatus.ACTIVE,
                new UserCredential("cred-1", UserId.of("usr-1"), EmailAddress.of("user@arka.com"), "hash", CredentialStatus.ACTIVE),
                List.of(),
                Set.of("ORG_OWNER"));

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
        assertEquals("UserLoggedIn", session.domainEvent().eventType());
        assertTrue(session.timestamps().refreshTokenExpiresAt().isAfter(session.timestamps().accessTokenExpiresAt()));
    }
}
