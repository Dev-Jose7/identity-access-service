package com.arka.identityaccess.application.port.in;

import com.arka.identityaccess.application.command.RevokeSessionsCommand;
import com.arka.identityaccess.application.result.RevokeSessionsResult;
import reactor.core.publisher.Mono;

public interface RevokeSessionsCommandUseCase {

    Mono<RevokeSessionsResult> handle(RevokeSessionsCommand command);
}
