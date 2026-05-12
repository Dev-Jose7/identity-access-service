package io.identityaccess.application.port.in;

import io.identityaccess.application.command.LogoutCommand;
import io.identityaccess.application.result.LogoutResult;
import reactor.core.publisher.Mono;

public interface LogoutCommandUseCase {
    Mono<LogoutResult> handle(LogoutCommand command);
}
