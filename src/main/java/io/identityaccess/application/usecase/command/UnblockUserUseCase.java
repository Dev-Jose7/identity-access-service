package io.identityaccess.application.usecase.command;

import io.identityaccess.application.command.UnblockUserCommand;
import io.identityaccess.application.port.in.UnblockUserCommandUseCase;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.UnblockUserResult;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.model.user.event.AccountUnblockedEvent;
import io.identityaccess.domain.model.user.valueobject.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UnblockUserUseCase implements UnblockUserCommandUseCase {

    private final UserPersistencePort userPersistencePort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ClockPort clockPort;

    public UnblockUserUseCase(
            UserPersistencePort userPersistencePort,
            SecurityAuditPort securityAuditPort,
            OutboxPersistencePort outboxPersistencePort,
            ClockPort clockPort) {
        this.userPersistencePort = userPersistencePort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.clockPort = clockPort;
    }

    @Override
    public Mono<UnblockUserResult> handle(UnblockUserCommand command) {
        UserId targetUserId = UserId.of(command.userId());
        String actorUserId = requireActor(command.actorUserId());
        String reason = normalizeReason(command.reason());

        return userPersistencePort
                .loadStatus(targetUserId)
                .flatMap(currentStatus -> rejectIfStateCannotBeUnblocked(currentStatus)
                        .then(Mono.defer(() -> {
                            var now = clockPort.now();
                            return userPersistencePort.unblock(targetUserId, now)
                                    .flatMap(updatedStatus -> {
                                        boolean changed = "BLOCKED".equalsIgnoreCase(currentStatus.status());
                                        Mono<Void> publishEvent = changed
                                                ? outboxPersistencePort.store(AccountUnblockedEvent.create(
                                                                updatedStatus.userId(),
                                                                actorUserId,
                                                                reason,
                                                                now))
                                                        .then()
                                                : Mono.empty();
                                        return securityAuditPort
                                                .recordAccountUnblocked(actorUserId, updatedStatus.userId(), reason, changed)
                                                .then(publishEvent)
                                                .thenReturn(new UnblockUserResult(
                                                        updatedStatus.userId(),
                                                        updatedStatus.status(),
                                                        changed));
                                    });
                        })));
    }

    private Mono<Void> rejectIfStateCannotBeUnblocked(UserPersistencePort.UserStatusSnapshot statusSnapshot) {
        if ("BLOCKED".equalsIgnoreCase(statusSnapshot.status()) || "ACTIVE".equalsIgnoreCase(statusSnapshot.status())) {
            return Mono.empty();
        }
        return Mono.error(new OperationNotPermittedException("Only BLOCKED accounts can be unblocked"));
    }

    private String requireActor(String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new OperationNotPermittedException("Authenticated actor is required");
        }
        return actorUserId.trim();
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "ADMIN_UNBLOCK";
        }
        return reason.trim();
    }
}
