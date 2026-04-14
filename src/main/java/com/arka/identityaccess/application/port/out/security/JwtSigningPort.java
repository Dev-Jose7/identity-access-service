package com.arka.identityaccess.application.port.out.security;

import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import reactor.core.publisher.Mono;

public interface JwtSigningPort {

    Mono<String> signAccessToken(SessionAggregate session, AccessProfile accessProfile);

    Mono<String> signRefreshToken(SessionAggregate session);
}
