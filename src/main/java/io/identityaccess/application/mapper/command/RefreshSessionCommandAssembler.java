package io.identityaccess.application.mapper.command;

import io.identityaccess.application.command.RefreshSessionCommand;
import io.identityaccess.application.port.out.security.JwtVerificationPort;
import io.identityaccess.application.port.out.security.JwtVerificationPort.VerificationResult;
import io.identityaccess.domain.exception.SessionRefreshNotAllowedException;
import io.identityaccess.domain.model.session.valueobject.RefreshJti;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RefreshSessionCommandAssembler {

    private final JwtVerificationPort jwtVerificationPort;

    public RefreshSessionCommandAssembler(JwtVerificationPort jwtVerificationPort) {
        this.jwtVerificationPort = jwtVerificationPort;
    }

    public Mono<RefreshJti> toRefreshJti(RefreshSessionCommand command) {
        return jwtVerificationPort
                .verify(command.refreshToken())
                .map(this::toRefreshJti);
    }

    private RefreshJti toRefreshJti(VerificationResult verification) {
        if (!verification.valid()) {
            throw new SessionRefreshNotAllowedException();
        }
        if (!"refresh".equalsIgnoreCase(verification.tokenType())) {
            throw new SessionRefreshNotAllowedException();
        }
        if (verification.jti() == null || verification.jti().isBlank()) {
            throw new SessionRefreshNotAllowedException();
        }
        return RefreshJti.of(verification.jti());
    }
}
