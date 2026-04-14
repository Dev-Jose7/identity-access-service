package com.arka.identityaccess.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.port.out.event.DomainEventPublisherPort;
import com.arka.identityaccess.application.port.out.event.DomainEventTopicPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxRelayPort;
import com.arka.identityaccess.application.port.out.persistence.PendingOutboxEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayPublisherTest {

    @Mock
    private OutboxRelayPort outboxRelayPort;

    @Mock
    private DomainEventTopicPort domainEventTopicPort;

    @Mock
    private DomainEventPublisherPort domainEventPublisherPort;

    @Test
    void shouldMarkPublishedWhenKafkaPublishSucceeds() {
        OutboxEventRelayPublisher publisher = new OutboxEventRelayPublisher(
                outboxRelayPort,
                domainEventTopicPort,
                domainEventPublisherPort,
                3);

        PendingOutboxEvent pending = new PendingOutboxEvent(
                "evt-1",
                "User",
                "usr-1",
                "UserLoggedIn",
                "{\"ok\":true}",
                0);

        when(domainEventTopicPort.topicFor("UserLoggedIn")).thenReturn("iam.user-logged-in.v1");
        when(domainEventPublisherPort.publish(any(), any(), any())).thenReturn(Mono.empty());
        when(outboxRelayPort.markPublished(eq("evt-1"), any())).thenReturn(Mono.empty());

        publisher.publish(pending).block();

        verify(outboxRelayPort).markPublished(eq("evt-1"), any());
        verify(outboxRelayPort, never()).markFailed(any(), any(), any(), anyInt());
    }

    @Test
    void shouldPersistFailureWhenKafkaPublishFails() {
        OutboxEventRelayPublisher publisher = new OutboxEventRelayPublisher(
                outboxRelayPort,
                domainEventTopicPort,
                domainEventPublisherPort,
                5);

        PendingOutboxEvent pending = new PendingOutboxEvent(
                "evt-2",
                "Session",
                "11111111-1111-1111-1111-111111111111",
                "SessionRefreshed",
                "{\"ok\":true}",
                1);

        when(domainEventTopicPort.topicFor("SessionRefreshed")).thenReturn("iam.session-refreshed.v1");
        when(domainEventPublisherPort.publish(any(), any(), any()))
                .thenReturn(Mono.error(new IllegalStateException("broker unavailable")));
        when(outboxRelayPort.markFailed(eq("evt-2"), any(), any(), eq(5))).thenReturn(Mono.empty());

        publisher.publish(pending).block();

        verify(outboxRelayPort).markFailed(eq("evt-2"), any(), any(), eq(5));
        verify(outboxRelayPort, never()).markPublished(eq("evt-2"), any());
    }

    @Test
    void shouldPersistFailureWhenTopicMappingFails() {
        OutboxEventRelayPublisher publisher = new OutboxEventRelayPublisher(
                outboxRelayPort,
                domainEventTopicPort,
                domainEventPublisherPort,
                3);

        PendingOutboxEvent pending = new PendingOutboxEvent(
                "evt-3",
                "User",
                "usr-9",
                "UnknownEventType",
                "{\"ok\":true}",
                2);

        when(domainEventTopicPort.topicFor("UnknownEventType"))
                .thenThrow(new IllegalStateException("No Kafka topic mapping defined"));
        when(outboxRelayPort.markFailed(eq("evt-3"), any(), any(), eq(3))).thenReturn(Mono.empty());

        publisher.publish(pending).block();

        verify(outboxRelayPort).markFailed(eq("evt-3"), any(), any(), eq(3));
        verify(domainEventPublisherPort, never()).publish(any(), any(), any());
    }
}
