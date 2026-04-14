package com.arka.identityaccess.application.port.in;

import com.arka.identityaccess.application.command.AssignRoleCommand;
import com.arka.identityaccess.application.result.AssignRoleResult;
import reactor.core.publisher.Mono;

public interface AssignRoleCommandUseCase {

    Mono<AssignRoleResult> handle(AssignRoleCommand command);
}
