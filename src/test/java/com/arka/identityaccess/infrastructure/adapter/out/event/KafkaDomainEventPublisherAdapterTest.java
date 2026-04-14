package com.arka.identityaccess.infrastructure.adapter.out.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class KafkaDomainEventPublisherAdapterTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldPublishEventToKafka() {
        KafkaDomainEventPublisherAdapter adapter = new KafkaDomainEventPublisherAdapter(kafkaTemplate);
        when(kafkaTemplate.send(eq("topic-a"), eq("key-a"), eq("{\"ok\":true}")))
                .thenReturn(CompletableFuture.completedFuture(null));

        adapter.publish("topic-a", "key-a", "{\"ok\":true}").block();

        verify(kafkaTemplate).send(eq("topic-a"), eq("key-a"), eq("{\"ok\":true}"));
    }

    @Test
    void shouldPropagateKafkaFailure() {
        KafkaDomainEventPublisherAdapter adapter = new KafkaDomainEventPublisherAdapter(kafkaTemplate);
        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalStateException("kafka down"));

        when(kafkaTemplate.send(eq("topic-a"), eq("key-a"), eq("{\"ok\":true}")))
                .thenReturn(failedFuture);

        assertThrows(RuntimeException.class, () -> adapter.publish("topic-a", "key-a", "{\"ok\":true}").block());
    }
}
