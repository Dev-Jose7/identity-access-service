package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.command.LogoutCommand;
import com.arka.identityaccess.application.mapper.command.LogoutCommandAssembler;
import com.arka.identityaccess.application.mapper.result.LogoutResultMapper;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.result.LogoutResult;
import com.arka.identityaccess.application.usecase.command.LogoutUseCase;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.session.service.SessionPolicy;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private LogoutCommandAssembler assembler;

    @Mock
    private SessionPersistencePort sessionPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Test
    void shouldRevokeSessionAndPersistOutboxOnLogout() {
        LogoutUseCase useCase = new LogoutUseCase(
                assembler,
                sessionPersistencePort,
                clockPort,
                new SessionPolicy(),
                securityAuditPort,
                outboxPersistencePort,
                new LogoutResultMapper());

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        SessionAggregate activeSession = SessionAggregate.rehydrate(
                SessionId.of("11111111-1111-1111-1111-111111111111"),
                AccountId.of("usr-1"),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("44444444-4444-4444-4444-444444444444"),
                RefreshJti.of("55555555-5555-5555-5555-555555555555"),
                SessionTimestamps.of(now.minusSeconds(60), now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.ACTIVE);

        when(assembler.toSessionId(any())).thenReturn(SessionId.of("11111111-1111-1111-1111-111111111111"));
        when(sessionPersistencePort.findBySessionId(any())).thenReturn(Mono.just(activeSession));
        when(clockPort.now()).thenReturn(now);
        when(sessionPersistencePort.update(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(securityAuditPort.recordSessionRevoked(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        LogoutResult result = useCase.handle(new LogoutCommand("11111111-1111-1111-1111-111111111111")).block();

        assertNotNull(result);
        assertEquals("11111111-1111-1111-1111-111111111111", result.sessionId());
        assertEquals("REVOKED", result.status());
        verify(sessionPersistencePort).update(any());
        verify(outboxPersistencePort).store(any());
    }
}
