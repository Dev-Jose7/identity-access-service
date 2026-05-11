package io.identityaccess.infrastructure.adapter.out.persistence.repository;

import io.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveOutboxEventRepository extends ReactiveCrudRepository<OutboxEventRow, String> {

    @Modifying
    @Query("""
            INSERT INTO outbox_event (
                event_id,
                aggregate_type,
                aggregate_id,
                event_type,
                payload,
                status,
                occurred_at,
                published_at,
                retry_count,
                last_error,
                created_at,
                updated_at
            ) VALUES (
                :eventId,
                :aggregateType,
                :aggregateId,
                :eventType,
                CAST(:payload AS jsonb),
                :status,
                :occurredAt,
                :publishedAt,
                :retryCount,
                :lastError,
                :createdAt,
                :updatedAt
            )
            """)
    Mono<Integer> insert(
            @Param("eventId") String eventId,
            @Param("aggregateType") String aggregateType,
            @Param("aggregateId") String aggregateId,
            @Param("eventType") String eventType,
            @Param("payload") String payload,
            @Param("status") String status,
            @Param("occurredAt") Instant occurredAt,
            @Param("publishedAt") Instant publishedAt,
            @Param("retryCount") Integer retryCount,
            @Param("lastError") String lastError,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt);

    @Query("""
            SELECT event_id, aggregate_type, aggregate_id, event_type, payload, status,
                   occurred_at, published_at, retry_count, last_error, created_at, updated_at
            FROM outbox_event
            WHERE status = 'PENDING'
            ORDER BY occurred_at ASC
            LIMIT :batchSize
            """)
    Flux<OutboxEventRow> findPending(@Param("batchSize") int batchSize);

    @Modifying
    @Query("""
            UPDATE outbox_event
            SET status = 'PUBLISHED', published_at = :publishedAt, updated_at = :publishedAt
            WHERE event_id = :eventId
            """)
    Mono<Integer> markPublished(@Param("eventId") String eventId, @Param("publishedAt") Instant publishedAt);
}
