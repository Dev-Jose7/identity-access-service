package com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper;

import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OutboxRowMapper {

    private final ObjectMapper objectMapper;

    public OutboxRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEventRow toRow(DomainEvent event) {
        Instant now = Instant.now();
        return new OutboxEventRow(
                event.eventId(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                toPayload(event),
                "PENDING",
                event.occurredAt(),
                null,
                0,
                null,
                now,
                now);
    }

    private String toPayload(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize domain event payload", exception);
        }
    }
}
