package io.identityaccess.application.port.out.audit;

import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.user.UserAggregate;
import reactor.core.publisher.Mono;

public interface SecurityAuditPort {

    Mono<Void> recordLoginSuccess(UserAggregate user, SessionAggregate session);

    Mono<Void> recordAccountRegistered(UserAggregate user);

    Mono<Void> recordSessionRefreshed(SessionAggregate session);

    Mono<Void> recordSessionRevoked(SessionAggregate session);

    Mono<Void> recordRoleAssignedToAccount(String actorUserId, String targetUserId, String roleCode, boolean assigned);

    Mono<Void> recordAccountBlocked(String actorUserId, String targetUserId, String reason, boolean changed);

    Mono<Void> recordAccountUnblocked(String actorUserId, String targetUserId, String reason, boolean changed);

    Mono<Void> recordSessionsRevoked(String actorUserId, String targetUserId, String reason, long revokedSessions);

    Mono<Void> recordAccessCatalogChanged(
            String actorUserId,
            String eventType,
            String targetType,
            String targetId,
            String targetCode,
            boolean changed);
}
