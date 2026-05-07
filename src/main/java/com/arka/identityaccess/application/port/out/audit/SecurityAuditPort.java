package com.arka.identityaccess.application.port.out.audit;

import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import reactor.core.publisher.Mono;

public interface SecurityAuditPort {

    Mono<Void> recordLoginSuccess(UserAggregate user, SessionAggregate session);

    Mono<Void> recordUserRegistered(UserAggregate user);

    Mono<Void> recordSessionRefreshed(SessionAggregate session);

    Mono<Void> recordSessionRevoked(SessionAggregate session);

    Mono<Void> recordRoleAssigned(String actorUserId, String targetUserId, String roleCode, boolean assigned);

    Mono<Void> recordUserBlocked(String actorUserId, String targetUserId, String reason, boolean changed);

    Mono<Void> recordSessionsRevokedByUser(String actorUserId, String targetUserId, String reason, long revokedSessions);
}
