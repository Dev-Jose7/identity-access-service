package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command;

import com.arka.identityaccess.application.command.AssignRoleCommand;
import com.arka.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.AssignRoleRequest;
import org.springframework.stereotype.Component;

@Component
public class AssignRoleCommandMapper {

    public AssignRoleCommand toCommand(String userId, AssignRoleRequest request, IamSecurityPrincipal principal) {
        return new AssignRoleCommand(
                userId,
                request.roleCode(),
                principal.userId());
    }
}
