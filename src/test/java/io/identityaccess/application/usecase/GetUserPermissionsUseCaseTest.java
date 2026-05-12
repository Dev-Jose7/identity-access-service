package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.query.GetUserPermissionsQuery;
import io.identityaccess.application.result.UserPermissionsResult;
import io.identityaccess.application.usecase.query.GetUserPermissionsUseCase;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GetUserPermissionsUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Test
    void shouldReturnEffectiveRolesAndPermissions() {
        GetUserPermissionsUseCase useCase = new GetUserPermissionsUseCase(userPersistencePort);

        when(userPersistencePort.loadAuthorizationSnapshot(any()))
                .thenReturn(Mono.just(new UserPersistencePort.AuthorizationSnapshot(
                        "user@example.test",
                        Set.of("ACCESS_ADMIN", "ACCOUNT_USER"),
                        Set.of("iam.account.read", "iam.account.create"))));

        UserPermissionsResult result = useCase.handle(new GetUserPermissionsQuery("usr-5")).block();

        assertEquals("usr-5", result.userId());
        assertEquals(Set.of("ACCESS_ADMIN", "ACCOUNT_USER"), result.roles());
        assertEquals(Set.of("iam.account.read", "iam.account.create"), result.permissions());
    }

    @Test
    void shouldRejectBlankUserId() {
        GetUserPermissionsUseCase useCase = new GetUserPermissionsUseCase(userPersistencePort);

        assertThrows(IllegalArgumentException.class, () -> useCase.handle(new GetUserPermissionsQuery(" ")));
    }

    @Test
    void shouldReturnEmptyPermissionsWhenUserHasNoAssignments() {
        GetUserPermissionsUseCase useCase = new GetUserPermissionsUseCase(userPersistencePort);

        when(userPersistencePort.loadAuthorizationSnapshot(any()))
                .thenReturn(Mono.just(new UserPersistencePort.AuthorizationSnapshot(
                        "user@example.test",
                        Set.of(),
                        Set.of())));

        UserPermissionsResult result = useCase.handle(new GetUserPermissionsQuery("usr-empty")).block();

        assertEquals("usr-empty", result.userId());
        assertEquals(Set.of(), result.roles());
        assertEquals(Set.of(), result.permissions());
    }
}
