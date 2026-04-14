package com.arka.identityaccess.infrastructure.adapter.out.security;

import com.arka.identityaccess.application.port.out.security.PasswordHashPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BCryptPasswordHasherAdapter implements PasswordHashPort {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Mono<Boolean> matches(String rawPassword, String passwordHash) {
        return Mono.fromSupplier(() -> passwordEncoder.matches(rawPassword, passwordHash));
    }

    @Override
    public Mono<String> hash(String rawPassword) {
        return Mono.fromSupplier(() -> passwordEncoder.encode(rawPassword));
    }
}
