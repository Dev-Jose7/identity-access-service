package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public record AccessProfile(
        AccountId accountId,
        EmailAddress email,
        Set<RoleCode> activeRoles,
        Set<PermissionCode> effectivePermissions,
        Instant calculatedAt) {

    public AccessProfile {
        if (accountId == null || email == null || calculatedAt == null) {
            throw new DomainInvariantViolationException("access profile requires account identity and calculation timestamp");
        }
        activeRoles = sanitizeRoles(activeRoles);
        effectivePermissions = sanitizePermissions(effectivePermissions);
    }

    public static AccessProfile of(
            AccountId accountId,
            EmailAddress email,
            Set<RoleCode> activeRoles,
            Set<PermissionCode> effectivePermissions,
            Instant calculatedAt) {
        return new AccessProfile(accountId, email, activeRoles, effectivePermissions, calculatedAt);
    }

    public Set<String> roleCodeValues() {
        return activeRoles.stream()
                .map(RoleCode::value)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> permissionCodeValues() {
        return effectivePermissions.stream()
                .map(PermissionCode::value)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<RoleCode> sanitizeRoles(Set<RoleCode> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .filter(role -> role != null)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<PermissionCode> sanitizePermissions(Set<PermissionCode> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Set.of();
        }
        return permissions.stream()
                .filter(permission -> permission != null)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
}
