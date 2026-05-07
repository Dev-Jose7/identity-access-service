package com.arka.identityaccess.application.port.in;

import com.arka.identityaccess.application.command.RefreshSessionCommand;
import com.arka.identityaccess.application.result.TokenPairResult;
import reactor.core.publisher.Mono;

public interface RefreshSessionCommandUseCase {
    Mono<TokenPairResult> handle(RefreshSessionCommand command);
}
