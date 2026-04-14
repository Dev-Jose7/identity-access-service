package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.AuthAuditRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveAuthAuditRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthAuditR2dbcRepositoryAdapter implements SecurityAuditPort {

    private final ReactiveAuthAuditRepository reactiveAuthAuditRepository;
    private final AuthAuditRowMapper authAuditRowMapper;

    public AuthAuditR2dbcRepositoryAdapter(ReactiveAuthAuditRepository reactiveAuthAuditRepository, AuthAuditRowMapper authAuditRowMapper) {
        this.reactiveAuthAuditRepository = reactiveAuthAuditRepository;
        this.authAuditRowMapper = authAuditRowMapper;
    }

    @Override
    public Mono<Void> recordLoginSuccess(AccountAggregate account, SessionAggregate session) {
        return insert(authAuditRowMapper.toLoginSuccessRow(account, session));
    }

    @Override
    public Mono<Void> recordUserRegistered(AccountAggregate account) {
        return insert(authAuditRowMapper.toRegisterRow(account));
    }

    @Override
    public Mono<Void> recordSessionRefreshed(SessionAggregate session) {
        return insert(authAuditRowMapper.toSessionEventRow("SESSION_REFRESHED", session));
    }

    @Override
    public Mono<Void> recordSessionRevoked(SessionAggregate session) {
        return insert(authAuditRowMapper.toSessionEventRow("SESSION_REVOKED", session));
    }

    @Override
    public Mono<Void> recordRoleAssigned(String actorUserId, String targetUserId, String roleCode, boolean assigned) {
        return insert(authAuditRowMapper.toRoleAssignedRow(actorUserId, targetUserId, roleCode, assigned));
    }

    @Override
    public Mono<Void> recordUserBlocked(String actorUserId, String targetUserId, String reason, boolean changed) {
        return insert(authAuditRowMapper.toUserBlockedRow(actorUserId, targetUserId, reason, changed));
    }

    @Override
    public Mono<Void> recordSessionsRevoked(String actorUserId, String targetUserId, String reason, long revokedSessions) {
        return insert(authAuditRowMapper.toSessionsRevokedRow(actorUserId, targetUserId, reason, revokedSessions));
    }

    private Mono<Void> insert(com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.AuthAuditRow row) {
        return reactiveAuthAuditRepository
                .insert(
                        row.auditId(),
                        row.eventType(),
                        row.userId(),
                        row.sessionId(),
                        row.ipAddress(),
                        row.deviceId(),
                        row.result(),
                        row.payload(),
                        row.occurredAt())
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.<Void>empty()
                        : Mono.error(new IllegalStateException("Auth audit insert did not affect exactly one row")));
    }
}
