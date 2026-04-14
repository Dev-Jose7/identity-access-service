package com.arka.identityaccess.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.identityaccess.domain.access.aggregate.AccountAccessAggregate;
import com.arka.identityaccess.domain.access.aggregate.RoleAggregate;
import com.arka.identityaccess.domain.access.entity.AccessAssignment;
import com.arka.identityaccess.domain.access.enumtype.AccessAssignmentStatus;
import com.arka.identityaccess.domain.access.valueobject.AccessAssignmentId;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AccessSubmodelTest {

    @Test
    void shouldManageRolePermissionAndAssignmentLifecycle() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        RoleAggregate role = RoleAggregate.create(RoleCode.of("ORG_MANAGER"), "Organization Manager", now);
        assertTrue(role.grantPermission(PermissionCode.of("iam.user.read"), now.plusSeconds(5)));
        assertEquals(1, role.permissions().size());

        AccountAggregate account = AccountAggregate.register(
                EmailAddress.of("manager@arka.com"),
                "$2a$10$hash",
                false,
                now);
        AccountId actor = account.id();

        AccountAccessAggregate accountAccess = AccountAccessAggregate.empty(account.id());
        AccountAccessAggregate.RoleAssignmentDecision assigned = accountAccess.assignRole(
                role.id(),
                actor.value(),
                now.plusSeconds(20));
        assertTrue(assigned.assigned());
        assertTrue(accountAccess.hasActiveRole(role.id()));

        AccountAccessAggregate.RoleRevocationDecision revoked = accountAccess.revokeRole(
                role.id(),
                actor.value(),
                now.plusSeconds(30));
        assertTrue(revoked.revoked());
        assertEquals(0, accountAccess.resolveActiveRoles().size());
    }

    @Test
    void shouldRejectDuplicateActiveRoleInsideAccountAccessAggregate() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AccountId accountId = AccountId.of("usr-10");
        RoleAggregate role = RoleAggregate.create(RoleCode.of("ORG_USER"), "Organization User", now);

        AccessAssignment existing = AccessAssignment.rehydrate(
                AccessAssignmentId.of("11111111-1111-1111-1111-111111111111"),
                accountId,
                role.id(),
                AccessAssignmentStatus.ASSIGNED,
                "actor-1",
                now,
                null,
                null);
        AccountAccessAggregate accountAccess = AccountAccessAggregate.rehydrate(accountId, List.of(existing));

        AccountAccessAggregate.RoleAssignmentDecision duplicateDecision = accountAccess.assignRole(
                role.id(),
                "actor-2",
                now.plusSeconds(1));

        assertEquals(false, duplicateDecision.assigned());
        assertEquals(1, accountAccess.resolveActiveRoles().size());
    }
}
