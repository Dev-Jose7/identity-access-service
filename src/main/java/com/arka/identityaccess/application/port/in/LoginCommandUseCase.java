package com.arka.identityaccess.application.port.in;

import com.arka.identityaccess.application.command.LoginCommand;
import com.arka.identityaccess.application.result.LoginResult;
import reactor.core.publisher.Mono;

public interface LoginCommandUseCase {

    Mono<LoginResult> handle(LoginCommand command);
}
