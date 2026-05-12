package io.identityaccess.application.port.out.persistence;

import io.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxPersistencePort {

    Mono<Void> store(DomainEvent event);

    Flux<PendingOutboxEvent> findPending(int batchSize, int maxRetries);

    Mono<Void> markPublished(String eventId, Instant publishedAt);

    Mono<Void> markFailed(String eventId, String lastError, int maxRetries, Instant failedAt);

    record PendingOutboxEvent(
            String eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload,
            int retryCount) {
    }
}
