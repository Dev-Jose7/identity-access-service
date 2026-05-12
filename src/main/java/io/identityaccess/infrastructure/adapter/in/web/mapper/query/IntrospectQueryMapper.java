package io.identityaccess.infrastructure.adapter.in.web.mapper.query;

import io.identityaccess.application.query.IntrospectTokenQuery;
import io.identityaccess.infrastructure.adapter.in.web.request.IntrospectRequest;
import org.springframework.stereotype.Component;

@Component
public class IntrospectQueryMapper {

    public IntrospectTokenQuery toQuery(IntrospectRequest request) {
        String rawToken = request.token() == null ? "" : request.token().trim();
        if (rawToken.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            rawToken = rawToken.substring("Bearer ".length()).trim();
        }
        return new IntrospectTokenQuery(rawToken);
    }
}
