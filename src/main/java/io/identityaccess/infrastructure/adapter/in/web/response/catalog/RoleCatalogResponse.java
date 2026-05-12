package io.identityaccess.infrastructure.adapter.in.web.response.catalog;

public record RoleCatalogResponse(
        String roleId,
        String roleCode,
        String description,
        String status,
        boolean protectedRole) {}
