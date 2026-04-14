package com.arka.identityaccess.application.port.out.persistence;

public record PendingOutboxEvent(
        String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        int retryCount) {}
