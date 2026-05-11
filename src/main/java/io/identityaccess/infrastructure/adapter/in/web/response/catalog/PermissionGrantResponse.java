package io.identityaccess.infrastructure.adapter.in.web.response.catalog;

public record PermissionGrantResponse(
        String roleId,
        String permissionCode,
        boolean changed,
        String status) {}
