package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.port.out.security.JwtVerificationPort;
import com.arka.identityaccess.application.query.IntrospectTokenQuery;
import com.arka.identityaccess.application.result.IntrospectResult;
import com.arka.identityaccess.application.usecase.query.IntrospectTokenUseCase;
import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.model.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.model.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.domain.model.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class IntrospectTokenUseCaseTest {

    @Mock
    private JwtVerificationPort jwtVerificationPort;

    @Mock
    private SessionPersistencePort sessionPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldReturnInactiveWhenTokenVerificationFails() {
        IntrospectTokenUseCase useCase = new IntrospectTokenUseCase(jwtVerificationPort, sessionPersistencePort, clockPort);

        when(jwtVerificationPort.verify("bad-token"))
                .thenReturn(Mono.just(JwtVerificationPort.VerificationResult.invalid("invalid_signature")));

        IntrospectResult result = useCase.handle(new IntrospectTokenQuery("bad-token")).block();

        assertFalse(result.active());
        assertEquals("invalid_signature", result.inactiveReason());
        verify(sessionPersistencePort, never()).findOptionalBySessionId(any());
    }

    @Test
    void shouldReturnActiveForValidAccessTokenWithActiveSession() {
        IntrospectTokenUseCase useCase = new IntrospectTokenUseCase(jwtVerificationPort, sessionPersistencePort, clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(clockPort.now()).thenReturn(now);

        JwtVerificationPort.VerificationResult verification = JwtVerificationPort.VerificationResult.valid(
                "usr-1",
                "ses-1",
                "access",
                "identity-access-service",
                List.of("arka-b2b"),
                now.getEpochSecond(),
                now.plusSeconds(600).getEpochSecond(),
                "acc-1",
                "user@arka.com",
                Set.of("ORG_OWNER"),
                Set.of("iam.user.read"));

        SessionAggregate session = SessionAggregate.rehydrate(
                SessionId.of("ses-1"),
                UserId.of("usr-1"),
                ClientDevice.of("dev-1", "Mac", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("acc-1"),
                RefreshJti.of("ref-1"),
                SessionTimestamps.of(now.minusSeconds(60), now.plusSeconds(600), now.plusSeconds(3600)),
                SessionStatus.ACTIVE,
                null);

        when(jwtVerificationPort.verify("ok-token")).thenReturn(Mono.just(verification));
        when(sessionPersistencePort.findOptionalBySessionId(SessionId.of("ses-1"))).thenReturn(Mono.just(session));

        IntrospectResult result = useCase.handle(new IntrospectTokenQuery("ok-token")).block();

        assertTrue(result.active());
        assertEquals("usr-1", result.subject());
        assertEquals("ses-1", result.sessionId());
        assertEquals("user@arka.com", result.email());
        assertEquals(Set.of("ORG_OWNER"), result.roles());
        assertEquals(Set.of("iam.user.read"), result.permissions());
    }

    @Test
    void shouldReturnInactiveWhenSessionIsRevoked() {
        IntrospectTokenUseCase useCase = new IntrospectTokenUseCase(jwtVerificationPort, sessionPersistencePort, clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        JwtVerificationPort.VerificationResult verification = JwtVerificationPort.VerificationResult.valid(
                "usr-1",
                "ses-1",
                "access",
                "identity-access-service",
                List.of("arka-b2b"),
                now.getEpochSecond(),
                now.plusSeconds(600).getEpochSecond(),
                "acc-1",
                "user@arka.com",
                Set.of("ORG_OWNER"),
                Set.of("iam.user.read"));

        SessionAggregate revokedSession = SessionAggregate.rehydrate(
                SessionId.of("ses-1"),
                UserId.of("usr-1"),
                ClientDevice.of("dev-1", "Mac", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("acc-1"),
                RefreshJti.of("ref-1"),
                SessionTimestamps.of(now.minusSeconds(60), now.plusSeconds(600), now.plusSeconds(3600)),
                SessionStatus.REVOKED,
                null);

        when(jwtVerificationPort.verify("revoked-token")).thenReturn(Mono.just(verification));
        when(sessionPersistencePort.findOptionalBySessionId(SessionId.of("ses-1"))).thenReturn(Mono.just(revokedSession));

        IntrospectResult result = useCase.handle(new IntrospectTokenQuery("revoked-token")).block();

        assertFalse(result.active());
        assertEquals("session_inactive_or_mismatch", result.inactiveReason());
    }

    @Test
    void shouldReturnInactiveWhenRefreshJtiDoesNotMatchSession() {
        IntrospectTokenUseCase useCase = new IntrospectTokenUseCase(jwtVerificationPort, sessionPersistencePort, clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        JwtVerificationPort.VerificationResult verification = JwtVerificationPort.VerificationResult.valid(
                "usr-1",
                "ses-1",
                "refresh",
                "identity-access-service",
                List.of("arka-b2b"),
                now.getEpochSecond(),
                now.plusSeconds(600).getEpochSecond(),
                "ref-other",
                "user@arka.com",
                Set.of("ORG_OWNER"),
                Set.of("iam.user.read"));

        SessionAggregate session = SessionAggregate.rehydrate(
                SessionId.of("ses-1"),
                UserId.of("usr-1"),
                ClientDevice.of("dev-1", "Mac", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("acc-1"),
                RefreshJti.of("ref-1"),
                SessionTimestamps.of(now.minusSeconds(60), now.plusSeconds(600), now.plusSeconds(3600)),
                SessionStatus.ACTIVE,
                null);

        when(jwtVerificationPort.verify("refresh-token")).thenReturn(Mono.just(verification));
        when(sessionPersistencePort.findOptionalBySessionId(SessionId.of("ses-1"))).thenReturn(Mono.just(session));

        IntrospectResult result = useCase.handle(new IntrospectTokenQuery("refresh-token")).block();

        assertFalse(result.active());
        assertEquals("session_inactive_or_mismatch", result.inactiveReason());
    }
}
