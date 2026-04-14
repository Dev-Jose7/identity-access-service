package com.arka.identityaccess.domain.access.service;

import com.arka.identityaccess.domain.access.exception.RoleInvalidException;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.identity.enumtype.RegistrationMode;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;

public class InitialAccessProvisioningPolicy {

    private static final RoleCode DEFAULT_ADMIN_CREATE_ROLE = RoleCode.of("ORG_USER");
    private static final RoleCode FOUNDER_ROLE = RoleCode.of("ORG_OWNER");
    private static final RoleCode FORBIDDEN_ADMIN_CREATE_OWNER = RoleCode.of("ORG_OWNER");
    private static final RoleCode FORBIDDEN_PLATFORM_ROLE = RoleCode.of("ARKA_ADMIN");

    public ProvisioningDecision resolveInitialAccess(RegistrationMode registrationMode, String requestedRoleCode) {
        if (registrationMode == null) {
            throw new OperationNotPermittedException("registration mode is required");
        }

        return switch (registrationMode) {
            case ONBOARDING_OWNER -> new ProvisioningDecision(FOUNDER_ROLE, false);
            case ADMIN_CREATE -> resolveAdminCreate(requestedRoleCode);
        };
    }

    private ProvisioningDecision resolveAdminCreate(String requestedRoleCode) {
        RoleCode targetRoleCode = resolveAdminTargetRoleCode(requestedRoleCode);
        return new ProvisioningDecision(targetRoleCode, true);
    }

    private RoleCode resolveAdminTargetRoleCode(String requestedRoleCode) {
        if (requestedRoleCode == null || requestedRoleCode.isBlank()) {
            return DEFAULT_ADMIN_CREATE_ROLE;
        }

        RoleCode roleCode = RoleCode.of(requestedRoleCode);
        if (roleCode.equals(FORBIDDEN_ADMIN_CREATE_OWNER) || roleCode.equals(FORBIDDEN_PLATFORM_ROLE)) {
            throw new RoleInvalidException("requested role cannot be assigned with current registration mode");
        }
        return roleCode;
    }

    public record ProvisioningDecision(RoleCode targetRoleCode, boolean requiresActorAssignmentValidation) {}
}
