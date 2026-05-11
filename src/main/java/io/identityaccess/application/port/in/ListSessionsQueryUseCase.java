package io.identityaccess.application.port.in;

import io.identityaccess.application.result.SessionSummaryResult;
import reactor.core.publisher.Flux;

public interface ListSessionsQueryUseCase {

    Flux<SessionSummaryResult> handle();
}
