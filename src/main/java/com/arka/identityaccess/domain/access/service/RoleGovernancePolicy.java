package com.arka.identityaccess.domain.access.service;

import com.arka.identityaccess.domain.access.aggregate.RoleAggregate;
import com.arka.identityaccess.domain.access.exception.RoleInvalidException;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import java.util.Set;

public class RoleGovernancePolicy {

    private final Set<RoleCode> protectedRoles;

    public RoleGovernancePolicy(Set<RoleCode> protectedRoles) {
        this.protectedRoles = protectedRoles == null ? Set.of() : Set.copyOf(protectedRoles);
    }

    public void ensureRoleCanBeChanged(RoleAggregate role) {
        if (role == null) {
            throw new RoleInvalidException("role is required");
        }
        if (protectedRoles.contains(role.code())) {
            throw new RoleInvalidException("protected role cannot be modified");
        }
    }

    public void ensureRoleCanBeDisabled(RoleAggregate role) {
        ensureRoleCanBeChanged(role);
    }
}
