package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.identityaccess.application.command.RevokeSessionsCommand;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.RevokeSessionsResult;
import io.identityaccess.application.usecase.command.RevokeSessionsUseCase;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.exception.UserNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RevokeSessionsUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private SessionPersistencePort sessionPersistencePort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldRevokeSessionsAndPublishEventWhenAnySessionWasRevoked() {
        RevokeSessionsUseCase useCase = new RevokeSessionsUseCase(
                userPersistencePort,
                sessionPersistencePort,
                securityAuditPort,
                clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        RevokeSessionsCommand command = new RevokeSessionsCommand("usr-55", "actor-1", null);

        when(userPersistencePort.existsById(any())).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(now);
        when(sessionPersistencePort.revokeActiveSessionsByUserId(any(), eq("ADMIN_REVOKE"), eq(now)))
                .thenReturn(Mono.just(3L));
        when(securityAuditPort.recordSessionsRevoked("actor-1", "usr-55", "ADMIN_REVOKE", 3L))
                .thenReturn(Mono.empty());
        RevokeSessionsResult result = useCase.handle(command).block();

        assertEquals("usr-55", result.userId());
        assertEquals(3L, result.revokedSessions());
        assertEquals("ADMIN_REVOKE", result.reason());
        assertEquals("COMPLETED", result.status());
    }

    @Test
    void shouldNotPublishEventWhenNoSessionsAreRevoked() {
        RevokeSessionsUseCase useCase = new RevokeSessionsUseCase(
                userPersistencePort,
                sessionPersistencePort,
                securityAuditPort,
                clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        RevokeSessionsCommand command = new RevokeSessionsCommand("usr-55", "actor-1", "MANUAL");

        when(userPersistencePort.existsById(any())).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(now);
        when(sessionPersistencePort.revokeActiveSessionsByUserId(any(), eq("MANUAL"), eq(now)))
                .thenReturn(Mono.just(0L));
        when(securityAuditPort.recordSessionsRevoked("actor-1", "usr-55", "MANUAL", 0L))
                .thenReturn(Mono.empty());

        RevokeSessionsResult result = useCase.handle(command).block();

        assertEquals("usr-55", result.userId());
        assertEquals(0L, result.revokedSessions());
        assertEquals("MANUAL", result.reason());
    }

    @Test
    void shouldFailWhenTargetUserDoesNotExist() {
        RevokeSessionsUseCase useCase = new RevokeSessionsUseCase(
                userPersistencePort,
                sessionPersistencePort,
                securityAuditPort,
                clockPort);

        RevokeSessionsCommand command = new RevokeSessionsCommand("usr-55", "actor-1", "MANUAL");
        when(userPersistencePort.existsById(any())).thenReturn(Mono.just(false));

        assertThrows(UserNotFoundException.class, () -> useCase.handle(command).block());
        verify(sessionPersistencePort, never()).revokeActiveSessionsByUserId(any(), any(), any());
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        RevokeSessionsUseCase useCase = new RevokeSessionsUseCase(
                userPersistencePort,
                sessionPersistencePort,
                securityAuditPort,
                clockPort);

        RevokeSessionsCommand command = new RevokeSessionsCommand("usr-55", "", "MANUAL");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
    }
}
