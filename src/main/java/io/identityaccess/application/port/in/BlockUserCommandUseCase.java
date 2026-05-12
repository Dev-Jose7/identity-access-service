package io.identityaccess.application.port.in;

import io.identityaccess.application.command.BlockUserCommand;
import io.identityaccess.application.result.BlockUserResult;
import reactor.core.publisher.Mono;

public interface BlockUserCommandUseCase {

    Mono<BlockUserResult> handle(BlockUserCommand command);
}
