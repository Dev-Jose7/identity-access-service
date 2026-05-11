package io.identityaccess.infrastructure.adapter.in.web.mapper.response;

import io.identityaccess.application.result.UserPermissionsResult;
import io.identityaccess.infrastructure.adapter.in.web.response.UserPermissionsResponse;
import org.springframework.stereotype.Component;

@Component
public class UserPermissionsResponseMapper {

    public UserPermissionsResponse toResponse(UserPermissionsResult result) {
        return new UserPermissionsResponse(
                result.userId(),
                result.roles(),
                result.permissions());
    }
}
