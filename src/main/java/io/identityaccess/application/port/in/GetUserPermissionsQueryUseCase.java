package io.identityaccess.application.port.in;

import io.identityaccess.application.query.GetUserPermissionsQuery;
import io.identityaccess.application.result.UserPermissionsResult;
import reactor.core.publisher.Mono;

public interface GetUserPermissionsQueryUseCase {

    Mono<UserPermissionsResult> handle(GetUserPermissionsQuery query);
}
