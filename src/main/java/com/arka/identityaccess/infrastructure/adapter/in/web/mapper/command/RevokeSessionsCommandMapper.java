package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command;

import com.arka.identityaccess.application.command.RevokeSessionsCommand;
import com.arka.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.RevokeSessionsRequest;
import org.springframework.stereotype.Component;

@Component
public class RevokeSessionsCommandMapper {

    public RevokeSessionsCommand toCommand(String userId, RevokeSessionsRequest request, IamSecurityPrincipal principal) {
        return new RevokeSessionsCommand(
                userId,
                principal.userId(),
                request.reason());
    }
}
