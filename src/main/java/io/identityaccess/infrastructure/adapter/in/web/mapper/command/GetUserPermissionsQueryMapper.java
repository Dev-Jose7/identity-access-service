package io.identityaccess.infrastructure.adapter.in.web.mapper.command;

import io.identityaccess.application.query.GetUserPermissionsQuery;
import org.springframework.stereotype.Component;

@Component
public class GetUserPermissionsQueryMapper {

    public GetUserPermissionsQuery toQuery(String userId) {
        return new GetUserPermissionsQuery(userId);
    }
}
