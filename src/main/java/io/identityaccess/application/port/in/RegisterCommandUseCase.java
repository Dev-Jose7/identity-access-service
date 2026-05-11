package io.identityaccess.application.port.in;

import io.identityaccess.application.command.RegisterCommand;
import io.identityaccess.application.result.RegisterResult;
import reactor.core.publisher.Mono;

public interface RegisterCommandUseCase {
    Mono<RegisterResult> handle(RegisterCommand command);
}
