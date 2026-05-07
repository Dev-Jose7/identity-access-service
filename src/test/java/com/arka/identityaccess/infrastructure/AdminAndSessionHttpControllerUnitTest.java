package com.arka.identityaccess.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.port.in.AssignRoleCommandUseCase;
import com.arka.identityaccess.application.port.in.BlockUserCommandUseCase;
import com.arka.identityaccess.application.port.in.GetUserPermissionsQueryUseCase;
import com.arka.identityaccess.application.port.in.RegisterCommandUseCase;
import com.arka.identityaccess.application.port.in.RevokeSessionsCommandUseCase;
import com.arka.identityaccess.application.result.AssignRoleResult;
import com.arka.identityaccess.application.result.BlockUserResult;
import com.arka.identityaccess.application.result.RevokeSessionsResult;
import com.arka.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.AdminIamHttpController;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.SessionAdminHttpController;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.AssignRoleCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.BlockUserCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.GetUserPermissionsQueryMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.RegisterCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.RevokeSessionsCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.AssignRoleResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.BlockUserResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.RegisterResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.RevokeSessionsResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.UserPermissionsResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.AssignRoleRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.BlockUserRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.RevokeSessionsRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.AssignRoleResponse;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.BlockUserResponse;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.RevokeSessionsResponse;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AdminAndSessionHttpControllerUnitTest {

    @Mock
    private RegisterCommandUseCase registerCommandUseCase;
    @Mock
    private AssignRoleCommandUseCase assignRoleCommandUseCase;
    @Mock
    private BlockUserCommandUseCase blockUserCommandUseCase;
    @Mock
    private GetUserPermissionsQueryUseCase getUserPermissionsQueryUseCase;
    @Mock
    private RevokeSessionsCommandUseCase revokeSessionsCommandUseCase;

    private AdminIamHttpController adminIamHttpController;
    private SessionAdminHttpController sessionAdminHttpController;

    @BeforeEach
    void setUp() {
        adminIamHttpController = new AdminIamHttpController(
                new RegisterCommandMapper(),
                registerCommandUseCase,
                new RegisterResponseMapper(),
                new AssignRoleCommandMapper(),
                assignRoleCommandUseCase,
                new AssignRoleResponseMapper(),
                new BlockUserCommandMapper(),
                blockUserCommandUseCase,
                new BlockUserResponseMapper(),
                new GetUserPermissionsQueryMapper(),
                getUserPermissionsQueryUseCase,
                new UserPermissionsResponseMapper());

        sessionAdminHttpController = new SessionAdminHttpController(
                new RevokeSessionsCommandMapper(),
                revokeSessionsCommandUseCase,
                new RevokeSessionsResponseMapper());
    }

    @Test
    void shouldMapAssignRoleEndpointUsingAuthenticatedPrincipal() {
        when(assignRoleCommandUseCase.handle(any()))
                .thenReturn(Mono.just(new AssignRoleResult("usr-9", "ORG_ADMIN", true, "ASSIGNED")));

        AssignRoleResponse response = adminIamHttpController
                .assignRole("usr-9", new AssignRoleRequest("ORG_ADMIN"), authenticated())
                .block();

        assertEquals("usr-9", response.userId());
        assertEquals("ORG_ADMIN", response.roleCode());
        assertEquals(true, response.assigned());
        assertEquals("ASSIGNED", response.status());
    }

    @Test
    void shouldMapBlockUserEndpointUsingAuthenticatedPrincipal() {
        when(blockUserCommandUseCase.handle(any()))
                .thenReturn(Mono.just(new BlockUserResult("usr-9", "BLOCKED", true)));

        BlockUserResponse response = adminIamHttpController
                .blockUser("usr-9", new BlockUserRequest("manual_block"), authenticated())
                .block();

        assertEquals("usr-9", response.userId());
        assertEquals("BLOCKED", response.status());
        assertEquals(true, response.changed());
    }

    @Test
    void shouldMapRevokeSessionsEndpointUsingAuthenticatedPrincipal() {
        when(revokeSessionsCommandUseCase.handle(any()))
                .thenReturn(Mono.just(new RevokeSessionsResult("usr-9", 2L, "ADMIN_REVOKE", "COMPLETED")));

        RevokeSessionsResponse response = sessionAdminHttpController
                .revokeSessions("usr-9", new RevokeSessionsRequest("ADMIN_REVOKE"), authenticated())
                .block();

        assertEquals("usr-9", response.userId());
        assertEquals(2L, response.revokedSessions());
        assertEquals("ADMIN_REVOKE", response.reason());
        assertEquals("COMPLETED", response.status());
    }

    @Test
    void shouldRejectUnsupportedPrincipalTypeForProtectedEndpoints() {
        Authentication unsupported = new UsernamePasswordAuthenticationToken("plain-string", "N/A");

        assertThrows(
                IllegalArgumentException.class,
                () -> adminIamHttpController.assignRole("usr-9", new AssignRoleRequest("ORG_ADMIN"), unsupported));
    }

    private Authentication authenticated() {
        IamSecurityPrincipal principal = new IamSecurityPrincipal(
                "actor-1",
                "ses-1",
                "owner@arka.com",
                Set.of("ORG_OWNER"));
        return new UsernamePasswordAuthenticationToken(principal, "N/A", java.util.List.of());
    }
}
