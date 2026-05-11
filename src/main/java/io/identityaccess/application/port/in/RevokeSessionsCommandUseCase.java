package io.identityaccess.application.port.in;

import io.identityaccess.application.command.RevokeSessionsCommand;
import io.identityaccess.application.result.RevokeSessionsResult;
import reactor.core.publisher.Mono;

public interface RevokeSessionsCommandUseCase {

    Mono<RevokeSessionsResult> handle(RevokeSessionsCommand command);
}
