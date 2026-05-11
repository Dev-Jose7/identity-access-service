package io.identityaccess.application.command.catalog;

public record RevokePermissionFromRoleCommand(
        String roleId,
        String permissionCode,
        String actorUserId) {}
