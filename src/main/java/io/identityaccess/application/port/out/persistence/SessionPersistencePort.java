package io.identityaccess.application.port.out.persistence;

import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.session.valueobject.RefreshJti;
import io.identityaccess.domain.model.session.valueobject.SessionId;
import io.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SessionPersistencePort {

    Mono<SessionAggregate> create(SessionAggregate session);

    Mono<SessionAggregate> update(SessionAggregate session);

    Mono<SessionAggregate> findActiveByRefreshJti(RefreshJti refreshJti);

    Mono<SessionAggregate> findBySessionId(SessionId sessionId);

    Mono<SessionAggregate> findOptionalBySessionId(SessionId sessionId);

    Flux<SessionRecord> listSessions();

    Mono<Long> revokeActiveSessionsByUserId(UserId userId, String reason, Instant revokedAt);

    Mono<Long> expireExpiredSessions(Instant now);

    record SessionRecord(
            String sessionId,
            String userId,
            String status,
            String ipAddress,
            String deviceId,
            String deviceName,
            String deviceType,
            Instant issuedAt,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt,
            Instant lastSeenAt,
            Instant revokedAt,
            String revocationReason,
            Instant createdAt,
            Instant updatedAt) {}
}
