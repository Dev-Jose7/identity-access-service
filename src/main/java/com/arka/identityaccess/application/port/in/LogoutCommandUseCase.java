package com.arka.identityaccess.application.port.in;

import com.arka.identityaccess.application.command.LogoutCommand;
import com.arka.identityaccess.application.result.LogoutResult;
import reactor.core.publisher.Mono;

public interface LogoutCommandUseCase {
    Mono<LogoutResult> handle(LogoutCommand command);
}
