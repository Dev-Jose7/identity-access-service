package com.arka.identityaccess.application.port.out.persistence;

import com.arka.identityaccess.domain.event.DomainEvent;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import reactor.core.publisher.Mono;

public interface OutboxPersistencePort {

    Mono<OutboxEventRow> store(DomainEvent event);
}
