package com.arka.identityaccess.application.port.out.persistence;

import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxRelayPort {

    Flux<PendingOutboxEvent> findPending(int batchSize);

    Mono<Void> markPublished(String eventId, Instant publishedAt);

    Mono<Void> markFailed(String eventId, String errorMessage, Instant failedAt, int maxRetries);
}
