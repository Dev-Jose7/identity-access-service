package io.identityaccess.infrastructure.adapter.out.persistence;

import io.identityaccess.application.port.out.persistence.AccessCatalogPersistencePort;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.exception.RoleInvalidException;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AccessCatalogR2dbcAdapter implements AccessCatalogPersistencePort {

    private final DatabaseClient databaseClient;

    public AccessCatalogR2dbcAdapter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    @Transactional
    public Mono<RoleRecord> createRole(String roleCode, String description, boolean protectedRole, Instant now) {
        String roleId = UUID.randomUUID().toString();
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        String normalizedDescription = normalizeDescription(description);
        return databaseClient.sql("""
                        INSERT INTO role (role_id, role_code, description, status, protected_role, created_at, updated_at)
                        VALUES (:roleId, :roleCode, :description, 'ACTIVE', :protectedRole, :createdAt, :updatedAt)
                        """)
                .bind("roleId", roleId)
                .bind("roleCode", normalizedRoleCode)
                .bind("description", normalizedDescription)
                .bind("protectedRole", protectedRole)
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .fetch()
                .rowsUpdated()
                .then(loadRoleById(roleId))
                .onErrorMap(DuplicateKeyException.class, ex -> new OperationNotPermittedException("Role code already exists"));
    }

    @Override
    @Transactional
    public Mono<RoleRecord> updateRole(String roleId, String description, Instant now) {
        String normalizedRoleId = requireId(roleId, "Role id is required");
        String normalizedDescription = normalizeDescription(description);
        return ensureRoleIsMutable(normalizedRoleId)
                .then(databaseClient.sql("""
                        UPDATE role
                        SET description = :description,
                            updated_at = :updatedAt
                        WHERE role_id = :roleId
                        """)
                .bind("roleId", normalizedRoleId)
                .bind("description", normalizedDescription)
                .bind("updatedAt", now)
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> rowsUpdated > 0 ? loadRoleById(normalizedRoleId) : Mono.error(new RoleInvalidException())));
    }

    @Override
    @Transactional
    public Mono<RoleRecord> disableRole(String roleId, Instant now) {
        String normalizedRoleId = requireId(roleId, "Role id is required");
        return ensureRoleIsMutable(normalizedRoleId)
                .then(databaseClient.sql("""
                        UPDATE role
                        SET status = 'DISABLED',
                            updated_at = :updatedAt
                        WHERE role_id = :roleId
                        """)
                .bind("roleId", normalizedRoleId)
                .bind("updatedAt", now)
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> rowsUpdated > 0 ? loadRoleById(normalizedRoleId) : Mono.error(new RoleInvalidException())));
    }

    @Override
    public Flux<RoleRecord> listRoles() {
        return databaseClient.sql("""
                        SELECT role_id, role_code, description, status, protected_role
                        FROM role
                        ORDER BY role_code ASC
                        """)
                .map((row, metadata) -> toRoleRecord(row))
                .all();
    }

    @Override
    @Transactional
    public Mono<PermissionRecord> createPermission(
            String permissionCode,
            String resource,
            String action,
            String scope,
            String description,
            boolean systemPermission,
            Instant now) {
        String permissionId = UUID.randomUUID().toString();
        String normalizedPermissionCode = normalizePermissionCode(permissionCode);
        String normalizedResource = normalizeResource(resource);
        String normalizedAction = normalizeAction(action);
        String normalizedScope = normalizeScope(scope);
        String normalizedDescription = normalizeDescription(description);
        return databaseClient.sql("""
                        INSERT INTO permission (
                            permission_id,
                            permission_code,
                            resource,
                            action,
                            scope,
                            description,
                            status,
                            system_permission,
                            created_at,
                            updated_at
                        ) VALUES (
                            :permissionId,
                            :permissionCode,
                            :resource,
                            :action,
                            :scope,
                            :description,
                            'ACTIVE',
                            :systemPermission,
                            :createdAt,
                            :updatedAt
                        )
                        """)
                .bind("permissionId", permissionId)
                .bind("permissionCode", normalizedPermissionCode)
                .bind("resource", normalizedResource)
                .bind("action", normalizedAction)
                .bind("scope", normalizedScope)
                .bind("description", normalizedDescription)
                .bind("systemPermission", systemPermission)
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .fetch()
                .rowsUpdated()
                .then(loadPermissionById(permissionId))
                .onErrorMap(DuplicateKeyException.class, ex -> new OperationNotPermittedException("Permission code already exists"));
    }

    @Override
    @Transactional
    public Mono<PermissionRecord> updatePermission(
            String permissionId,
            String resource,
            String action,
            String scope,
            String description,
            Instant now) {
        String normalizedPermissionId = requireId(permissionId, "Permission id is required");
        String normalizedResource = normalizeResource(resource);
        String normalizedAction = normalizeAction(action);
        String normalizedScope = normalizeScope(scope);
        String normalizedDescription = normalizeDescription(description);
        return databaseClient.sql("""
                        UPDATE permission
                        SET resource = :resource,
                            action = :action,
                            scope = :scope,
                            description = :description,
                            updated_at = :updatedAt
                        WHERE permission_id = :permissionId
                        """)
                .bind("permissionId", normalizedPermissionId)
                .bind("resource", normalizedResource)
                .bind("action", normalizedAction)
                .bind("scope", normalizedScope)
                .bind("description", normalizedDescription)
                .bind("updatedAt", now)
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> rowsUpdated > 0 ? loadPermissionById(normalizedPermissionId) : Mono.error(new RoleInvalidException()));
    }

    @Override
    @Transactional
    public Mono<PermissionRecord> disablePermission(String permissionId, Instant now) {
        String normalizedPermissionId = requireId(permissionId, "Permission id is required");
        return databaseClient.sql("""
                        UPDATE permission
                        SET status = 'DISABLED',
                            updated_at = :updatedAt
                        WHERE permission_id = :permissionId
                          AND system_permission = FALSE
                        """)
                .bind("permissionId", normalizedPermissionId)
                .bind("updatedAt", now)
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> rowsUpdated > 0 ? loadPermissionById(normalizedPermissionId) : Mono.error(new RoleInvalidException()));
    }

    @Override
    public Flux<PermissionRecord> listPermissions() {
        return databaseClient.sql("""
                        SELECT permission_id, permission_code, resource, action, scope, description, status, system_permission
                        FROM permission
                        ORDER BY permission_code ASC
                        """)
                .map((row, metadata) -> toPermissionRecord(row))
                .all();
    }

    @Override
    public Flux<PermissionRecord> listPermissionsByRoleId(String roleId) {
        String normalizedRoleId = requireId(roleId, "Role id is required");
        return loadRoleById(normalizedRoleId)
                .thenMany(databaseClient.sql("""
                                SELECT p.permission_id,
                                       p.permission_code,
                                       p.resource,
                                       p.action,
                                       p.scope,
                                       p.description,
                                       p.status,
                                       p.system_permission
                                FROM role_permission rp
                                JOIN permission p ON p.permission_code = rp.permission_code
                                WHERE rp.role_id = :roleId
                                  AND p.status = 'ACTIVE'
                                ORDER BY p.permission_code ASC
                                """)
                        .bind("roleId", normalizedRoleId)
                        .map((row, metadata) -> toPermissionRecord(row))
                        .all());
    }

    @Override
    @Transactional
    public Mono<PermissionGrantRecord> grantPermissionToRole(String roleId, String permissionCode, Instant now) {
        String normalizedRoleId = requireId(roleId, "Role id is required");
        String normalizedPermissionCode = normalizePermissionCode(permissionCode);
        return ensureRoleIsMutable(normalizedRoleId)
                .then(databaseClient.sql("""
                        INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
                        SELECT r.role_id, p.permission_code, p.resource, p.action, p.scope, :createdAt
                        FROM role r
                        JOIN permission p ON p.permission_code = :permissionCode
                        WHERE r.role_id = :roleId
                          AND r.status = 'ACTIVE'
                          AND p.status = 'ACTIVE'
                        ON CONFLICT DO NOTHING
                        """)
                .bind("roleId", normalizedRoleId)
                .bind("permissionCode", normalizedPermissionCode)
                .bind("createdAt", now)
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> validateGrantTarget(normalizedRoleId, normalizedPermissionCode)
                        .thenReturn(new PermissionGrantRecord(normalizedRoleId, normalizedPermissionCode, rowsUpdated > 0))));
    }

    @Override
    @Transactional
    public Mono<PermissionGrantRecord> revokePermissionFromRole(String roleId, String permissionCode) {
        String normalizedRoleId = requireId(roleId, "Role id is required");
        String normalizedPermissionCode = normalizePermissionCode(permissionCode);
        return ensureRoleIsMutable(normalizedRoleId)
                .then(databaseClient.sql("""
                        DELETE FROM role_permission
                        WHERE role_id = :roleId
                          AND permission_code = :permissionCode
                        """)
                .bind("roleId", normalizedRoleId)
                .bind("permissionCode", normalizedPermissionCode)
                .fetch()
                .rowsUpdated()
                .map(rowsUpdated -> new PermissionGrantRecord(normalizedRoleId, normalizedPermissionCode, rowsUpdated > 0)));
    }

    private Mono<RoleRecord> loadRoleById(String roleId) {
        return databaseClient.sql("""
                        SELECT role_id, role_code, description, status, protected_role
                        FROM role
                        WHERE role_id = :roleId
                        """)
                .bind("roleId", roleId)
                .map((row, metadata) -> toRoleRecord(row))
                .one()
                .switchIfEmpty(Mono.error(new RoleInvalidException()));
    }

    private Mono<PermissionRecord> loadPermissionById(String permissionId) {
        return databaseClient.sql("""
                        SELECT permission_id, permission_code, resource, action, scope, description, status, system_permission
                        FROM permission
                        WHERE permission_id = :permissionId
                        """)
                .bind("permissionId", permissionId)
                .map((row, metadata) -> toPermissionRecord(row))
                .one()
                .switchIfEmpty(Mono.error(new RoleInvalidException()));
    }

    private Mono<Void> validateGrantTarget(String roleId, String permissionCode) {
        return databaseClient.sql("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM role r
                            JOIN permission p ON p.permission_code = :permissionCode
                            WHERE r.role_id = :roleId
                              AND r.status = 'ACTIVE'
                              AND p.status = 'ACTIVE'
                        ) AS valid_target
                        """)
                .bind("roleId", roleId)
                .bind("permissionCode", permissionCode)
                .map((row, metadata) -> Boolean.TRUE.equals(row.get("valid_target", Boolean.class)))
                .one()
                .flatMap(valid -> valid ? Mono.empty() : Mono.error(new RoleInvalidException()));
    }

    private Mono<Void> ensureRoleIsMutable(String roleId) {
        return loadRoleById(roleId)
                .flatMap(role -> role.protectedRole()
                        ? Mono.error(new OperationNotPermittedException("Protected role cannot be changed"))
                        : Mono.empty());
    }

    private RoleRecord toRoleRecord(Row row) {
        return new RoleRecord(
                row.get("role_id", String.class),
                row.get("role_code", String.class),
                row.get("description", String.class),
                row.get("status", String.class),
                Boolean.TRUE.equals(row.get("protected_role", Boolean.class)));
    }

    private PermissionRecord toPermissionRecord(Row row) {
        return new PermissionRecord(
                row.get("permission_id", String.class),
                row.get("permission_code", String.class),
                row.get("resource", String.class),
                row.get("action", String.class),
                row.get("scope", String.class),
                row.get("description", String.class),
                row.get("status", String.class),
                Boolean.TRUE.equals(row.get("system_permission", Boolean.class)));
    }

    private String normalizeRoleCode(String value) {
        return io.identityaccess.domain.model.role.RoleCode.from(value).value();
    }

    private String normalizePermissionCode(String value) {
        if (value == null || value.isBlank()) {
            throw new RoleInvalidException();
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[a-z][a-z0-9.-]{1,149}$")) {
            throw new RoleInvalidException();
        }
        return normalized;
    }

    private String normalizeResource(String value) {
        String normalized = requireId(value, "Resource is required").toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[a-z][a-z0-9_.:-]{1,99}$")) {
            throw new RoleInvalidException();
        }
        return normalized;
    }

    private String normalizeAction(String value) {
        String normalized = requireId(value, "Action is required").toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[a-z][a-z0-9_.:-]{1,99}$")) {
            throw new RoleInvalidException();
        }
        return normalized;
    }

    private String normalizeScope(String value) {
        String normalized = value == null || value.isBlank() ? "GLOBAL" : value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("^[A-Z][A-Z0-9_:-]{1,49}$")) {
            throw new RoleInvalidException();
        }
        return normalized;
    }

    private String normalizeDescription(String value) {
        if (value == null || value.isBlank() || value.length() > 255) {
            throw new RoleInvalidException();
        }
        return value.trim();
    }

    private String requireId(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new OperationNotPermittedException(message);
        }
        return value.trim();
    }
}
