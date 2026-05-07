package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.query.GetUserPermissionsQuery;
import com.arka.identityaccess.application.result.UserPermissionsResult;
import com.arka.identityaccess.application.usecase.query.GetUserPermissionsUseCase;
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
                        "user@arka.com",
                        Set.of("ORG_ADMIN", "ORG_USER"),
                        Set.of("iam.user.read", "iam.user.create"))));

        UserPermissionsResult result = useCase.handle(new GetUserPermissionsQuery("usr-5")).block();

        assertEquals("usr-5", result.userId());
        assertEquals(Set.of("ORG_ADMIN", "ORG_USER"), result.roles());
        assertEquals(Set.of("iam.user.read", "iam.user.create"), result.permissions());
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
                        "user@arka.com",
                        Set.of(),
                        Set.of())));

        UserPermissionsResult result = useCase.handle(new GetUserPermissionsQuery("usr-empty")).block();

        assertEquals("usr-empty", result.userId());
        assertEquals(Set.of(), result.roles());
        assertEquals(Set.of(), result.permissions());
    }
}
