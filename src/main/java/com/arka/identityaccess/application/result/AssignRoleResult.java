package com.arka.identityaccess.application.result;

public record AssignRoleResult(
        String userId,
        String roleCode,
        boolean assigned,
        String status) {}
