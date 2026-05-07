package com.arka.identityaccess.application.port.in;

import com.arka.identityaccess.application.command.BlockUserCommand;
import com.arka.identityaccess.application.result.BlockUserResult;
import reactor.core.publisher.Mono;

public interface BlockUserCommandUseCase {

    Mono<BlockUserResult> handle(BlockUserCommand command);
}
