package io.identityaccess.infrastructure.adapter.in.web.mapper.response;

import io.identityaccess.application.result.AssignRoleResult;
import io.identityaccess.infrastructure.adapter.in.web.response.AssignRoleResponse;
import org.springframework.stereotype.Component;

@Component
public class AssignRoleResponseMapper {

    public AssignRoleResponse toResponse(AssignRoleResult result) {
        return new AssignRoleResponse(
                result.userId(),
                result.roleCode(),
                result.assigned(),
                result.status());
    }
}
