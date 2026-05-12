package io.identityaccess.application.port.out.event;

import reactor.core.publisher.Mono;

public interface DomainEventPublisherPort {

    Mono<Void> publish(String topic, String key, String payload);
}
