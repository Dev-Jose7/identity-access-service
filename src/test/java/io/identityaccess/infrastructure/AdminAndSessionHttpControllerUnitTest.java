package io.identityaccess.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.identityaccess.application.port.in.AssignRoleCommandUseCase;
import io.identityaccess.application.port.in.BlockUserCommandUseCase;
import io.identityaccess.application.port.in.GetUserPermissionsQueryUseCase;
import io.identityaccess.application.port.in.ListAccountsQueryUseCase;
import io.identityaccess.application.port.in.ListSessionsQueryUseCase;
import io.identityaccess.application.port.in.RegisterCommandUseCase;
import io.identityaccess.application.port.in.RevokeSessionsCommandUseCase;
import io.identityaccess.application.port.in.UnblockUserCommandUseCase;
import io.identityaccess.application.result.AccountSummaryResult;
import io.identityaccess.application.result.AssignRoleResult;
import io.identityaccess.application.result.BlockUserResult;
import io.identityaccess.application.result.RevokeSessionsResult;
import io.identityaccess.application.result.SessionSummaryResult;
import io.identityaccess.application.result.UnblockUserResult;
import io.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import io.identityaccess.infrastructure.adapter.in.web.controller.AdminIamHttpController;
import io.identityaccess.infrastructure.adapter.in.web.controller.SessionAdminHttpController;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.AssignRoleCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.BlockUserCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.GetUserPermissionsQueryMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.RegisterCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.RevokeSessionsCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.UnblockUserCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.AssignRoleResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.BlockUserResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.AccountSummaryResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.RegisterResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.RevokeSessionsResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.SessionSummaryResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.UnblockUserResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.UserPermissionsResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.request.AssignRoleRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.BlockUserRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.RevokeSessionsRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.UnblockUserRequest;
import io.identityaccess.infrastructure.adapter.in.web.response.AccountSummaryResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.AssignRoleResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.BlockUserResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.RevokeSessionsResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.SessionSummaryResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.UnblockUserResponse;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AdminAndSessionHttpControllerUnitTest {

    @Mock
    private RegisterCommandUseCase registerCommandUseCase;
    @Mock
    private ListAccountsQueryUseCase listAccountsQueryUseCase;
    @Mock
    private AssignRoleCommandUseCase assignRoleCommandUseCase;
    @Mock
    private BlockUserCommandUseCase blockUserCommandUseCase;
    @Mock
    private UnblockUserCommandUseCase unblockUserCommandUseCase;
    @Mock
    private GetUserPermissionsQueryUseCase getUserPermissionsQueryUseCase;
    @Mock
    private RevokeSessionsCommandUseCase revokeSessionsCommandUseCase;
    @Mock
    private ListSessionsQueryUseCase listSessionsQueryUseCase;

    private AdminIamHttpController adminIamHttpController;
    private SessionAdminHttpController sessionAdminHttpController;

    @BeforeEach
    void setUp() {
        adminIamHttpController = new AdminIamHttpController(
                new RegisterCommandMapper(),
                registerCommandUseCase,
                new RegisterResponseMapper(),
                listAccountsQueryUseCase,
                new AccountSummaryResponseMapper(),
                new AssignRoleCommandMapper(),
                assignRoleCommandUseCase,
                new AssignRoleResponseMapper(),
                new BlockUserCommandMapper(),
                blockUserCommandUseCase,
                new BlockUserResponseMapper(),
                new UnblockUserCommandMapper(),
                unblockUserCommandUseCase,
                new UnblockUserResponseMapper(),
                new GetUserPermissionsQueryMapper(),
                getUserPermissionsQueryUseCase,
                new UserPermissionsResponseMapper());

        sessionAdminHttpController = new SessionAdminHttpController(
                new RevokeSessionsCommandMapper(),
                revokeSessionsCommandUseCase,
                new RevokeSessionsResponseMapper(),
                listSessionsQueryUseCase,
                new SessionSummaryResponseMapper());
    }

    @Test
    void shouldMapListAccountsEndpoint() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(listAccountsQueryUseCase.handle())
                .thenReturn(Flux.just(new AccountSummaryResult(
                        "usr-1",
                        "admin@example.test",
                        "ACTIVE",
                        0,
                        now,
                        now,
                        Set.of("SYSTEM_ADMIN"))));

        AccountSummaryResponse response = adminIamHttpController.listAccounts().blockFirst();

        assertEquals("usr-1", response.userId());
        assertEquals("admin@example.test", response.email());
        assertEquals("ACTIVE", response.status());
        assertEquals(Set.of("SYSTEM_ADMIN"), response.roles());
    }

    @Test
    void shouldProtectListAccountsWithAccountReadPermission() throws NoSuchMethodException {
        PreAuthorize annotation = AdminIamHttpController.class
                .getMethod("listAccounts")
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('iam.account.read')", annotation.value());
    }

    @Test
    void shouldMapAssignRoleEndpointUsingAuthenticatedPrincipal() {
        when(assignRoleCommandUseCase.handle(any()))
                .thenReturn(Mono.just(new AssignRoleResult("usr-9", "ACCESS_ADMIN", true, "ASSIGNED")));

        AssignRoleResponse response = adminIamHttpController
                .assignRole("usr-9", new AssignRoleRequest("ACCESS_ADMIN"), authenticated())
                .block();

        assertEquals("usr-9", response.userId());
        assertEquals("ACCESS_ADMIN", response.roleCode());
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
    void shouldMapUnblockUserEndpointUsingAuthenticatedPrincipal() {
        when(unblockUserCommandUseCase.handle(any()))
                .thenReturn(Mono.just(new UnblockUserResult("usr-9", "ACTIVE", true)));

        UnblockUserResponse response = adminIamHttpController
                .unblockUser("usr-9", new UnblockUserRequest("manual_unblock"), authenticated())
                .block();

        assertEquals("usr-9", response.userId());
        assertEquals("ACTIVE", response.status());
        assertEquals(true, response.changed());
    }

    @Test
    void shouldProtectUnblockUserWithAccountUnblockPermission() throws NoSuchMethodException {
        PreAuthorize annotation = AdminIamHttpController.class
                .getMethod("unblockUser", String.class, UnblockUserRequest.class, Authentication.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('iam.account.unblock')", annotation.value());
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
    void shouldMapListSessionsEndpoint() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(listSessionsQueryUseCase.handle())
                .thenReturn(Flux.just(new SessionSummaryResult(
                        "ses-1",
                        "usr-1",
                        "ACTIVE",
                        "127.0.0.1",
                        "device-1",
                        "browser",
                        "WEB",
                        now,
                        now.plusSeconds(900),
                        now.plusSeconds(604800),
                        now,
                        null,
                        null,
                        now,
                        now)));

        SessionSummaryResponse response = sessionAdminHttpController.listSessions().blockFirst();

        assertEquals("ses-1", response.sessionId());
        assertEquals("usr-1", response.userId());
        assertEquals("ACTIVE", response.status());
        assertEquals("127.0.0.1", response.ipAddress());
    }

    @Test
    void shouldProtectListSessionsWithAccountReadPermission() throws NoSuchMethodException {
        PreAuthorize annotation = SessionAdminHttpController.class
                .getMethod("listSessions")
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('iam.account.read')", annotation.value());
    }

    @Test
    void shouldRejectUnsupportedPrincipalTypeForProtectedEndpoints() {
        Authentication unsupported = new UsernamePasswordAuthenticationToken("plain-string", "N/A");

        assertThrows(
                IllegalArgumentException.class,
                () -> adminIamHttpController.assignRole("usr-9", new AssignRoleRequest("ACCESS_ADMIN"), unsupported));
    }

    private Authentication authenticated() {
        IamSecurityPrincipal principal = new IamSecurityPrincipal(
                "actor-1",
                "ses-1",
                "owner@example.test",
                Set.of("SYSTEM_ADMIN"));
        return new UsernamePasswordAuthenticationToken(principal, "N/A", java.util.List.of());
    }
}
