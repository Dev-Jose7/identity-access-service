package io.identityaccess.application.port.out.event;

import io.identityaccess.application.port.out.persistence.OutboxPersistencePort.PendingOutboxEvent;
import reactor.core.publisher.Mono;

public interface OutboxEventPublicationPort {

    Mono<Void> publish(PendingOutboxEvent event);
}
