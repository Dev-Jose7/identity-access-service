package com.arka.identityaccess.domain.model.session;

import com.arka.identityaccess.domain.event.DomainEvent;
import com.arka.identityaccess.domain.model.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.model.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.model.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.domain.model.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.model.session.event.SessionRefreshedEvent;
import com.arka.identityaccess.domain.model.session.event.SessionRevokedEvent;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.event.UserLoggedInEvent;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.domain.service.SessionPolicy;
import com.arka.identityaccess.domain.service.TokenPolicy;
import java.time.Instant;

public class SessionAggregate {

    private final SessionId id;
    private final UserId userId;
    private final ClientDevice clientDevice;
    private final ClientIp clientIp;
    private final AccessJti accessJti;
    private final RefreshJti refreshJti;
    private final SessionTimestamps timestamps;
    private final SessionStatus status;
    private final DomainEvent domainEvent;

    private SessionAggregate(
            SessionId id,
            UserId userId,
            ClientDevice clientDevice,
            ClientIp clientIp,
            AccessJti accessJti,
            RefreshJti refreshJti,
            SessionTimestamps timestamps,
            SessionStatus status,
            DomainEvent domainEvent) {
        this.id = id;
        this.userId = userId;
        this.clientDevice = clientDevice;
        this.clientIp = clientIp;
        this.accessJti = accessJti;
        this.refreshJti = refreshJti;
        this.timestamps = timestamps;
        this.status = status;
        this.domainEvent = domainEvent;
    }

    public static SessionAggregate open(
            UserAggregate user,
            ClientDevice clientDevice,
            ClientIp clientIp,
            Instant now,
            SessionPolicy sessionPolicy,
            TokenPolicy tokenPolicy) {
        sessionPolicy.ensureUserCanOpenSession(user);
        SessionId sessionId = SessionId.newId();
        AccessJti accessJti = AccessJti.newId();
        RefreshJti refreshJti = RefreshJti.newId();
        SessionTimestamps timestamps = sessionPolicy.createTimestamps(now, tokenPolicy);
        DomainEvent event = UserLoggedInEvent.create(
                user.id(),
                sessionId,
                accessJti,
                refreshJti,
                now);
        return new SessionAggregate(
                sessionId,
                user.id(),
                clientDevice,
                clientIp,
                accessJti,
                refreshJti,
                timestamps,
                SessionStatus.ACTIVE,
                event);
    }

    public SessionAggregate refresh(Instant now, SessionPolicy sessionPolicy, TokenPolicy tokenPolicy) {
        sessionPolicy.ensureSessionCanRefresh(this, now);
        AccessJti newAccessJti = AccessJti.newId();
        RefreshJti newRefreshJti = RefreshJti.newId();
        SessionTimestamps newTimestamps = sessionPolicy.refreshTimestamps(now, tokenPolicy);
        DomainEvent event = SessionRefreshedEvent.create(userId, id, accessJti, newAccessJti, refreshJti, newRefreshJti, now);
        return new SessionAggregate(
                id,
                userId,
                clientDevice,
                clientIp,
                newAccessJti,
                newRefreshJti,
                newTimestamps,
                SessionStatus.ACTIVE,
                event);
    }

    public SessionAggregate revoke(Instant now, SessionPolicy sessionPolicy) {
        sessionPolicy.ensureSessionCanLogout(this);
        DomainEvent event = SessionRevokedEvent.create(userId, id, now, "LOGOUT");
        return new SessionAggregate(
                id,
                userId,
                clientDevice,
                clientIp,
                accessJti,
                refreshJti,
                timestamps,
                SessionStatus.REVOKED,
                event);
    }

    public static SessionAggregate rehydrate(
            SessionId id,
            UserId userId,
            ClientDevice clientDevice,
            ClientIp clientIp,
            AccessJti accessJti,
            RefreshJti refreshJti,
            SessionTimestamps timestamps,
            SessionStatus status,
            DomainEvent domainEvent) {
        return new SessionAggregate(
                id,
                userId,
                clientDevice,
                clientIp,
                accessJti,
                refreshJti,
                timestamps,
                status,
                domainEvent);
    }

    public SessionId id() { return id; }
    public UserId userId() { return userId; }
    public ClientDevice clientDevice() { return clientDevice; }
    public ClientIp clientIp() { return clientIp; }
    public AccessJti accessJti() { return accessJti; }
    public RefreshJti refreshJti() { return refreshJti; }
    public SessionTimestamps timestamps() { return timestamps; }
    public SessionStatus status() { return status; }
    public DomainEvent domainEvent() { return domainEvent; }
}
