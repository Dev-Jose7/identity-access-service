package com.arka.identityaccess.application.port.out.persistence;

import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SessionPersistencePort {

    Mono<SessionAggregate> create(SessionAggregate session);

    Mono<SessionAggregate> update(SessionAggregate session);

    Mono<SessionAggregate> findActiveByRefreshJti(RefreshJti refreshJti);

    Mono<SessionAggregate> findBySessionId(SessionId sessionId);

    Mono<SessionAggregate> findOptionalBySessionId(SessionId sessionId);

    Flux<SessionAggregate> revokeActiveSessionsByUserId(AccountId userId, String reason, Instant revokedAt);
}
