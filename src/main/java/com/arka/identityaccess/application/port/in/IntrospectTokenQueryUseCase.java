package com.arka.identityaccess.application.port.in;

import com.arka.identityaccess.application.query.IntrospectTokenQuery;
import com.arka.identityaccess.application.result.IntrospectResult;
import reactor.core.publisher.Mono;

public interface IntrospectTokenQueryUseCase {

    Mono<IntrospectResult> handle(IntrospectTokenQuery query);
}
