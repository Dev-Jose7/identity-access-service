package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.BlockUserCommand;
import com.arka.identityaccess.application.port.in.BlockUserCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.BlockUserResult;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
        AccountId targetUserId = AccountId.of(command.userId());
        String actorUserId = requireActor(command.actorUserId());
        String reason = normalizeReason(command.reason());
        Instant now = clockPort.now();

        return userPersistencePort.loadById(targetUserId)
                .flatMap(user -> {
                    boolean changed = user.block(reason, now);
                    if (!changed) {
                        user.pullDomainEvents();
                        return securityAuditPort
                                .recordUserBlocked(actorUserId, user.id().value(), reason, false)
                                .thenReturn(new BlockUserResult(
                                        user.id().value(),
                                        user.status().name(),
                                        false));
                    }
                    return userPersistencePort
                            .block(targetUserId, now)
                            .flatMap(updatedStatus -> securityAuditPort
                                    .recordUserBlocked(actorUserId, updatedStatus.userId(), reason, true)
                                    .then(publishDomainEvents(user.pullDomainEvents()))
                                    .thenReturn(new BlockUserResult(
                                            updatedStatus.userId(),
                                            updatedStatus.status(),
                                            true)));
                });
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

    private Mono<Void> publishDomainEvents(Iterable<? extends DomainEvent> domainEvents) {
        return Flux.fromIterable(domainEvents).concatMap(outboxPersistencePort::store).then();
    }
}
