package io.identityaccess.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.exception.RoleInvalidException;
import io.identityaccess.domain.model.role.RoleCode;
import io.identityaccess.domain.model.user.RegistrationMode;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserRegistrationPolicyTest {

    private final UserRegistrationPolicy policy = new UserRegistrationPolicy();

    @Test
    void shouldResolveSystemAdminForPrimaryRegistration() {
        UserRegistrationPolicy.RegistrationDecision decision =
                policy.evaluate(RegistrationMode.PRIMARY_REGISTRATION, null, null);

        assertEquals(RoleCode.of("SYSTEM_ADMIN"), decision.targetRoleCode());
        assertFalse(decision.requiresActorAssignmentValidation());
        assertTrue(decision.primaryRegistration());
    }

    @Test
    void shouldResolveAccountUserForPublicSelfRegistration() {
        UserRegistrationPolicy.RegistrationDecision decision =
                policy.evaluate(RegistrationMode.PUBLIC_SELF_REGISTRATION, null, null);

        assertEquals(RoleCode.of("ACCOUNT_USER"), decision.targetRoleCode());
        assertFalse(decision.requiresActorAssignmentValidation());
        assertFalse(decision.primaryRegistration());
    }

    @Test
    void shouldRejectWhenPublicDefaultRoleIsProtected() {
        assertThrows(
                RoleInvalidException.class,
                () -> new UserRegistrationPolicy(
                        true,
                        true,
                        "SYSTEM_ADMIN",
                        "SYSTEM_ADMIN",
                        "ACCOUNT_USER",
                        Set.of("SYSTEM_ADMIN")));
    }

    @Test
    void shouldRejectWhenPrimaryRegistrationIsDisabled() {
        UserRegistrationPolicy disabledPrimaryPolicy = new UserRegistrationPolicy(
                true,
                false,
                "SYSTEM_ADMIN",
                "ACCOUNT_USER",
                "ACCOUNT_USER",
                Set.of("SYSTEM_ADMIN"));

        assertThrows(
                OperationNotPermittedException.class,
                () -> disabledPrimaryPolicy.evaluate(RegistrationMode.PRIMARY_REGISTRATION, null, null));
    }

    @Test
    void shouldRejectWhenPublicRegistrationIsDisabled() {
        UserRegistrationPolicy disabledPublicPolicy = new UserRegistrationPolicy(
                false,
                true,
                "SYSTEM_ADMIN",
                "ACCOUNT_USER",
                "ACCOUNT_USER",
                Set.of("SYSTEM_ADMIN"));

        assertThrows(
                OperationNotPermittedException.class,
                () -> disabledPublicPolicy.evaluate(RegistrationMode.PUBLIC_SELF_REGISTRATION, null, null));
    }

    @Test
    void shouldResolveAccountUserByDefaultForAdminCreateWithoutRole() {
        UserRegistrationPolicy.RegistrationDecision decision =
                policy.evaluate(RegistrationMode.ADMIN_CREATE, null, "actor-1");

        assertEquals(RoleCode.of("ACCOUNT_USER"), decision.targetRoleCode());
        assertTrue(decision.requiresActorAssignmentValidation());
        assertFalse(decision.primaryRegistration());
    }

    @Test
    void shouldResolveRequestedRoleForAdminCreate() {
        UserRegistrationPolicy.RegistrationDecision decision =
                policy.evaluate(RegistrationMode.ADMIN_CREATE, "ACCESS_MANAGER", "actor-1");

        assertEquals(RoleCode.of("ACCESS_MANAGER"), decision.targetRoleCode());
        assertTrue(decision.requiresActorAssignmentValidation());
        assertFalse(decision.primaryRegistration());
    }

    @Test
    void shouldRejectAdminCreateWhenRoleIsProtected() {
        assertThrows(
                RoleInvalidException.class,
                () -> policy.evaluate(RegistrationMode.ADMIN_CREATE, "SYSTEM_ADMIN", "actor-1"));
    }

    @Test
    void shouldRejectWhenAdminActorContextIsMissing() {
        assertThrows(
                OperationNotPermittedException.class,
                () -> policy.evaluate(RegistrationMode.ADMIN_CREATE, "ACCOUNT_USER", ""));
    }

    @Test
    void shouldRejectWhenRegistrationModeIsMissing() {
        assertThrows(
                OperationNotPermittedException.class,
                () -> policy.evaluate(null, "ACCOUNT_USER", "actor-1"));
    }
}
