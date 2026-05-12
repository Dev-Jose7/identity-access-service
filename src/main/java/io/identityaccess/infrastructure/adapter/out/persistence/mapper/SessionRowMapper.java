package io.identityaccess.infrastructure.adapter.out.persistence.mapper;

import io.identityaccess.domain.event.DomainEvent;
import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.session.enumtype.SessionStatus;
import io.identityaccess.domain.model.session.valueobject.AccessJti;
import io.identityaccess.domain.model.session.valueobject.ClientDevice;
import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.model.session.valueobject.RefreshJti;
import io.identityaccess.domain.model.session.valueobject.SessionId;
import io.identityaccess.domain.model.session.valueobject.SessionTimestamps;
import io.identityaccess.domain.model.user.valueobject.UserId;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.SessionRow;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SessionRowMapper {

    public SessionRow toRow(SessionAggregate session) {
        Instant now = Instant.now();
        Instant revokedAt = session.status() == SessionStatus.REVOKED ? now : null;
        return new SessionRow(
                session.id().value(),
                session.userId().value(),
                session.clientDevice().deviceId(),
                session.clientDevice().deviceName(),
                session.clientDevice().deviceType(),
                session.clientIp().value(),
                session.accessJti().value(),
                session.refreshJti().value(),
                session.timestamps().createdAt(),
                session.timestamps().accessTokenExpiresAt(),
                session.timestamps().refreshTokenExpiresAt(),
                now,
                session.status().name(),
                revokedAt,
                session.status() == SessionStatus.REVOKED ? "LOGOUT" : null,
                now,
                now);
    }

    public SessionAggregate toAggregate(SessionRow row) { return toAggregate(row, null); }

    public SessionAggregate toAggregate(SessionRow row, DomainEvent domainEvent) {
        return SessionAggregate.rehydrate(
                SessionId.of(row.sessionId()),
                UserId.of(row.userId()),
                ClientDevice.of(row.deviceId(), row.deviceName(), row.deviceType()),
                ClientIp.of(row.ipAddress()),
                AccessJti.of(row.accessJti()),
                RefreshJti.of(row.refreshJti()),
                SessionTimestamps.of(row.issuedAt(), row.accessTokenExpiresAt(), row.refreshTokenExpiresAt()),
                SessionStatus.valueOf(row.status().toUpperCase()),
                domainEvent);
    }
}
