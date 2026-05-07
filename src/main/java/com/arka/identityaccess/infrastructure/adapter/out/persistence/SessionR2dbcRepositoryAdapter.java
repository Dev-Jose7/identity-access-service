package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.domain.exception.SessionNotFoundException;
import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.SessionRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveSessionRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Component
public class SessionR2dbcRepositoryAdapter implements SessionPersistencePort {

    private final ReactiveSessionRepository reactiveSessionRepository;
    private final SessionRowMapper sessionRowMapper;

    public SessionR2dbcRepositoryAdapter(ReactiveSessionRepository reactiveSessionRepository, SessionRowMapper sessionRowMapper) {
        this.reactiveSessionRepository = reactiveSessionRepository;
        this.sessionRowMapper = sessionRowMapper;
    }

    @Override
    @Transactional
    public Mono<SessionAggregate> create(SessionAggregate session) {
        var row = sessionRowMapper.toRow(session);
        return reactiveSessionRepository
                .insert(
                        row.sessionId(),
                        row.userId(),
                        row.deviceId(),
                        row.deviceName(),
                        row.deviceType(),
                        row.ipAddress(),
                        row.accessJti(),
                        row.refreshJti(),
                        row.issuedAt(),
                        row.accessTokenExpiresAt(),
                        row.refreshTokenExpiresAt(),
                        row.lastSeenAt(),
                        row.status(),
                        row.revokedAt(),
                        row.revocationReason(),
                        row.createdAt(),
                        row.updatedAt())
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(session)
                        : Mono.error(new IllegalStateException("Session insert did not affect exactly one row")));
    }

    @Override
    @Transactional
    public Mono<SessionAggregate> update(SessionAggregate session) {
        var row = sessionRowMapper.toRow(session);
        return reactiveSessionRepository
                .update(
                        row.sessionId(),
                        row.accessJti(),
                        row.refreshJti(),
                        row.issuedAt(),
                        row.accessTokenExpiresAt(),
                        row.refreshTokenExpiresAt(),
                        row.lastSeenAt(),
                        row.status(),
                        row.revokedAt(),
                        row.revocationReason(),
                        Instant.now())
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(session)
                        : Mono.error(new SessionNotFoundException()));
    }

    @Override
    public Mono<SessionAggregate> findActiveByRefreshJti(RefreshJti refreshJti) {
        return reactiveSessionRepository.findActiveByRefreshJti(refreshJti.value()).map(sessionRowMapper::toAggregate).switchIfEmpty(Mono.error(new SessionNotFoundException()));
    }

    @Override
    public Mono<SessionAggregate> findBySessionId(SessionId sessionId) {
        return reactiveSessionRepository.findById(sessionId.value()).map(sessionRowMapper::toAggregate).switchIfEmpty(Mono.error(new SessionNotFoundException()));
    }

    @Override
    public Mono<SessionAggregate> findOptionalBySessionId(SessionId sessionId) {
        return reactiveSessionRepository.findById(sessionId.value()).map(sessionRowMapper::toAggregate);
    }

    @Override
    public Mono<Long> revokeActiveSessionsByUserId(UserId userId, String reason, Instant revokedAt) {
        String revocationReason = reason == null || reason.isBlank() ? "ADMIN_REVOKE" : reason.trim();
        return reactiveSessionRepository
                .revokeActiveByUserId(userId.value(), revocationReason, revokedAt, revokedAt)
                .map(Integer::longValue);
    }
}
