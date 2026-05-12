package io.identityaccess.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.identityaccess.application.port.in.AccessCatalogCommandUseCase;
import io.identityaccess.application.result.catalog.PermissionCatalogResult;
import io.identityaccess.application.result.catalog.PermissionGrantResult;
import io.identityaccess.application.result.catalog.RoleCatalogResult;
import io.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import io.identityaccess.infrastructure.adapter.in.web.controller.AccessCatalogHttpController;
import io.identityaccess.infrastructure.adapter.in.web.request.catalog.CreatePermissionRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.catalog.CreateRoleRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.catalog.GrantPermissionToRoleRequest;
import io.identityaccess.infrastructure.adapter.in.web.response.catalog.PermissionCatalogResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.catalog.PermissionGrantResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.catalog.RoleCatalogResponse;
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
class AccessCatalogHttpControllerUnitTest {

    @Mock
    private AccessCatalogCommandUseCase accessCatalogCommandUseCase;

    private AccessCatalogHttpController controller;

    @BeforeEach
    void setUp() {
        controller = new AccessCatalogHttpController(accessCatalogCommandUseCase);
    }

    @Test
    void shouldMapCreateRoleEndpointUsingAuthenticatedPrincipal() {
        when(accessCatalogCommandUseCase.createRole(any()))
                .thenReturn(Mono.just(new RoleCatalogResult(
                        "role-1",
                        "ACCESS_OPERATOR",
                        "Access operator",
                        "ACTIVE",
                        false)));

        RoleCatalogResponse response = controller
                .createRole(new CreateRoleRequest("ACCESS_OPERATOR", "Access operator", false), authenticated())
                .block();

        assertEquals("role-1", response.roleId());
        assertEquals("ACCESS_OPERATOR", response.roleCode());
        assertEquals("ACTIVE", response.status());
    }

    @Test
    void shouldMapCreatePermissionEndpointUsingAuthenticatedPrincipal() {
        when(accessCatalogCommandUseCase.createPermission(any()))
                .thenReturn(Mono.just(new PermissionCatalogResult(
                        "perm-1",
                        "iam.account.export",
                        "iam.account",
                        "export",
                        "GLOBAL",
                        "Export account",
                        "ACTIVE",
                        false)));

        PermissionCatalogResponse response = controller
                .createPermission(new CreatePermissionRequest(
                        "iam.account.export",
                        "iam.account",
                        "export",
                        "GLOBAL",
                        "Export account",
                        false), authenticated())
                .block();

        assertEquals("perm-1", response.permissionId());
        assertEquals("iam.account.export", response.permissionCode());
        assertEquals("iam.account", response.resource());
        assertEquals("export", response.action());
    }

    @Test
    void shouldMapGrantPermissionEndpointUsingAuthenticatedPrincipal() {
        when(accessCatalogCommandUseCase.grantPermissionToRole(any()))
                .thenReturn(Mono.just(new PermissionGrantResult(
                        "role-1",
                        "iam.account.read",
                        true,
                        "GRANTED")));

        PermissionGrantResponse response = controller
                .grantPermissionToRole(
                        "role-1",
                        new GrantPermissionToRoleRequest("iam.account.read"),
                        authenticated())
                .block();

        assertEquals("role-1", response.roleId());
        assertEquals("iam.account.read", response.permissionCode());
        assertEquals(true, response.changed());
        assertEquals("GRANTED", response.status());
    }

    @Test
    void shouldMapListRolePermissionsEndpoint() {
        when(accessCatalogCommandUseCase.listPermissionsByRoleId("role-1"))
                .thenReturn(Flux.just(new PermissionCatalogResult(
                        "perm-1",
                        "iam.account.read",
                        "iam.account",
                        "read",
                        "GLOBAL",
                        "Read accounts",
                        "ACTIVE",
                        true)));

        PermissionCatalogResponse response = controller.listPermissionsByRoleId("role-1").blockFirst();

        assertEquals("perm-1", response.permissionId());
        assertEquals("iam.account.read", response.permissionCode());
        assertEquals("iam.account", response.resource());
        assertEquals("read", response.action());
    }

    @Test
    void shouldProtectListRolePermissionsWithPermissionReadPermission() throws NoSuchMethodException {
        PreAuthorize annotation = AccessCatalogHttpController.class
                .getMethod("listPermissionsByRoleId", String.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('iam.permission.read')", annotation.value());
    }

    private Authentication authenticated() {
        IamSecurityPrincipal principal = new IamSecurityPrincipal(
                "actor-1",
                "ses-1",
                "operator@example.test",
                Set.of("SYSTEM_ADMIN"));
        return new UsernamePasswordAuthenticationToken(principal, "N/A", java.util.List.of());
    }
}
