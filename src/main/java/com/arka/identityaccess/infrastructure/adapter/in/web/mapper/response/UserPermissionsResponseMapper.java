package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response;

import com.arka.identityaccess.application.result.UserPermissionsResult;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.UserPermissionsResponse;
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
