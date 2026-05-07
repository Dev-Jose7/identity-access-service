package com.arka.identityaccess.application.port.out.security;

import reactor.core.publisher.Mono;

public interface PasswordHashPort {

    Mono<Boolean> matches(String rawPassword, String passwordHash);

    Mono<String> hash(String rawPassword);
}
