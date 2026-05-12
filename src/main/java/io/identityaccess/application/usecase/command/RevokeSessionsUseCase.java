package io.identityaccess.application.usecase.command;

import io.identityaccess.application.command.RevokeSessionsCommand;
import io.identityaccess.application.port.in.RevokeSessionsCommandUseCase;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.RevokeSessionsResult;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.exception.UserNotFoundException;
import io.identityaccess.domain.model.user.valueobject.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RevokeSessionsUseCase implements RevokeSessionsCommandUseCase {

    private final UserPersistencePort userPersistencePort;
    private final SessionPersistencePort sessionPersistencePort;
    private final SecurityAuditPort securityAuditPort;
    private final ClockPort clockPort;

    public RevokeSessionsUseCase(
            UserPersistencePort userPersistencePort,
            SessionPersistencePort sessionPersistencePort,
            SecurityAuditPort securityAuditPort,
            ClockPort clockPort) {
        this.userPersistencePort = userPersistencePort;
        this.sessionPersistencePort = sessionPersistencePort;
        this.securityAuditPort = securityAuditPort;
        this.clockPort = clockPort;
    }

    @Override
    public Mono<RevokeSessionsResult> handle(RevokeSessionsCommand command) {
        UserId targetUserId = UserId.of(command.userId());
        String actorUserId = requireActor(command.actorUserId());
        String reason = normalizeReason(command.reason());

        return userPersistencePort.existsById(targetUserId)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new UserNotFoundException()))
                .then(Mono.defer(() -> sessionPersistencePort.revokeActiveSessionsByUserId(
                        targetUserId,
                        reason,
                        clockPort.now())))
                .flatMap(revokedSessions -> securityAuditPort
                        .recordSessionsRevoked(actorUserId, targetUserId.value(), reason, revokedSessions)
                        .thenReturn(new RevokeSessionsResult(
                                targetUserId.value(),
                                revokedSessions,
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
