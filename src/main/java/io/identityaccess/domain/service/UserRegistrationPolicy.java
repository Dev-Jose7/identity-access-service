package io.identityaccess.domain.service;

import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.exception.RoleInvalidException;
import io.identityaccess.domain.model.role.RoleCode;
import io.identityaccess.domain.model.user.RegistrationMode;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class UserRegistrationPolicy {

    private final boolean publicRegistrationEnabled;
    private final boolean primaryRegistrationEnabled;
    private final RoleCode primaryInitialRoleCode;
    private final RoleCode publicDefaultRoleCode;
    private final RoleCode adminDefaultRoleCode;
    private final Set<String> protectedRoleCodes;

    public UserRegistrationPolicy() {
        this(true, true, "SYSTEM_ADMIN", "ACCOUNT_USER", "ACCOUNT_USER", Set.of("SYSTEM_ADMIN"));
    }

    public UserRegistrationPolicy(
            boolean publicRegistrationEnabled,
            boolean primaryRegistrationEnabled,
            String primaryInitialRoleCode,
            String publicDefaultRoleCode,
            String adminDefaultRoleCode,
            Set<String> protectedRoleCodes) {
        this.publicRegistrationEnabled = publicRegistrationEnabled;
        this.primaryRegistrationEnabled = primaryRegistrationEnabled;
        this.primaryInitialRoleCode = RoleCode.of(primaryInitialRoleCode);
        this.publicDefaultRoleCode = RoleCode.of(publicDefaultRoleCode);
        this.adminDefaultRoleCode = RoleCode.of(adminDefaultRoleCode);
        this.protectedRoleCodes = normalizeProtectedRoles(protectedRoleCodes);
        ensurePublicRoleIsNotProtected();
    }

    public RegistrationDecision evaluate(
            RegistrationMode registrationMode,
            String requestedRoleCode,
            String actorId) {
        if (registrationMode == null) {
            throw new OperationNotPermittedException("Registration mode is required");
        }

        return switch (registrationMode) {
            case PRIMARY_REGISTRATION -> evaluatePrimaryRegistration();
            case PUBLIC_SELF_REGISTRATION -> evaluatePublicRegistration();
            case ADMIN_CREATE -> evaluateAdminCreate(requestedRoleCode, actorId);
        };
    }

    private RegistrationDecision evaluatePrimaryRegistration() {
        if (!primaryRegistrationEnabled) {
            throw new OperationNotPermittedException("Primary account registration is disabled");
        }
        return new RegistrationDecision(primaryInitialRoleCode, false, true);
    }

    private RegistrationDecision evaluatePublicRegistration() {
        if (!publicRegistrationEnabled) {
            throw new OperationNotPermittedException("Public account registration is disabled");
        }
        return new RegistrationDecision(publicDefaultRoleCode, false, false);
    }

    private RegistrationDecision evaluateAdminCreate(String requestedRoleCode, String actorId) {
        if (actorId == null || actorId.isBlank()) {
            throw new OperationNotPermittedException("Register requires authenticated actor context");
        }

        RoleCode targetRoleCode = resolveAdminTargetRoleCode(requestedRoleCode);
        return new RegistrationDecision(targetRoleCode, true, false);
    }

    private RoleCode resolveAdminTargetRoleCode(String requestedRoleCode) {
        if (requestedRoleCode == null || requestedRoleCode.isBlank()) {
            return adminDefaultRoleCode;
        }

        RoleCode roleCode = RoleCode.of(requestedRoleCode);
        if (protectedRoleCodes.contains(roleCode.value())) {
            throw new RoleInvalidException();
        }
        return roleCode;
    }

    private Set<String> normalizeProtectedRoles(Set<String> rawRoleCodes) {
        if (rawRoleCodes == null || rawRoleCodes.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String rawRoleCode : rawRoleCodes) {
            if (rawRoleCode == null || rawRoleCode.isBlank()) {
                continue;
            }
            normalized.add(RoleCode.of(rawRoleCode.trim().toUpperCase(Locale.ROOT)).value());
        }
        return Collections.unmodifiableSet(normalized);
    }

    private void ensurePublicRoleIsNotProtected() {
        if (protectedRoleCodes.contains(publicDefaultRoleCode.value())) {
            throw new RoleInvalidException();
        }
    }

    public record RegistrationDecision(
            RoleCode targetRoleCode,
            boolean requiresActorAssignmentValidation,
            boolean primaryRegistration) {}
}
