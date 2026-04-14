package com.arka.identityaccess.domain.access.aggregate;

import com.arka.identityaccess.domain.access.enumtype.RoleStatus;
import com.arka.identityaccess.domain.access.event.PermissionGrantedToRoleEvent;
import com.arka.identityaccess.domain.access.event.PermissionRevokedFromRoleEvent;
import com.arka.identityaccess.domain.access.event.RoleCreatedEvent;
import com.arka.identityaccess.domain.access.event.RoleDisabledEvent;
import com.arka.identityaccess.domain.access.event.RoleUpdatedEvent;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RoleAggregate {

    private final RoleId id;
    private final RoleCode code;
    private String displayName;
    private RoleStatus status;
    private final Set<PermissionCode> permissions;
    private final List<DomainEvent> domainEvents;

    private RoleAggregate(
            RoleId id,
            RoleCode code,
            String displayName,
            RoleStatus status,
            Set<PermissionCode> permissions,
            List<DomainEvent> domainEvents) {
        if (id == null || code == null || displayName == null || displayName.isBlank() || status == null) {
            throw new DomainInvariantViolationException("role aggregate is incomplete");
        }
        this.id = id;
        this.code = code;
        this.displayName = displayName.trim();
        this.status = status;
        this.permissions = new LinkedHashSet<>(permissions == null ? Set.of() : permissions);
        this.domainEvents = new ArrayList<>(domainEvents == null ? List.of() : domainEvents);
    }

    public static RoleAggregate create(RoleCode code, String displayName, Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("role creation requires occurredAt");
        }
        RoleId roleId = RoleId.newId();
        return new RoleAggregate(
                roleId,
                code,
                displayName,
                RoleStatus.ACTIVE,
                Set.of(),
                List.of(RoleCreatedEvent.create(roleId, code, occurredAt)));
    }

    public static RoleAggregate rehydrate(
            RoleId id,
            RoleCode code,
            String displayName,
            RoleStatus status,
            Set<PermissionCode> permissions) {
        return new RoleAggregate(id, code, displayName, status, permissions, List.of());
    }

    public boolean updateDisplayName(String nextDisplayName, Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("role update requires occurredAt");
        }
        if (nextDisplayName == null || nextDisplayName.isBlank()) {
            throw new DomainInvariantViolationException("role displayName is required");
        }
        if (this.displayName.equals(nextDisplayName.trim())) {
            return false;
        }
        this.displayName = nextDisplayName.trim();
        recordDomainEvent(RoleUpdatedEvent.create(id, occurredAt));
        return true;
    }

    public boolean disable(Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("role disable requires occurredAt");
        }
        if (status == RoleStatus.DISABLED) {
            return false;
        }
        status = RoleStatus.DISABLED;
        recordDomainEvent(RoleDisabledEvent.create(id, occurredAt));
        return true;
    }

    public boolean grantPermission(PermissionCode permissionCode, Instant occurredAt) {
        if (permissionCode == null || occurredAt == null) {
            throw new DomainInvariantViolationException("permission grant requires permissionCode and occurredAt");
        }
        if (!status.isAssignable()) {
            throw new DomainInvariantViolationException("cannot grant permissions to disabled role");
        }
        if (permissions.contains(permissionCode)) {
            return false;
        }
        permissions.add(permissionCode);
        recordDomainEvent(PermissionGrantedToRoleEvent.create(id, permissionCode, occurredAt));
        return true;
    }

    public boolean revokePermission(PermissionCode permissionCode, Instant occurredAt) {
        if (permissionCode == null || occurredAt == null) {
            throw new DomainInvariantViolationException("permission revoke requires permissionCode and occurredAt");
        }
        if (!permissions.contains(permissionCode)) {
            return false;
        }
        permissions.remove(permissionCode);
        recordDomainEvent(PermissionRevokedFromRoleEvent.create(id, permissionCode, occurredAt));
        return true;
    }

    private void recordDomainEvent(DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    public RoleId id() {
        return id;
    }

    public RoleCode code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public RoleStatus status() {
        return status;
    }

    public Set<PermissionCode> permissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
