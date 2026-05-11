package io.identityaccess.application.port.in;

import io.identityaccess.application.command.catalog.CreatePermissionCommand;
import io.identityaccess.application.command.catalog.CreateRoleCommand;
import io.identityaccess.application.command.catalog.GrantPermissionToRoleCommand;
import io.identityaccess.application.command.catalog.RevokePermissionFromRoleCommand;
import io.identityaccess.application.command.catalog.UpdatePermissionCommand;
import io.identityaccess.application.command.catalog.UpdateRoleCommand;
import io.identityaccess.application.result.catalog.PermissionCatalogResult;
import io.identityaccess.application.result.catalog.PermissionGrantResult;
import io.identityaccess.application.result.catalog.RoleCatalogResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccessCatalogCommandUseCase {

    Mono<RoleCatalogResult> createRole(CreateRoleCommand command);

    Mono<RoleCatalogResult> updateRole(UpdateRoleCommand command);

    Mono<RoleCatalogResult> disableRole(String roleId, String actorUserId);

    Flux<RoleCatalogResult> listRoles();

    Mono<PermissionCatalogResult> createPermission(CreatePermissionCommand command);

    Mono<PermissionCatalogResult> updatePermission(UpdatePermissionCommand command);

    Mono<PermissionCatalogResult> disablePermission(String permissionId, String actorUserId);

    Flux<PermissionCatalogResult> listPermissions();

    Flux<PermissionCatalogResult> listPermissionsByRoleId(String roleId);

    Mono<PermissionGrantResult> grantPermissionToRole(GrantPermissionToRoleCommand command);

    Mono<PermissionGrantResult> revokePermissionFromRole(RevokePermissionFromRoleCommand command);
}
