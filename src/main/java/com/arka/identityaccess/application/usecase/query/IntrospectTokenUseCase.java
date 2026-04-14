package com.arka.identityaccess.application.usecase.query;

import com.arka.identityaccess.application.query.IntrospectTokenQuery;
import com.arka.identityaccess.application.port.in.IntrospectTokenQueryUseCase;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.port.out.security.JwtVerificationPort;
import com.arka.identityaccess.application.result.IntrospectResult;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
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
