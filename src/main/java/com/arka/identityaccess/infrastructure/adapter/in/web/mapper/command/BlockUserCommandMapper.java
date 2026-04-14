package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command;

import com.arka.identityaccess.application.command.BlockUserCommand;
import com.arka.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.BlockUserRequest;
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
