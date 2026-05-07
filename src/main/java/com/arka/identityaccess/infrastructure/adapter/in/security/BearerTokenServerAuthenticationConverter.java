package com.arka.identityaccess.infrastructure.adapter.in.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class BearerTokenServerAuthenticationConverter implements ServerAuthenticationConverter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank() || !authorization.startsWith(BEARER_PREFIX)) {
            return Mono.empty();
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            return Mono.empty();
        }
        return Mono.just(new JwtAuthenticationTokenRequest(token));
    }
}
