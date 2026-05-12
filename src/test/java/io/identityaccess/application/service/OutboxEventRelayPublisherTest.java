package io.identityaccess.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.identityaccess.application.port.out.event.OutboxEventPublicationPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort.PendingOutboxEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayPublisherTest {

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private OutboxEventPublicationPort outboxEventPublicationPort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldPublishPendingEventsAndMarkThemAsPublished() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        PendingOutboxEvent event = event("evt-1", "AccountRegistered", 0);
        OutboxEventRelayPublisher publisher = new OutboxEventRelayPublisher(
                outboxPersistencePort,
                outboxEventPublicationPort,
                clockPort);

        when(outboxPersistencePort.findPending(10, 3)).thenReturn(Flux.just(event));
        when(outboxEventPublicationPort.publish(event)).thenReturn(Mono.empty());
        when(clockPort.now()).thenReturn(now);
        when(outboxPersistencePort.markPublished("evt-1", now)).thenReturn(Mono.empty());

        StepVerifier.create(publisher.publishPending(10, 3))
                .verifyComplete();

        verify(outboxPersistencePort).markPublished("evt-1", now);
    }

    @Test
    void shouldMarkFailedAndContinueWhenPublicationFails() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        PendingOutboxEvent event = event("evt-2", "UnknownEvent", 1);
        OutboxEventRelayPublisher publisher = new OutboxEventRelayPublisher(
                outboxPersistencePort,
                outboxEventPublicationPort,
                clockPort);

        when(outboxPersistencePort.findPending(10, 3)).thenReturn(Flux.just(event));
        when(outboxEventPublicationPort.publish(event)).thenReturn(Mono.error(new IllegalStateException("no topic")));
        when(clockPort.now()).thenReturn(now);
        when(outboxPersistencePort.markFailed(eq("evt-2"), any(), eq(3), eq(now))).thenReturn(Mono.empty());

        StepVerifier.create(publisher.publishPending(10, 3))
                .verifyComplete();

        verify(outboxPersistencePort).markFailed(eq("evt-2"), eq("java.lang.IllegalStateException: no topic"), eq(3), eq(now));
    }

    private PendingOutboxEvent event(String eventId, String eventType, int retryCount) {
        return new PendingOutboxEvent(
                eventId,
                "Account",
                "acc-1",
                eventType,
                "{}",
                retryCount);
    }
}
