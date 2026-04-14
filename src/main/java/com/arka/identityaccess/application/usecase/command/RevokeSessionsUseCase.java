package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.RevokeSessionsCommand;
import com.arka.identityaccess.application.port.in.RevokeSessionsCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.RevokeSessionsResult;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.application.exception.AccountNotFoundException;
import com.arka.identityaccess.domain.session.event.SessionRevokedEvent;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RevokeSessionsUseCase implements RevokeSessionsCommandUseCase {

    private final UserPersistencePort userPersistencePort;
    private final SessionPersistencePort sessionPersistencePort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ClockPort clockPort;

    public RevokeSessionsUseCase(
            UserPersistencePort userPersistencePort,
            SessionPersistencePort sessionPersistencePort,
            SecurityAuditPort securityAuditPort,
            OutboxPersistencePort outboxPersistencePort,
            ClockPort clockPort) {
        this.userPersistencePort = userPersistencePort;
        this.sessionPersistencePort = sessionPersistencePort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.clockPort = clockPort;
    }

    @Override
    public Mono<RevokeSessionsResult> handle(RevokeSessionsCommand command) {
        AccountId targetUserId = AccountId.of(command.userId());
        String actorUserId = requireActor(command.actorUserId());
        String reason = normalizeReason(command.reason());
        Instant revokedAt = clockPort.now();

        return userPersistencePort.existsById(targetUserId)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new AccountNotFoundException()))
                .thenMany(Flux.defer(() -> sessionPersistencePort.revokeActiveSessionsByUserId(
                        targetUserId,
                        reason,
                        revokedAt)))
                .collectList()
                .flatMap(revokedSessions -> securityAuditPort
                        .recordSessionsRevoked(actorUserId, targetUserId.value(), reason, revokedSessions.size())
                        .then(Flux.fromIterable(revokedSessions)
                                .map(session -> SessionRevokedEvent.create(
                                        targetUserId,
                                        session.id(),
                                        revokedAt,
                                        reason))
                                .concatMap(outboxPersistencePort::store)
                                .then())
                        .thenReturn(new RevokeSessionsResult(
                                targetUserId.value(),
                                revokedSessions.size(),
                                reason,
                                "COMPLETED")));
    }

    private String requireActor(String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new OperationNotPermittedException("Authenticated actor is required");
        }
        return actorUserId.trim();
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "ADMIN_REVOKE";
        }
        return reason.trim();
    }
}
