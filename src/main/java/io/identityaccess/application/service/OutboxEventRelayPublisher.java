package io.identityaccess.application.service;

import io.identityaccess.application.port.out.event.DomainEventPublisherPort;
import io.identityaccess.infrastructure.adapter.out.event.DomainEventKafkaMapper;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import io.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OutboxEventRelayPublisher {

    private final ReactiveOutboxEventRepository reactiveOutboxEventRepository;
    private final DomainEventKafkaMapper domainEventKafkaMapper;
    private final DomainEventPublisherPort domainEventPublisherPort;

    public OutboxEventRelayPublisher(
            ReactiveOutboxEventRepository reactiveOutboxEventRepository,
            DomainEventKafkaMapper domainEventKafkaMapper,
            DomainEventPublisherPort domainEventPublisherPort) {
        this.reactiveOutboxEventRepository = reactiveOutboxEventRepository;
        this.domainEventKafkaMapper = domainEventKafkaMapper;
        this.domainEventPublisherPort = domainEventPublisherPort;
    }

    public Mono<Void> publishPending(int batchSize) {
        return reactiveOutboxEventRepository
                .findPending(batchSize)
                .concatMap(this::publish)
                .then();
    }

    public Mono<Void> publish(OutboxEventRow row) {
        return domainEventPublisherPort
                .publish(
                        domainEventKafkaMapper.topicFor(row),
                        domainEventKafkaMapper.keyFor(row),
                        domainEventKafkaMapper.payloadFor(row))
                .then(reactiveOutboxEventRepository.markPublished(row.eventId(), Instant.now()))
                .then();
    }
}
