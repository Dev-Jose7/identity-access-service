package com.arka.identityaccess.domain.service;

import com.arka.identityaccess.domain.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.exception.RoleInvalidException;
import com.arka.identityaccess.domain.model.role.RoleCode;
import com.arka.identityaccess.domain.model.user.RegistrationMode;

public class UserRegistrationPolicy {

    public RegistrationDecision evaluate(
            RegistrationMode registrationMode,
            String requestedRoleCode,
            String actorId) {
        if (registrationMode == null) {
            throw new OperationNotPermittedException("Registration mode is required");
        }

        return switch (registrationMode) {
            case ONBOARDING_OWNER -> new RegistrationDecision(RoleCode.ORG_OWNER, false);
            case ADMIN_CREATE -> evaluateAdminCreate(requestedRoleCode, actorId);
        };
    }

    private RegistrationDecision evaluateAdminCreate(String requestedRoleCode, String actorId) {
        if (actorId == null || actorId.isBlank()) {
            throw new OperationNotPermittedException("Register requires authenticated actor context");
        }

        RoleCode targetRoleCode = resolveAdminTargetRoleCode(requestedRoleCode);
        return new RegistrationDecision(targetRoleCode, true);
    }

    private RoleCode resolveAdminTargetRoleCode(String requestedRoleCode) {
        if (requestedRoleCode == null || requestedRoleCode.isBlank()) {
            return RoleCode.ORG_USER;
        }

        RoleCode roleCode = RoleCode.from(requestedRoleCode);
        if (roleCode == RoleCode.ORG_OWNER || roleCode == RoleCode.ARKA_ADMIN) {
            throw new RoleInvalidException();
        }
        return roleCode;
    }

    public record RegistrationDecision(RoleCode targetRoleCode, boolean requiresActorAssignmentValidation) {}
}
