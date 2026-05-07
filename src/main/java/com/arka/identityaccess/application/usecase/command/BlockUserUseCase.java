package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.BlockUserCommand;
import com.arka.identityaccess.application.port.in.BlockUserCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.BlockUserResult;
import com.arka.identityaccess.domain.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.model.user.event.UserBlockedEvent;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BlockUserUseCase implements BlockUserCommandUseCase {

    private final UserPersistencePort userPersistencePort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ClockPort clockPort;

    public BlockUserUseCase(
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
    public Mono<BlockUserResult> handle(BlockUserCommand command) {
        UserId targetUserId = UserId.of(command.userId());
        String actorUserId = requireActor(command.actorUserId());
        String reason = normalizeReason(command.reason());

        return userPersistencePort
                .loadStatus(targetUserId)
                .flatMap(currentStatus -> userPersistencePort
                        .block(targetUserId, clockPort.now())
                        .flatMap(updatedStatus -> {
                            boolean changed = !"BLOCKED".equalsIgnoreCase(currentStatus.status());
                            Mono<Void> publishEvent = changed
                                    ? outboxPersistencePort.store(UserBlockedEvent.create(
                                                    updatedStatus.userId(),
                                                    actorUserId,
                                                    reason,
                                                    clockPort.now()))
                                            .then()
                                    : Mono.empty();
                            return securityAuditPort
                                    .recordUserBlocked(actorUserId, updatedStatus.userId(), reason, changed)
                                    .then(publishEvent)
                                    .thenReturn(new BlockUserResult(
                                            updatedStatus.userId(),
                                            updatedStatus.status(),
                                            changed));
                        }));
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
}
