package io.identityaccess.infrastructure.adapter.in.web.response;

public record AssignRoleResponse(
        String userId,
        String roleCode,
        boolean assigned,
        String status) {}
