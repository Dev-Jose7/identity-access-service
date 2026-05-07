package com.arka.identityaccess.application.port.out.security;

import com.arka.identityaccess.domain.model.session.SessionAggregate;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface JwtSigningPort {

    Mono<String> signAccessToken(SessionAggregate session, String email, Set<String> roles, Set<String> permissions);

    Mono<String> signRefreshToken(SessionAggregate session);
}
