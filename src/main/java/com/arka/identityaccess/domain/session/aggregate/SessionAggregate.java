package com.arka.identityaccess.domain.session.aggregate;

import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.session.exception.SessionAlreadyRevokedException;
import com.arka.identityaccess.domain.session.exception.SessionRefreshNotAllowedException;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.session.event.SessionRefreshedEvent;
import com.arka.identityaccess.domain.session.event.SessionRevokedEvent;
import com.arka.identityaccess.domain.session.event.SessionOpenedEvent;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionAggregate {

    private final SessionId id;
    private final AccountId userId;
    private final ClientDevice clientDevice;
    private final ClientIp clientIp;
    private final AccessJti accessJti;
    private final RefreshJti refreshJti;
    private final SessionTimestamps timestamps;
    private final SessionStatus status;
    private final List<DomainEvent> domainEvents;

    private SessionAggregate(
            SessionId id,
            AccountId userId,
            ClientDevice clientDevice,
            ClientIp clientIp,
            AccessJti accessJti,
            RefreshJti refreshJti,
            SessionTimestamps timestamps,
            SessionStatus status,
            List<DomainEvent> domainEvents) {
        if (id == null
                || userId == null
                || clientDevice == null
                || clientIp == null
                || accessJti == null
                || refreshJti == null
                || timestamps == null
                || status == null) {
            throw new DomainInvariantViolationException("session aggregate is incomplete");
        }
        this.id = id;
        this.userId = userId;
        this.clientDevice = clientDevice;
        this.clientIp = clientIp;
        this.accessJti = accessJti;
        this.refreshJti = refreshJti;
        this.timestamps = timestamps;
        this.status = status;
        this.domainEvents = new ArrayList<>(domainEvents == null ? List.of() : domainEvents);
    }

    public static SessionAggregate open(
            AccountId accountId,
            ClientDevice clientDevice,
            ClientIp clientIp,
            Instant issuedAt,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt) {
        if (accountId == null || issuedAt == null || accessTokenExpiresAt == null || refreshTokenExpiresAt == null) {
            throw new DomainInvariantViolationException(
                    "session opening requires accountId and explicit token expirations");
        }
        SessionTimestamps timestamps = SessionTimestamps.of(issuedAt, accessTokenExpiresAt, refreshTokenExpiresAt);
        SessionId sessionId = SessionId.newId();
        AccessJti accessJti = AccessJti.newId();
        RefreshJti refreshJti = RefreshJti.newId();
        DomainEvent event = SessionOpenedEvent.create(
                accountId,
                sessionId,
                accessJti,
                refreshJti,
                issuedAt);
        return new SessionAggregate(
                sessionId,
                accountId,
                clientDevice,
                clientIp,
                accessJti,
                refreshJti,
                timestamps,
                SessionStatus.ACTIVE,
                List.of(event));
    }

    public SessionAggregate refresh(
            Instant occurredAt,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt) {
        return refresh(occurredAt, accessTokenExpiresAt, refreshTokenExpiresAt, true);
    }

    public SessionAggregate refresh(
            Instant occurredAt,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt,
            boolean rotateRefreshToken) {
        if (status != SessionStatus.ACTIVE) {
            throw new SessionRefreshNotAllowedException();
        }
        if (occurredAt == null || accessTokenExpiresAt == null || refreshTokenExpiresAt == null) {
            throw new DomainInvariantViolationException(
                    "session refresh requires occurredAt and explicit token expirations");
        }
        SessionTimestamps refreshedTimestamps = SessionTimestamps.of(
                occurredAt,
                accessTokenExpiresAt,
                refreshTokenExpiresAt);
        AccessJti newAccessJti = AccessJti.newId();
        RefreshJti newRefreshJti = rotateRefreshToken ? RefreshJti.newId() : refreshJti;
        DomainEvent event = SessionRefreshedEvent.create(
                userId,
                id,
                accessJti,
                newAccessJti,
                refreshJti,
                newRefreshJti,
                occurredAt);
        return new SessionAggregate(
                id,
                userId,
                clientDevice,
                clientIp,
                newAccessJti,
                newRefreshJti,
                refreshedTimestamps,
                SessionStatus.ACTIVE,
                List.of(event));
    }

    public SessionAggregate revoke(Instant occurredAt, String reason) {
        if (status != SessionStatus.ACTIVE) {
            throw new SessionAlreadyRevokedException();
        }
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("session revocation requires occurredAt");
        }
        DomainEvent event = SessionRevokedEvent.create(userId, id, occurredAt, reason);
        return new SessionAggregate(
                id,
                userId,
                clientDevice,
                clientIp,
                accessJti,
                refreshJti,
                timestamps,
                SessionStatus.REVOKED,
                List.of(event));
    }

    public static SessionAggregate rehydrate(
            SessionId id,
            AccountId userId,
            ClientDevice clientDevice,
            ClientIp clientIp,
            AccessJti accessJti,
            RefreshJti refreshJti,
            SessionTimestamps timestamps,
            SessionStatus status) {
        return new SessionAggregate(
                id,
                userId,
                clientDevice,
                clientIp,
                accessJti,
                refreshJti,
                timestamps,
                status,
                List.of());
    }

    public SessionId id() { return id; }
    public AccountId userId() { return userId; }
    public ClientDevice clientDevice() { return clientDevice; }
    public ClientIp clientIp() { return clientIp; }
    public AccessJti accessJti() { return accessJti; }
    public RefreshJti refreshJti() { return refreshJti; }
    public SessionTimestamps timestamps() { return timestamps; }
    public SessionStatus status() { return status; }
    public List<DomainEvent> domainEvents() { return Collections.unmodifiableList(domainEvents); }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
