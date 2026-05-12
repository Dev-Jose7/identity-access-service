package io.identityaccess.application.port.out.persistence;

import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccessCatalogPersistencePort {

    Mono<RoleRecord> createRole(String roleCode, String description, boolean protectedRole, Instant now);

    Mono<RoleRecord> updateRole(String roleId, String description, Instant now);

    Mono<RoleRecord> disableRole(String roleId, Instant now);

    Flux<RoleRecord> listRoles();

    Mono<PermissionRecord> createPermission(
            String permissionCode,
            String resource,
            String action,
            String scope,
            String description,
            boolean systemPermission,
            Instant now);

    Mono<PermissionRecord> updatePermission(
            String permissionId,
            String resource,
            String action,
            String scope,
            String description,
            Instant now);

    Mono<PermissionRecord> disablePermission(String permissionId, Instant now);

    Flux<PermissionRecord> listPermissions();

    Flux<PermissionRecord> listPermissionsByRoleId(String roleId);

    Mono<PermissionGrantRecord> grantPermissionToRole(String roleId, String permissionCode, Instant now);

    Mono<PermissionGrantRecord> revokePermissionFromRole(String roleId, String permissionCode);

    record RoleRecord(
            String roleId,
            String roleCode,
            String description,
            String status,
            boolean protectedRole) {}

    record PermissionRecord(
            String permissionId,
            String permissionCode,
            String resource,
            String action,
            String scope,
            String description,
            String status,
            boolean systemPermission) {}

    record PermissionGrantRecord(
            String roleId,
            String permissionCode,
            boolean changed) {}
}
