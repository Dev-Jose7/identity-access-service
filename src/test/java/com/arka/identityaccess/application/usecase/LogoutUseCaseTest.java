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
import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.model.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.model.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.domain.model.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.domain.service.SessionPolicy;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
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
                SessionId.of("ses-1"),
                UserId.of("usr-1"),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("acc-old"),
                RefreshJti.of("ref-old"),
                SessionTimestamps.of(now.minusSeconds(60), now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.ACTIVE,
                null);

        when(assembler.toSessionId(any())).thenReturn(SessionId.of("ses-1"));
        when(sessionPersistencePort.findBySessionId(any())).thenReturn(Mono.just(activeSession));
        when(clockPort.now()).thenReturn(now);
        when(sessionPersistencePort.update(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(securityAuditPort.recordSessionRevoked(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-3",
                "Session",
                "ses-1",
                "SessionRevoked",
                "{}",
                "PENDING",
                now,
                null,
                0,
                null,
                now,
                now)));

        LogoutResult result = useCase.handle(new LogoutCommand("ses-1")).block();

        assertNotNull(result);
        assertEquals("ses-1", result.sessionId());
        assertEquals("REVOKED", result.status());
        verify(sessionPersistencePort).update(any());
        verify(outboxPersistencePort).store(any());
    }
}
