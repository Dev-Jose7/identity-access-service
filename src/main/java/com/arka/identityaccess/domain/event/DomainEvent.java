package com.arka.identityaccess.domain.event;

import java.time.Instant;
import java.util.Map;

public interface DomainEvent {

    String eventId();

    String eventType();

    Instant occurredAt();

    String aggregateId();

    Map<String, Object> payload();

    default String eventVersion() {
        return "1.0.0";
    }
}
