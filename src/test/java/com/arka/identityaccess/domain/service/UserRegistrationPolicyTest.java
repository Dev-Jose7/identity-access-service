package com.arka.identityaccess.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.identityaccess.domain.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.exception.RoleInvalidException;
import com.arka.identityaccess.domain.model.role.RoleCode;
import com.arka.identityaccess.domain.model.user.RegistrationMode;
import org.junit.jupiter.api.Test;

class UserRegistrationPolicyTest {

    private final UserRegistrationPolicy policy = new UserRegistrationPolicy();

    @Test
    void shouldResolveOrgOwnerForFounderOnboarding() {
        UserRegistrationPolicy.RegistrationDecision decision =
                policy.evaluate(RegistrationMode.ONBOARDING_OWNER, null, null);

        assertEquals(RoleCode.ORG_OWNER, decision.targetRoleCode());
        assertEquals(false, decision.requiresActorAssignmentValidation());
    }

    @Test
    void shouldResolveOrgUserByDefaultForAdminCreateWithoutRole() {
        UserRegistrationPolicy.RegistrationDecision decision =
                policy.evaluate(RegistrationMode.ADMIN_CREATE, null, "actor-1");

        assertEquals(RoleCode.ORG_USER, decision.targetRoleCode());
        assertTrue(decision.requiresActorAssignmentValidation());
    }

    @Test
    void shouldResolveRequestedRoleForAdminCreate() {
        UserRegistrationPolicy.RegistrationDecision decision =
                policy.evaluate(RegistrationMode.ADMIN_CREATE, "ORG_MANAGER", "actor-1");

        assertEquals(RoleCode.ORG_MANAGER, decision.targetRoleCode());
        assertTrue(decision.requiresActorAssignmentValidation());
    }

    @Test
    void shouldRejectAdminCreateWhenRoleIsOrgOwner() {
        assertThrows(
                RoleInvalidException.class,
                () -> policy.evaluate(RegistrationMode.ADMIN_CREATE, "ORG_OWNER", "actor-1"));
    }

    @Test
    void shouldRejectAdminCreateWhenRoleIsArkaAdmin() {
        assertThrows(
                RoleInvalidException.class,
                () -> policy.evaluate(RegistrationMode.ADMIN_CREATE, "ARKA_ADMIN", "actor-1"));
    }

    @Test
    void shouldRejectWhenAdminActorContextIsMissing() {
        assertThrows(
                OperationNotPermittedException.class,
                () -> policy.evaluate(RegistrationMode.ADMIN_CREATE, "ORG_USER", ""));
    }

    @Test
    void shouldRejectWhenRegistrationModeIsMissing() {
        assertThrows(
                OperationNotPermittedException.class,
                () -> policy.evaluate(null, "ORG_USER", "actor-1"));
    }
}
