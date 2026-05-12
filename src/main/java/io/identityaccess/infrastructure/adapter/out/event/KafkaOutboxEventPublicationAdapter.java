package io.identityaccess.infrastructure.adapter.out.event;

import io.identityaccess.application.port.out.event.DomainEventPublisherPort;
import io.identityaccess.application.port.out.event.OutboxEventPublicationPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort.PendingOutboxEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaOutboxEventPublicationAdapter implements OutboxEventPublicationPort {

    private final DomainEventKafkaMapper domainEventKafkaMapper;
    private final DomainEventPublisherPort domainEventPublisherPort;

    public KafkaOutboxEventPublicationAdapter(
            DomainEventKafkaMapper domainEventKafkaMapper,
            DomainEventPublisherPort domainEventPublisherPort) {
        this.domainEventKafkaMapper = domainEventKafkaMapper;
        this.domainEventPublisherPort = domainEventPublisherPort;
    }

    @Override
    public Mono<Void> publish(PendingOutboxEvent event) {
        return domainEventPublisherPort.publish(
                domainEventKafkaMapper.topicFor(event),
                domainEventKafkaMapper.keyFor(event),
                domainEventKafkaMapper.payloadFor(event));
    }
}
