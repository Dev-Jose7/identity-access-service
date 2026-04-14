package com.arka.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("outbox_event")
public record OutboxEventRow(
        @Id @Column("event_id") String eventId,
        @Column("aggregate_type") String aggregateType,
        @Column("aggregate_id") String aggregateId,
        @Column("event_type") String eventType,
        @Column("payload") String payload,
        @Column("status") String status,
        @Column("occurred_at") Instant occurredAt,
        @Column("published_at") Instant publishedAt,
        @Column("retry_count") Integer retryCount,
        @Column("last_error") String lastError,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
