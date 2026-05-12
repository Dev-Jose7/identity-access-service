package io.identityaccess.application.port.out.cache;

import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import reactor.core.publisher.Mono;

public interface SecurityRateLimitPort {

    Mono<Void> ensureLoginAllowed(EmailAddress email, ClientIp clientIp);

    Mono<Void> ensureRefreshAllowed(String refreshToken, ClientIp clientIp);

    Mono<Void> ensureRegistrationAllowed(EmailAddress email, ClientIp clientIp);
}
