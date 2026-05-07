package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.domain.event.DomainEvent;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.OutboxRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import org.springframework.stereotype.Component;
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
    public Mono<OutboxEventRow> store(DomainEvent event) {
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
                        ? Mono.just(row)
                        : Mono.error(new IllegalStateException("Outbox insert did not affect exactly one row")));
    }
}
