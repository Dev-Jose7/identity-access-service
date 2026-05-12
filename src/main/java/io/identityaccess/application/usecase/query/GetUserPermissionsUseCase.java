package io.identityaccess.application.usecase.query;

import io.identityaccess.application.query.GetUserPermissionsQuery;
import io.identityaccess.application.port.in.GetUserPermissionsQueryUseCase;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.UserPermissionsResult;
import io.identityaccess.domain.model.user.valueobject.UserId;
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
