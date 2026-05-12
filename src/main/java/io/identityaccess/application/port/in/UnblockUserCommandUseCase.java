package io.identityaccess.application.port.in;

import io.identityaccess.application.command.UnblockUserCommand;
import io.identityaccess.application.result.UnblockUserResult;
import reactor.core.publisher.Mono;

public interface UnblockUserCommandUseCase {

    Mono<UnblockUserResult> handle(UnblockUserCommand command);
}
