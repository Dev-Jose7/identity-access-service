package com.arka.identityaccess.application.port.out.cache;

import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import reactor.core.publisher.Mono;

public interface SecurityRateLimitPort {

    Mono<Void> ensureLoginAllowed(EmailAddress email, ClientIp clientIp);

    Mono<Void> ensureRefreshAllowed(String refreshToken, ClientIp clientIp);

    Mono<Void> ensureFounderRegistrationAllowed(EmailAddress email, ClientIp clientIp);
}
