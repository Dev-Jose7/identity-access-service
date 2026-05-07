package com.arka.identityaccess.application.port.in;

import com.arka.identityaccess.application.query.GetUserPermissionsQuery;
import com.arka.identityaccess.application.result.UserPermissionsResult;
import reactor.core.publisher.Mono;

public interface GetUserPermissionsQueryUseCase {

    Mono<UserPermissionsResult> handle(GetUserPermissionsQuery query);
}
