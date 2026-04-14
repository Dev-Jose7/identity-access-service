package com.arka.identityaccess.application.port.out.audit;

import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import reactor.core.publisher.Mono;

public interface SecurityAuditPort {

    Mono<Void> recordLoginSuccess(AccountAggregate account, SessionAggregate session);

    Mono<Void> recordUserRegistered(AccountAggregate account);

    Mono<Void> recordSessionRefreshed(SessionAggregate session);

    Mono<Void> recordSessionRevoked(SessionAggregate session);

    Mono<Void> recordRoleAssigned(String actorUserId, String targetUserId, String roleCode, boolean assigned);

    Mono<Void> recordUserBlocked(String actorUserId, String targetUserId, String reason, boolean changed);

    Mono<Void> recordSessionsRevoked(String actorUserId, String targetUserId, String reason, long revokedSessions);
}
