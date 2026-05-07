package com.arka.identityaccess.application.port.out.persistence;

import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import reactor.core.publisher.Mono;

public interface SessionPersistencePort {

    Mono<SessionAggregate> create(SessionAggregate session);

    Mono<SessionAggregate> update(SessionAggregate session);

    Mono<SessionAggregate> findActiveByRefreshJti(RefreshJti refreshJti);

    Mono<SessionAggregate> findBySessionId(SessionId sessionId);

    Mono<SessionAggregate> findOptionalBySessionId(SessionId sessionId);

    Mono<Long> revokeActiveSessionsByUserId(UserId userId, String reason, Instant revokedAt);
}
