package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;

public record RoleAssignmentContext(
        RoleId roleId,
        boolean assignable,
        boolean protectedRole) {

    public RoleAssignmentContext {
        if (roleId == null) {
            throw new DomainInvariantViolationException("role assignment context requires roleId");
        }
    }

    public static RoleAssignmentContext of(RoleId roleId, boolean assignable, boolean protectedRole) {
        return new RoleAssignmentContext(roleId, assignable, protectedRole);
    }
}
