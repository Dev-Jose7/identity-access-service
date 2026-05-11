package io.identityaccess.application.port.in;

import io.identityaccess.application.command.AssignRoleCommand;
import io.identityaccess.application.result.AssignRoleResult;
import reactor.core.publisher.Mono;

public interface AssignRoleCommandUseCase {

    Mono<AssignRoleResult> handle(AssignRoleCommand command);
}
