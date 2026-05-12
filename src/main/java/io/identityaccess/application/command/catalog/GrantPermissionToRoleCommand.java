package io.identityaccess.application.command.catalog;

public record GrantPermissionToRoleCommand(
        String roleId,
        String permissionCode,
        String actorUserId) {}
