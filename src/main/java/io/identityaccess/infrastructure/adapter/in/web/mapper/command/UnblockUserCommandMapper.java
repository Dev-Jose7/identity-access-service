package io.identityaccess.infrastructure.adapter.in.web.mapper.command;

import io.identityaccess.application.command.UnblockUserCommand;
import io.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import io.identityaccess.infrastructure.adapter.in.web.request.UnblockUserRequest;
import org.springframework.stereotype.Component;

@Component
public class UnblockUserCommandMapper {

    public UnblockUserCommand toCommand(String userId, UnblockUserRequest request, IamSecurityPrincipal principal) {
        return new UnblockUserCommand(
                userId,
                principal.userId(),
                request.reason());
    }
}
