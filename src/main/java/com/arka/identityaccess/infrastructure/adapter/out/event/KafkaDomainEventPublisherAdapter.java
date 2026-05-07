package com.arka.identityaccess.infrastructure.adapter.out.event;

import com.arka.identityaccess.application.port.out.event.DomainEventPublisherPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaDomainEventPublisherAdapter implements DomainEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaDomainEventPublisherAdapter(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> publish(String topic, String key, String payload) {
        return Mono.fromFuture(kafkaTemplate.send(topic, key, payload)).then();
    }
}
