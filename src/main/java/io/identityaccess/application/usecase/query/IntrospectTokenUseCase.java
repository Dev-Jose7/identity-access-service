package io.identityaccess.application.usecase.query;

import io.identityaccess.application.query.IntrospectTokenQuery;
import io.identityaccess.application.port.in.IntrospectTokenQueryUseCase;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.port.out.security.JwtVerificationPort;
import io.identityaccess.application.result.IntrospectResult;
import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.session.valueobject.SessionId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class IntrospectTokenUseCase implements IntrospectTokenQueryUseCase {

    private final JwtVerificationPort jwtVerificationPort;
    private final SessionPersistencePort sessionPersistencePort;
    private final ClockPort clockPort;

    public IntrospectTokenUseCase(
            JwtVerificationPort jwtVerificationPort,
            SessionPersistencePort sessionPersistencePort,
            ClockPort clockPort) {
        this.jwtVerificationPort = jwtVerificationPort;
        this.sessionPersistencePort = sessionPersistencePort;
        this.clockPort = clockPort;
    }

    @Override
    public Mono<IntrospectResult> handle(IntrospectTokenQuery command) {
        return jwtVerificationPort.verify(command.token())
                .flatMap(verification -> {
                    if (!verification.valid()) {
                        return Mono.just(toResult(verification, false, verification.invalidReason()));
                    }
                    SessionId sessionId = SessionId.of(verification.sessionId());
                    return sessionPersistencePort.findOptionalBySessionId(sessionId)
                            .map(session -> isSessionBoundTokenActive(session, verification))
                            .defaultIfEmpty(false)
                            .map(active -> toResult(
                                    verification,
                                    active,
                                    active ? null : "session_inactive_or_mismatch"));
                });
    }

    private boolean isSessionBoundTokenActive(SessionAggregate session, JwtVerificationPort.VerificationResult verification) {
        if (session == null || !session.status().isActive()) {
            return false;
        }
        if (!session.userId().value().equals(verification.subject())) {
            return false;
        }
        if ("access".equalsIgnoreCase(verification.tokenType())) {
            return session.accessJti().value().equals(verification.jti())
                    && session.timestamps().accessTokenExpiresAt().isAfter(clockPort.now());
        }
        if ("refresh".equalsIgnoreCase(verification.tokenType())) {
            return session.refreshJti().value().equals(verification.jti())
                    && session.timestamps().refreshTokenExpiresAt().isAfter(clockPort.now());
        }
        return false;
    }

    private IntrospectResult toResult(
            JwtVerificationPort.VerificationResult verification,
            boolean active,
            String inactiveReason) {
        return new IntrospectResult(
                active,
                inactiveReason,
                verification.subject(),
                verification.sessionId(),
                verification.tokenType(),
                verification.issuer(),
                verification.audience(),
                verification.issuedAtEpochSecond(),
                verification.expiresAtEpochSecond(),
                verification.jti(),
                verification.email(),
                verification.roles(),
                verification.permissions());
    }
}
