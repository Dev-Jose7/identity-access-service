package io.identityaccess.application.usecase.command;

import io.identityaccess.application.command.BlockUserCommand;
import io.identityaccess.application.port.in.BlockUserCommandUseCase;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.BlockUserResult;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.model.user.event.AccountBlockedEvent;
import io.identityaccess.domain.model.user.valueobject.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BlockUserUseCase implements BlockUserCommandUseCase {

    private final UserPersistencePort userPersistencePort;
    private final SessionPersistencePort sessionPersistencePort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ClockPort clockPort;

    public BlockUserUseCase(
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
    public Mono<BlockUserResult> handle(BlockUserCommand command) {
        UserId targetUserId = UserId.of(command.userId());
        String actorUserId = requireActor(command.actorUserId());
        String reason = normalizeReason(command.reason());

        return userPersistencePort
                .loadStatus(targetUserId)
                .flatMap(currentStatus -> rejectIfBlockingLastExclusiveRoleHolder(targetUserId, currentStatus)
                        .then(Mono.defer(() -> {
                            var now = clockPort.now();
                            return userPersistencePort.block(targetUserId, now)
                                    .map(updatedStatus -> new StatusChange(updatedStatus, now));
                        }))
                        .flatMap(updatedStatus -> {
                            boolean changed = !"BLOCKED".equalsIgnoreCase(currentStatus.status());
                            Mono<Void> publishEvent = changed
                                    ? outboxPersistencePort.store(AccountBlockedEvent.create(
                                                    updatedStatus.statusSnapshot().userId(),
                                                    actorUserId,
                                                    reason,
                                                    updatedStatus.changedAt()))
                                            .then()
                                    : Mono.empty();
                            Mono<Void> revokeSessions = "BLOCKED".equalsIgnoreCase(updatedStatus.statusSnapshot().status())
                                    ? sessionPersistencePort.revokeActiveSessionsByUserId(
                                                    targetUserId,
                                                    "ACCOUNT_BLOCKED",
                                                    updatedStatus.changedAt())
                                            .flatMap(revokedSessions -> securityAuditPort.recordSessionsRevoked(
                                                    actorUserId,
                                                    targetUserId.value(),
                                                    "ACCOUNT_BLOCKED",
                                                    revokedSessions))
                                    : Mono.empty();
                            return securityAuditPort
                                    .recordAccountBlocked(actorUserId, updatedStatus.statusSnapshot().userId(), reason, changed)
                                    .then(revokeSessions)
                                    .then(publishEvent)
                                    .thenReturn(new BlockUserResult(
                                            updatedStatus.statusSnapshot().userId(),
                                            updatedStatus.statusSnapshot().status(),
                                            changed));
                        }));
    }

    private Mono<Void> rejectIfBlockingLastExclusiveRoleHolder(
            UserId targetUserId,
            UserPersistencePort.UserStatusSnapshot currentStatus) {
        if ("BLOCKED".equalsIgnoreCase(currentStatus.status())) {
            return Mono.empty();
        }
        return userPersistencePort
                .wouldBlockLastActiveExclusiveRoleHolder(targetUserId)
                .flatMap(wouldBreakAccess -> Boolean.TRUE.equals(wouldBreakAccess)
                        ? Mono.error(new OperationNotPermittedException("Cannot block the last active exclusive role holder"))
                        : Mono.empty());
    }

    private String requireActor(String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new OperationNotPermittedException("Authenticated actor is required");
        }
        return actorUserId.trim();
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "ADMIN_BLOCK";
        }
        return reason.trim();
    }

    private record StatusChange(UserPersistencePort.UserStatusSnapshot statusSnapshot, java.time.Instant changedAt) {}
}
