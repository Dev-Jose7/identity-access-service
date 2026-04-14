package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response;

import com.arka.identityaccess.application.result.AssignRoleResult;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.AssignRoleResponse;
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
