package com.arka.identityaccess.application.port.in;

import com.arka.identityaccess.application.command.RegisterCommand;
import com.arka.identityaccess.application.result.RegisterResult;
import reactor.core.publisher.Mono;

public interface RegisterCommandUseCase {
    Mono<RegisterResult> handle(RegisterCommand command);
}
