package io.identityaccess.infrastructure.adapter.out.persistence;

import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import io.identityaccess.infrastructure.adapter.out.persistence.mapper.OutboxRowMapper;
import io.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OutboxPersistenceAdapter implements OutboxPersistencePort {

    private final ReactiveOutboxEventRepository reactiveOutboxEventRepository;
    private final OutboxRowMapper outboxRowMapper;

    public OutboxPersistenceAdapter(
            ReactiveOutboxEventRepository reactiveOutboxEventRepository,
            OutboxRowMapper outboxRowMapper) {
        this.reactiveOutboxEventRepository = reactiveOutboxEventRepository;
        this.outboxRowMapper = outboxRowMapper;
    }

    @Override
    public Mono<Void> store(DomainEvent event) {
        OutboxEventRow row = outboxRowMapper.toRow(event);
        return reactiveOutboxEventRepository
                .insert(
                        row.eventId(),
                        row.aggregateType(),
                        row.aggregateId(),
                        row.eventType(),
                        row.payload(),
                        row.status(),
                        row.occurredAt(),
                        row.publishedAt(),
                        row.retryCount(),
                        row.lastError(),
                        row.createdAt(),
                        row.updatedAt())
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.empty()
                        : Mono.error(new IllegalStateException("Outbox insert did not affect exactly one row")));
    }

    @Override
    public Flux<PendingOutboxEvent> findPending(int batchSize, int maxRetries) {
        return reactiveOutboxEventRepository
                .findPending(batchSize, maxRetries)
                .map(outboxRowMapper::toPendingEvent);
    }

    @Override
    public Mono<Void> markPublished(String eventId, Instant publishedAt) {
        return reactiveOutboxEventRepository
                .markPublished(eventId, publishedAt)
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.empty()
                        : Mono.error(new IllegalStateException("Outbox published update did not affect exactly one row")));
    }

    @Override
    public Mono<Void> markFailed(String eventId, String lastError, int maxRetries, Instant failedAt) {
        return reactiveOutboxEventRepository
                .markFailed(eventId, truncate(lastError), maxRetries, failedAt)
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.empty()
                        : Mono.error(new IllegalStateException("Outbox failure update did not affect exactly one row")));
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 2000 ? value : value.substring(0, 2000);
    }
}
