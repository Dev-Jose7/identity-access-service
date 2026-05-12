package io.identityaccess.application.usecase.query;

import io.identityaccess.application.port.in.ListSessionsQueryUseCase;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.result.SessionSummaryResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ListSessionsUseCase implements ListSessionsQueryUseCase {

    private final SessionPersistencePort sessionPersistencePort;
    private final ClockPort clockPort;

    public ListSessionsUseCase(SessionPersistencePort sessionPersistencePort, ClockPort clockPort) {
        this.sessionPersistencePort = sessionPersistencePort;
        this.clockPort = clockPort;
    }

    @Override
    public Flux<SessionSummaryResult> handle() {
        return sessionPersistencePort.expireExpiredSessions(clockPort.now())
                .thenMany(sessionPersistencePort.listSessions())
                .map(session -> new SessionSummaryResult(
                        session.sessionId(),
                        session.userId(),
                        session.status(),
                        session.ipAddress(),
                        session.deviceId(),
                        session.deviceName(),
                        session.deviceType(),
                        session.issuedAt(),
                        session.accessTokenExpiresAt(),
                        session.refreshTokenExpiresAt(),
                        session.lastSeenAt(),
                        session.revokedAt(),
                        session.revocationReason(),
                        session.createdAt(),
                        session.updatedAt()));
    }
}
