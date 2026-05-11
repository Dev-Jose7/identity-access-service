package io.identityaccess.infrastructure.adapter.out.persistence;

import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.domain.exception.SessionNotFoundException;
import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.session.valueobject.RefreshJti;
import io.identityaccess.domain.model.session.valueobject.SessionId;
import io.identityaccess.domain.model.user.valueobject.UserId;
import io.identityaccess.infrastructure.adapter.out.persistence.mapper.SessionRowMapper;
import io.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveSessionRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
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
    public Flux<SessionRecord> listSessions() {
        return reactiveSessionRepository.findAllOrderedByIssuedAtDesc()
                .map(row -> new SessionRecord(
                        row.sessionId(),
                        row.userId(),
                        row.status(),
                        row.ipAddress(),
                        row.deviceId(),
                        row.deviceName(),
                        row.deviceType(),
                        row.issuedAt(),
                        row.accessTokenExpiresAt(),
                        row.refreshTokenExpiresAt(),
                        row.lastSeenAt(),
                        row.revokedAt(),
                        row.revocationReason(),
                        row.createdAt(),
                        row.updatedAt()));
    }

    @Override
    public Mono<Long> revokeActiveSessionsByUserId(UserId userId, String reason, Instant revokedAt) {
        String revocationReason = reason == null || reason.isBlank() ? "ADMIN_REVOKE" : reason.trim();
        return reactiveSessionRepository
                .revokeActiveByUserId(userId.value(), revocationReason, revokedAt, revokedAt)
                .map(Integer::longValue);
    }

    @Override
    public Mono<Long> expireExpiredSessions(Instant now) {
        return reactiveSessionRepository
                .expireExpiredSessions(now, now)
                .map(Integer::longValue);
    }
}
