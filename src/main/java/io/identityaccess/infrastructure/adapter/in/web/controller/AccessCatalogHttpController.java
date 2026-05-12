package io.identityaccess.infrastructure.adapter.in.web.controller;

import io.identityaccess.application.command.catalog.CreatePermissionCommand;
import io.identityaccess.application.command.catalog.CreateRoleCommand;
import io.identityaccess.application.command.catalog.GrantPermissionToRoleCommand;
import io.identityaccess.application.command.catalog.RevokePermissionFromRoleCommand;
import io.identityaccess.application.command.catalog.UpdatePermissionCommand;
import io.identityaccess.application.command.catalog.UpdateRoleCommand;
import io.identityaccess.application.port.in.AccessCatalogCommandUseCase;
import io.identityaccess.application.result.catalog.PermissionCatalogResult;
import io.identityaccess.application.result.catalog.PermissionGrantResult;
import io.identityaccess.application.result.catalog.RoleCatalogResult;
import io.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import io.identityaccess.infrastructure.adapter.in.web.request.catalog.CreatePermissionRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.catalog.CreateRoleRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.catalog.GrantPermissionToRoleRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.catalog.UpdatePermissionRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.catalog.UpdateRoleRequest;
import io.identityaccess.infrastructure.adapter.in.web.response.catalog.PermissionCatalogResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.catalog.PermissionGrantResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.catalog.RoleCatalogResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/admin/iam")
public class AccessCatalogHttpController {

    private final AccessCatalogCommandUseCase accessCatalogCommandUseCase;

    public AccessCatalogHttpController(AccessCatalogCommandUseCase accessCatalogCommandUseCase) {
        this.accessCatalogCommandUseCase = accessCatalogCommandUseCase;
    }

    @PreAuthorize("hasAuthority('iam.role.read')")
    @GetMapping("/roles")
    public Flux<RoleCatalogResponse> listRoles() {
        return accessCatalogCommandUseCase.listRoles().map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.role.create')")
    @PostMapping("/roles")
    public Mono<RoleCatalogResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return accessCatalogCommandUseCase
                .createRole(new CreateRoleCommand(
                        request.roleCode(),
                        request.description(),
                        request.protectedRole(),
                        principal.userId()))
                .map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.role.update')")
    @PatchMapping("/roles/{roleId}")
    public Mono<RoleCatalogResponse> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return accessCatalogCommandUseCase
                .updateRole(new UpdateRoleCommand(roleId, request.description(), principal.userId()))
                .map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.role.update')")
    @PostMapping("/roles/{roleId}/disable")
    public Mono<RoleCatalogResponse> disableRole(@PathVariable String roleId, Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return accessCatalogCommandUseCase.disableRole(roleId, principal.userId()).map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.permission.read')")
    @GetMapping("/permissions")
    public Flux<PermissionCatalogResponse> listPermissions() {
        return accessCatalogCommandUseCase.listPermissions().map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.permission.read')")
    @GetMapping("/roles/{roleId}/permissions")
    public Flux<PermissionCatalogResponse> listPermissionsByRoleId(@PathVariable String roleId) {
        return accessCatalogCommandUseCase.listPermissionsByRoleId(roleId).map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.permission.create')")
    @PostMapping("/permissions")
    public Mono<PermissionCatalogResponse> createPermission(
            @Valid @RequestBody CreatePermissionRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return accessCatalogCommandUseCase
                .createPermission(new CreatePermissionCommand(
                        request.permissionCode(),
                        request.resource(),
                        request.action(),
                        request.scope(),
                        request.description(),
                        request.systemPermission(),
                        principal.userId()))
                .map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.permission.update')")
    @PatchMapping("/permissions/{permissionId}")
    public Mono<PermissionCatalogResponse> updatePermission(
            @PathVariable String permissionId,
            @Valid @RequestBody UpdatePermissionRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return accessCatalogCommandUseCase
                .updatePermission(new UpdatePermissionCommand(
                        permissionId,
                        request.resource(),
                        request.action(),
                        request.scope(),
                        request.description(),
                        principal.userId()))
                .map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.permission.update')")
    @PostMapping("/permissions/{permissionId}/disable")
    public Mono<PermissionCatalogResponse> disablePermission(@PathVariable String permissionId, Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return accessCatalogCommandUseCase.disablePermission(permissionId, principal.userId()).map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.role.update')")
    @PostMapping("/roles/{roleId}/permissions")
    public Mono<PermissionGrantResponse> grantPermissionToRole(
            @PathVariable String roleId,
            @Valid @RequestBody GrantPermissionToRoleRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return accessCatalogCommandUseCase
                .grantPermissionToRole(new GrantPermissionToRoleCommand(roleId, request.permissionCode(), principal.userId()))
                .map(this::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.role.update')")
    @DeleteMapping("/roles/{roleId}/permissions/{permissionCode}")
    public Mono<PermissionGrantResponse> revokePermissionFromRole(
            @PathVariable String roleId,
            @PathVariable String permissionCode,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return accessCatalogCommandUseCase
                .revokePermissionFromRole(new RevokePermissionFromRoleCommand(roleId, permissionCode, principal.userId()))
                .map(this::toResponse);
    }

    private RoleCatalogResponse toResponse(RoleCatalogResult result) {
        return new RoleCatalogResponse(
                result.roleId(),
                result.roleCode(),
                result.description(),
                result.status(),
                result.protectedRole());
    }

    private PermissionCatalogResponse toResponse(PermissionCatalogResult result) {
        return new PermissionCatalogResponse(
                result.permissionId(),
                result.permissionCode(),
                result.resource(),
                result.action(),
                result.scope(),
                result.description(),
                result.status(),
                result.systemPermission());
    }

    private PermissionGrantResponse toResponse(PermissionGrantResult result) {
        return new PermissionGrantResponse(
                result.roleId(),
                result.permissionCode(),
                result.changed(),
                result.status());
    }
}
