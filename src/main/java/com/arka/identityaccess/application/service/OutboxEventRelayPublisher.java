package com.arka.identityaccess.application.service;

import com.arka.identityaccess.application.port.out.event.DomainEventPublisherPort;
import com.arka.identityaccess.application.port.out.event.DomainEventTopicPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxRelayPort;
import com.arka.identityaccess.application.port.out.persistence.PendingOutboxEvent;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OutboxEventRelayPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventRelayPublisher.class);

    private final OutboxRelayPort outboxRelayPort;
    private final DomainEventTopicPort domainEventTopicPort;
    private final DomainEventPublisherPort domainEventPublisherPort;
    private final int maxRetries;

    public OutboxEventRelayPublisher(
            OutboxRelayPort outboxRelayPort,
            DomainEventTopicPort domainEventTopicPort,
            DomainEventPublisherPort domainEventPublisherPort,
            @Value("${app.outbox.relay.max-retries:3}") int maxRetries) {
        this.outboxRelayPort = outboxRelayPort;
        this.domainEventTopicPort = domainEventTopicPort;
        this.domainEventPublisherPort = domainEventPublisherPort;
        this.maxRetries = Math.max(1, maxRetries);
    }

    public Mono<Void> publishPending(int batchSize) {
        return outboxRelayPort
                .findPending(batchSize)
                .concatMap(this::publish)
                .then();
    }

    public Mono<Void> publish(PendingOutboxEvent row) {
        return Mono.defer(() -> {
                    String topic = domainEventTopicPort.topicFor(row.eventType());
                    return domainEventPublisherPort
                            .publish(
                                    topic,
                                    row.aggregateId(),
                                    row.payload())
                            .then(Mono.defer(() -> outboxRelayPort.markPublished(row.eventId(), Instant.now())));
                })
                .onErrorResume(throwable -> handlePublishFailure(row, throwable));
    }

    private Mono<Void> handlePublishFailure(PendingOutboxEvent row, Throwable throwable) {
        String errorMessage = truncateError(throwable);
        log.error(
                "Outbox relay publish failed: eventId={}, eventType={}, aggregateType={}, aggregateId={}, retryCount={}, error={}",
                row.eventId(),
                row.eventType(),
                row.aggregateType(),
                row.aggregateId(),
                row.retryCount(),
                errorMessage,
                throwable);

        return outboxRelayPort
                .markFailed(row.eventId(), errorMessage, Instant.now(), maxRetries)
                .onErrorResume(persistenceError -> {
                    log.error(
                            "Outbox relay failed to persist error state: eventId={}, eventType={}, persistenceError={}",
                            row.eventId(),
                            row.eventType(),
                            persistenceError.getMessage(),
                            persistenceError);
                    return Mono.empty();
                });
    }

    private String truncateError(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        String trimmed = message.trim();
        int maxLength = 2000;
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
