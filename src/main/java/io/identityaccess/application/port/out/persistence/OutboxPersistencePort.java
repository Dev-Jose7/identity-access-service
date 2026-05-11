package io.identityaccess.application.port.out.persistence;

import io.identityaccess.domain.event.DomainEvent;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import reactor.core.publisher.Mono;

public interface OutboxPersistencePort {

    Mono<OutboxEventRow> store(DomainEvent event);
}
