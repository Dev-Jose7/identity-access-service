package io.identityaccess.application.result.catalog;

public record RoleCatalogResult(
        String roleId,
        String roleCode,
        String description,
        String status,
        boolean protectedRole) {}
