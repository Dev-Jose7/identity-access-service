package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command;

import com.arka.identityaccess.application.query.GetUserPermissionsQuery;
import org.springframework.stereotype.Component;

@Component
public class GetUserPermissionsQueryMapper {

    public GetUserPermissionsQuery toQuery(String userId) {
        return new GetUserPermissionsQuery(userId);
    }
}
