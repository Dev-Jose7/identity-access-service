package io.identityaccess.application.usecase.command;

import io.identityaccess.application.command.catalog.CreatePermissionCommand;
import io.identityaccess.application.command.catalog.CreateRoleCommand;
import io.identityaccess.application.command.catalog.GrantPermissionToRoleCommand;
import io.identityaccess.application.command.catalog.RevokePermissionFromRoleCommand;
import io.identityaccess.application.command.catalog.UpdatePermissionCommand;
import io.identityaccess.application.command.catalog.UpdateRoleCommand;
import io.identityaccess.application.port.in.AccessCatalogCommandUseCase;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.AccessCatalogPersistencePort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.result.catalog.PermissionCatalogResult;
import io.identityaccess.application.result.catalog.PermissionGrantResult;
import io.identityaccess.application.result.catalog.RoleCatalogResult;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.model.role.event.PermissionCreatedEvent;
import io.identityaccess.domain.model.role.event.PermissionDisabledEvent;
import io.identityaccess.domain.model.role.event.PermissionGrantedToRoleEvent;
import io.identityaccess.domain.model.role.event.PermissionRevokedFromRoleEvent;
import io.identityaccess.domain.model.role.event.PermissionUpdatedEvent;
import io.identityaccess.domain.model.role.event.RoleCreatedEvent;
import io.identityaccess.domain.model.role.event.RoleDisabledEvent;
import io.identityaccess.domain.model.role.event.RoleUpdatedEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccessCatalogUseCase implements AccessCatalogCommandUseCase {

    private final AccessCatalogPersistencePort accessCatalogPersistencePort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final SecurityAuditPort securityAuditPort;
    private final ClockPort clockPort;

    public AccessCatalogUseCase(
            AccessCatalogPersistencePort accessCatalogPersistencePort,
            OutboxPersistencePort outboxPersistencePort,
            SecurityAuditPort securityAuditPort,
            ClockPort clockPort) {
        this.accessCatalogPersistencePort = accessCatalogPersistencePort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.securityAuditPort = securityAuditPort;
        this.clockPort = clockPort;
    }

    @Override
    public Mono<RoleCatalogResult> createRole(CreateRoleCommand command) {
        String actorUserId = requireActor(command.actorUserId());
        var now = clockPort.now();
        return accessCatalogPersistencePort
                .createRole(command.roleCode(), command.description(), command.protectedRole(), now)
                .flatMap(role -> securityAuditPort.recordAccessCatalogChanged(
                                actorUserId, "ROLE_CREATED", "ROLE", role.roleId(), role.roleCode(), true)
                        .then(outboxPersistencePort.store(RoleCreatedEvent.create(role.roleId(), role.roleCode(), actorUserId, now)))
                        .thenReturn(toResult(role)));
    }

    @Override
    public Mono<RoleCatalogResult> updateRole(UpdateRoleCommand command) {
        String actorUserId = requireActor(command.actorUserId());
        var now = clockPort.now();
        return accessCatalogPersistencePort
                .updateRole(command.roleId(), command.description(), now)
                .flatMap(role -> securityAuditPort.recordAccessCatalogChanged(
                                actorUserId, "ROLE_UPDATED", "ROLE", role.roleId(), role.roleCode(), true)
                        .then(outboxPersistencePort.store(RoleUpdatedEvent.create(role.roleId(), role.roleCode(), actorUserId, now)))
                        .thenReturn(toResult(role)));
    }

    @Override
    public Mono<RoleCatalogResult> disableRole(String roleId, String actorUserId) {
        String actor = requireActor(actorUserId);
        var now = clockPort.now();
        return accessCatalogPersistencePort
                .disableRole(roleId, now)
                .flatMap(role -> securityAuditPort.recordAccessCatalogChanged(
                                actor, "ROLE_DISABLED", "ROLE", role.roleId(), role.roleCode(), true)
                        .then(outboxPersistencePort.store(RoleDisabledEvent.create(role.roleId(), role.roleCode(), actor, now)))
                        .thenReturn(toResult(role)));
    }

    @Override
    public Flux<RoleCatalogResult> listRoles() {
        return accessCatalogPersistencePort.listRoles().map(this::toResult);
    }

    @Override
    public Mono<PermissionCatalogResult> createPermission(CreatePermissionCommand command) {
        String actorUserId = requireActor(command.actorUserId());
        var now = clockPort.now();
        return accessCatalogPersistencePort
                .createPermission(
                        command.permissionCode(),
                        command.resource(),
                        command.action(),
                        command.scope(),
                        command.description(),
                        command.systemPermission(),
                        now)
                .flatMap(permission -> securityAuditPort.recordAccessCatalogChanged(
                                actorUserId,
                                "PERMISSION_CREATED",
                                "PERMISSION",
                                permission.permissionId(),
                                permission.permissionCode(),
                                true)
                        .then(outboxPersistencePort.store(PermissionCreatedEvent.create(
                                permission.permissionId(), permission.permissionCode(), actorUserId, now)))
                        .thenReturn(toResult(permission)));
    }

    @Override
    public Mono<PermissionCatalogResult> updatePermission(UpdatePermissionCommand command) {
        String actorUserId = requireActor(command.actorUserId());
        var now = clockPort.now();
        return accessCatalogPersistencePort
                .updatePermission(
                        command.permissionId(),
                        command.resource(),
                        command.action(),
                        command.scope(),
                        command.description(),
                        now)
                .flatMap(permission -> securityAuditPort.recordAccessCatalogChanged(
                                actorUserId,
                                "PERMISSION_UPDATED",
                                "PERMISSION",
                                permission.permissionId(),
                                permission.permissionCode(),
                                true)
                        .then(outboxPersistencePort.store(PermissionUpdatedEvent.create(
                                permission.permissionId(), permission.permissionCode(), actorUserId, now)))
                        .thenReturn(toResult(permission)));
    }

    @Override
    public Mono<PermissionCatalogResult> disablePermission(String permissionId, String actorUserId) {
        String actor = requireActor(actorUserId);
        var now = clockPort.now();
        return accessCatalogPersistencePort
                .disablePermission(permissionId, now)
                .flatMap(permission -> securityAuditPort.recordAccessCatalogChanged(
                                actor,
                                "PERMISSION_DISABLED",
                                "PERMISSION",
                                permission.permissionId(),
                                permission.permissionCode(),
                                true)
                        .then(outboxPersistencePort.store(PermissionDisabledEvent.create(
                                permission.permissionId(), permission.permissionCode(), actor, now)))
                        .thenReturn(toResult(permission)));
    }

    @Override
    public Flux<PermissionCatalogResult> listPermissions() {
        return accessCatalogPersistencePort.listPermissions().map(this::toResult);
    }

    @Override
    public Flux<PermissionCatalogResult> listPermissionsByRoleId(String roleId) {
        return accessCatalogPersistencePort.listPermissionsByRoleId(roleId).map(this::toResult);
    }

    @Override
    public Mono<PermissionGrantResult> grantPermissionToRole(GrantPermissionToRoleCommand command) {
        String actorUserId = requireActor(command.actorUserId());
        var now = clockPort.now();
        return accessCatalogPersistencePort
                .grantPermissionToRole(command.roleId(), command.permissionCode(), now)
                .flatMap(grant -> securityAuditPort.recordAccessCatalogChanged(
                                actorUserId,
                                "PERMISSION_GRANTED_TO_ROLE",
                                "ROLE",
                                grant.roleId(),
                                grant.permissionCode(),
                                grant.changed())
                        .then(grant.changed()
                                ? outboxPersistencePort.store(PermissionGrantedToRoleEvent.create(
                                        grant.roleId(), grant.permissionCode(), actorUserId, now)).then()
                                : Mono.empty())
                        .thenReturn(new PermissionGrantResult(
                                grant.roleId(),
                                grant.permissionCode(),
                                grant.changed(),
                                grant.changed() ? "GRANTED" : "ALREADY_GRANTED")));
    }

    @Override
    public Mono<PermissionGrantResult> revokePermissionFromRole(RevokePermissionFromRoleCommand command) {
        String actorUserId = requireActor(command.actorUserId());
        var now = clockPort.now();
        return accessCatalogPersistencePort
                .revokePermissionFromRole(command.roleId(), command.permissionCode())
                .flatMap(grant -> securityAuditPort.recordAccessCatalogChanged(
                                actorUserId,
                                "PERMISSION_REVOKED_FROM_ROLE",
                                "ROLE",
                                grant.roleId(),
                                grant.permissionCode(),
                                grant.changed())
                        .then(grant.changed()
                                ? outboxPersistencePort.store(PermissionRevokedFromRoleEvent.create(
                                        grant.roleId(), grant.permissionCode(), actorUserId, now)).then()
                                : Mono.empty())
                        .thenReturn(new PermissionGrantResult(
                                grant.roleId(),
                                grant.permissionCode(),
                                grant.changed(),
                                grant.changed() ? "REVOKED" : "NOT_ASSIGNED")));
    }

    private RoleCatalogResult toResult(AccessCatalogPersistencePort.RoleRecord role) {
        return new RoleCatalogResult(role.roleId(), role.roleCode(), role.description(), role.status(), role.protectedRole());
    }

    private PermissionCatalogResult toResult(AccessCatalogPersistencePort.PermissionRecord permission) {
        return new PermissionCatalogResult(
                permission.permissionId(),
                permission.permissionCode(),
                permission.resource(),
                permission.action(),
                permission.scope(),
                permission.description(),
                permission.status(),
                permission.systemPermission());
    }

    private String requireActor(String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new OperationNotPermittedException("Authenticated actor is required");
        }
        return actorUserId.trim();
    }
}
