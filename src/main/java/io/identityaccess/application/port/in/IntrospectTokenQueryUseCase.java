package io.identityaccess.application.port.in;

import io.identityaccess.application.query.IntrospectTokenQuery;
import io.identityaccess.application.result.IntrospectResult;
import reactor.core.publisher.Mono;

public interface IntrospectTokenQueryUseCase {

    Mono<IntrospectResult> handle(IntrospectTokenQuery query);
}
