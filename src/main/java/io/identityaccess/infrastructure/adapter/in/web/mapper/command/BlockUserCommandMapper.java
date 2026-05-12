package io.identityaccess.infrastructure.adapter.in.web.mapper.command;

import io.identityaccess.application.command.BlockUserCommand;
import io.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import io.identityaccess.infrastructure.adapter.in.web.request.BlockUserRequest;
import org.springframework.stereotype.Component;

@Component
public class BlockUserCommandMapper {

    public BlockUserCommand toCommand(String userId, BlockUserRequest request, IamSecurityPrincipal principal) {
        return new BlockUserCommand(
                userId,
                principal.userId(),
                request.reason());
    }
}
