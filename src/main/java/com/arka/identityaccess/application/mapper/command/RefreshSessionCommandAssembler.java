package com.arka.identityaccess.application.mapper.command;

import com.arka.identityaccess.application.command.RefreshSessionCommand;
import com.arka.identityaccess.application.port.out.security.JwtVerificationPort;
import com.arka.identityaccess.domain.session.exception.SessionRefreshNotAllowedException;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RefreshSessionCommandAssembler {

    private final JwtVerificationPort jwtVerificationPort;

    public RefreshSessionCommandAssembler(JwtVerificationPort jwtVerificationPort) {
        this.jwtVerificationPort = jwtVerificationPort;
    }

    public Mono<RefreshJti> toRefreshJti(RefreshSessionCommand command) {
        return jwtVerificationPort.verify(command.refreshToken())
                .flatMap(verification -> {
                    if (!verification.valid()) {
                        return Mono.error(new SessionRefreshNotAllowedException());
                    }
                    if (!"refresh".equalsIgnoreCase(verification.tokenType())) {
                        return Mono.error(new SessionRefreshNotAllowedException());
                    }
                    if (verification.jti() == null || verification.jti().isBlank()) {
                        return Mono.error(new SessionRefreshNotAllowedException());
                    }
                    return Mono.just(RefreshJti.of(verification.jti()));
                });
    }
}
