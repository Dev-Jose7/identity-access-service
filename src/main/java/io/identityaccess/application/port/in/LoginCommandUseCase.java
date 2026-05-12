package io.identityaccess.application.port.in;

import io.identityaccess.application.command.LoginCommand;
import io.identityaccess.application.result.LoginResult;
import reactor.core.publisher.Mono;

public interface LoginCommandUseCase {

    Mono<LoginResult> handle(LoginCommand command);
}
