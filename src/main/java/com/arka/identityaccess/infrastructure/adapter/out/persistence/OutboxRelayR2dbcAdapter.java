package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.application.port.out.persistence.OutboxRelayPort;
import com.arka.identityaccess.application.port.out.persistence.PendingOutboxEvent;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OutboxRelayR2dbcAdapter implements OutboxRelayPort {

    private final ReactiveOutboxEventRepository reactiveOutboxEventRepository;

    public OutboxRelayR2dbcAdapter(ReactiveOutboxEventRepository reactiveOutboxEventRepository) {
        this.reactiveOutboxEventRepository = reactiveOutboxEventRepository;
    }

    @Override
    public Flux<PendingOutboxEvent> findPending(int batchSize) {
        return reactiveOutboxEventRepository.findPending(batchSize).map(this::toPendingEvent);
    }

    @Override
    public Mono<Void> markPublished(String eventId, Instant publishedAt) {
        return reactiveOutboxEventRepository
                .markPublished(eventId, publishedAt)
                .flatMap(rowsUpdated -> rowsUpdated != null && rowsUpdated == 1
                        ? Mono.<Void>empty()
                        : Mono.error(new IllegalStateException("Outbox markPublished did not affect exactly one row")));
    }

    @Override
    public Mono<Void> markFailed(String eventId, String errorMessage, Instant failedAt, int maxRetries) {
        return reactiveOutboxEventRepository
                .markFailed(eventId, errorMessage, failedAt, Math.max(1, maxRetries))
                .flatMap(rowsUpdated -> rowsUpdated != null && rowsUpdated == 1
                        ? Mono.<Void>empty()
                        : Mono.error(new IllegalStateException("Outbox markFailed did not affect exactly one row")));
    }

    private PendingOutboxEvent toPendingEvent(OutboxEventRow row) {
        return new PendingOutboxEvent(
                row.eventId(),
                row.aggregateType(),
                row.aggregateId(),
                row.eventType(),
                row.payload(),
                row.retryCount() == null ? 0 : row.retryCount());
    }
}
