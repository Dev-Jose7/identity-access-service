package com.arka.identityaccess.application.usecase.query;

import com.arka.identityaccess.application.query.GetUserPermissionsQuery;
import com.arka.identityaccess.application.port.in.GetUserPermissionsQueryUseCase;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.UserPermissionsResult;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetUserPermissionsUseCase implements GetUserPermissionsQueryUseCase {

    private final UserPersistencePort userPersistencePort;

    public GetUserPermissionsUseCase(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Mono<UserPermissionsResult> handle(GetUserPermissionsQuery query) {
        UserId userId = UserId.of(query.userId());
        return userPersistencePort.loadAuthorizationSnapshot(userId)
                .map(snapshot -> new UserPermissionsResult(
                        userId.value(),
                        snapshot.roles(),
                        snapshot.permissions()));
    }
}
