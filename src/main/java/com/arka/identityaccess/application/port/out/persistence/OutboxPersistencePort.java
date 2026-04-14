package com.arka.identityaccess.application.port.out.persistence;

import com.arka.identityaccess.domain.shared.event.DomainEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxPersistencePort {

    Mono<Void> store(DomainEvent event);

    default Mono<Void> storeAll(Iterable<? extends DomainEvent> events) {
        if (events == null) {
            return Mono.empty();
        }
        return Flux.fromIterable(events).concatMap(this::store).then();
    }
}
