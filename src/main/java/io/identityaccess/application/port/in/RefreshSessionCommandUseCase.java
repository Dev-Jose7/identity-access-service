package io.identityaccess.application.port.in;

import io.identityaccess.application.command.RefreshSessionCommand;
import io.identityaccess.application.result.TokenPairResult;
import reactor.core.publisher.Mono;

public interface RefreshSessionCommandUseCase {
    Mono<TokenPairResult> handle(RefreshSessionCommand command);
}
