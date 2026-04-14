package com.arka.identityaccess.domain.access.aggregate;

import com.arka.identityaccess.domain.access.enumtype.PermissionStatus;
import com.arka.identityaccess.domain.access.enumtype.PermissionScope;
import com.arka.identityaccess.domain.access.event.PermissionCreatedEvent;
import com.arka.identityaccess.domain.access.event.PermissionDisabledEvent;
import com.arka.identityaccess.domain.access.event.PermissionUpdatedEvent;
import com.arka.identityaccess.domain.access.valueobject.PermissionAction;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.PermissionId;
import com.arka.identityaccess.domain.access.valueobject.PermissionResource;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PermissionAggregate {

    private final PermissionId id;
    private final PermissionCode code;
    private final PermissionResource resource;
    private final PermissionAction action;
    private final PermissionScope scope;
    private String displayName;
    private PermissionStatus status;
    private final List<DomainEvent> domainEvents;

    private PermissionAggregate(
            PermissionId id,
            PermissionCode code,
            PermissionResource resource,
            PermissionAction action,
            PermissionScope scope,
            String displayName,
            PermissionStatus status,
            List<DomainEvent> domainEvents) {
        if (id == null
                || code == null
                || resource == null
                || action == null
                || scope == null
                || displayName == null
                || displayName.isBlank()
                || status == null) {
            throw new DomainInvariantViolationException("permission aggregate is incomplete");
        }
        this.id = id;
        this.code = code;
        this.resource = resource;
        this.action = action;
        this.scope = scope;
        this.displayName = displayName.trim();
        this.status = status;
        this.domainEvents = new ArrayList<>(domainEvents == null ? List.of() : domainEvents);
    }

    public static PermissionAggregate create(
            PermissionCode code,
            PermissionResource resource,
            PermissionAction action,
            PermissionScope scope,
            String displayName,
            Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("permission creation requires occurredAt");
        }
        PermissionId permissionId = PermissionId.newId();
        return new PermissionAggregate(
                permissionId,
                code,
                resource,
                action,
                scope,
                displayName,
                PermissionStatus.ACTIVE,
                List.of(PermissionCreatedEvent.create(permissionId, code, resource, action, scope, occurredAt)));
    }

    public static PermissionAggregate rehydrate(
            PermissionId id,
            PermissionCode code,
            PermissionResource resource,
            PermissionAction action,
            PermissionScope scope,
            String displayName,
            PermissionStatus status) {
        return new PermissionAggregate(id, code, resource, action, scope, displayName, status, List.of());
    }

    public boolean updateDisplayName(String nextDisplayName, Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("permission update requires occurredAt");
        }
        if (nextDisplayName == null || nextDisplayName.isBlank()) {
            throw new DomainInvariantViolationException("permission displayName is required");
        }
        if (this.displayName.equals(nextDisplayName.trim())) {
            return false;
        }
        this.displayName = nextDisplayName.trim();
        recordDomainEvent(PermissionUpdatedEvent.create(id, displayName, occurredAt));
        return true;
    }

    public boolean disable(Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("permission disable requires occurredAt");
        }
        if (status == PermissionStatus.DISABLED) {
            return false;
        }
        status = PermissionStatus.DISABLED;
        recordDomainEvent(PermissionDisabledEvent.create(id, occurredAt));
        return true;
    }

    private void recordDomainEvent(DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    public PermissionId id() {
        return id;
    }

    public PermissionCode code() {
        return code;
    }

    public PermissionResource resource() {
        return resource;
    }

    public PermissionAction action() {
        return action;
    }

    public PermissionScope scope() {
        return scope;
    }

    public String displayName() {
        return displayName;
    }

    public PermissionStatus status() {
        return status;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
