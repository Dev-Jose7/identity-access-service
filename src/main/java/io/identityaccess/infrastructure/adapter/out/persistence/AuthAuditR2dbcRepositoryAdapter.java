package io.identityaccess.infrastructure.adapter.out.persistence;

import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.session.valueobject.ClientDevice;
import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.model.user.UserAggregate;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import io.identityaccess.domain.model.user.valueobject.UserId;
import io.identityaccess.infrastructure.adapter.out.persistence.mapper.AuthAuditRowMapper;
import io.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveAuthAuditRepository;
import java.time.Instant;
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
    public Mono<Void> recordLoginSuccess(UserAggregate user, SessionAggregate session) {
        return insert(authAuditRowMapper.toLoginSuccessRow(user, session));
    }

    @Override
    public Mono<Void> recordLoginFailure(
            EmailAddress email,
            UserId userId,
            ClientIp clientIp,
            ClientDevice clientDevice,
            String failureReason,
            Instant occurredAt) {
        return insert(authAuditRowMapper.toLoginFailureRow(
                email,
                userId,
                clientIp,
                clientDevice,
                failureReason,
                occurredAt));
    }

    @Override
    public Mono<Void> recordAccountRegistered(UserAggregate user) {
        return insert(authAuditRowMapper.toRegisterRow(user));
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
    public Mono<Void> recordRoleAssignedToAccount(String actorUserId, String targetUserId, String roleCode, boolean assigned) {
        return insert(authAuditRowMapper.toRoleAssignedToAccountRow(actorUserId, targetUserId, roleCode, assigned));
    }

    @Override
    public Mono<Void> recordAccountBlocked(String actorUserId, String targetUserId, String reason, boolean changed) {
        return insert(authAuditRowMapper.toAccountBlockedRow(actorUserId, targetUserId, reason, changed));
    }

    @Override
    public Mono<Void> recordAccountUnblocked(String actorUserId, String targetUserId, String reason, boolean changed) {
        return insert(authAuditRowMapper.toAccountUnblockedRow(actorUserId, targetUserId, reason, changed));
    }

    @Override
    public Mono<Void> recordSessionsRevoked(String actorUserId, String targetUserId, String reason, long revokedSessions) {
        return insert(authAuditRowMapper.toSessionsRevokedRow(actorUserId, targetUserId, reason, revokedSessions));
    }

    @Override
    public Mono<Void> recordAccessCatalogChanged(
            String actorUserId,
            String eventType,
            String targetType,
            String targetId,
            String targetCode,
            boolean changed) {
        return insert(authAuditRowMapper.toAccessCatalogChangedRow(
                actorUserId,
                eventType,
                targetType,
                targetId,
                targetCode,
                changed));
    }

    private Mono<Void> insert(io.identityaccess.infrastructure.adapter.out.persistence.entity.AuthAuditRow row) {
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
