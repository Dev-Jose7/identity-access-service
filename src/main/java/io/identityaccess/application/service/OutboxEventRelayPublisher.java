package io.identityaccess.application.service;

import io.identityaccess.application.port.out.event.OutboxEventPublicationPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort.PendingOutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OutboxEventRelayPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxEventRelayPublisher.class);

    private final OutboxPersistencePort outboxPersistencePort;
    private final OutboxEventPublicationPort outboxEventPublicationPort;
    private final ClockPort clockPort;

    public OutboxEventRelayPublisher(
            OutboxPersistencePort outboxPersistencePort,
            OutboxEventPublicationPort outboxEventPublicationPort,
            ClockPort clockPort) {
        this.outboxPersistencePort = outboxPersistencePort;
        this.outboxEventPublicationPort = outboxEventPublicationPort;
        this.clockPort = clockPort;
    }

    public Mono<Void> publishPending(int batchSize, int maxRetries) {
        int safeMaxRetries = Math.max(1, maxRetries);
        return outboxPersistencePort
                .findPending(batchSize, safeMaxRetries)
                .concatMap(event -> Mono.defer(() -> publish(event, safeMaxRetries))
                        .onErrorResume(throwable -> markFailed(event, throwable, safeMaxRetries)))
                .then();
    }

    public Mono<Void> publish(PendingOutboxEvent event, int maxRetries) {
        return outboxEventPublicationPort
                .publish(event)
                .then(Mono.defer(() -> outboxPersistencePort.markPublished(event.eventId(), clockPort.now())))
                .then();
    }

    private Mono<Void> markFailed(PendingOutboxEvent event, Throwable throwable, int maxRetries) {
        int nextRetryCount = event.retryCount() + 1;
        LOGGER.warn(
                "Outbox relay failed eventId={} eventType={} aggregateType={} aggregateId={} retryCount={} maxRetries={} error={}",
                event.eventId(),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                nextRetryCount,
                maxRetries,
                errorSummary(throwable),
                throwable);
        return outboxPersistencePort.markFailed(event.eventId(), errorSummary(throwable), maxRetries, clockPort.now())
                .onErrorResume(markFailure -> {
                    LOGGER.error(
                            "Outbox relay could not persist failure eventId={} eventType={} aggregateType={} aggregateId={} retryCount={} error={}",
                            event.eventId(),
                            event.eventType(),
                            event.aggregateType(),
                            event.aggregateId(),
                            nextRetryCount,
                            errorSummary(markFailure),
                            markFailure);
                    return Mono.empty();
                });
    }

    private String errorSummary(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getName();
        }
        return throwable.getClass().getName() + ": " + message;
    }
}
