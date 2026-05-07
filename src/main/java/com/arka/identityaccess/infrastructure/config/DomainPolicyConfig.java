package com.arka.identityaccess.infrastructure.config;

import com.arka.identityaccess.domain.service.PasswordPolicy;
import com.arka.identityaccess.domain.service.SessionPolicy;
import com.arka.identityaccess.domain.service.TokenPolicy;
import com.arka.identityaccess.domain.service.UserRegistrationPolicy;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainPolicyConfig {

    @Bean
    public PasswordPolicy passwordPolicy() {
        return new PasswordPolicy();
    }

    @Bean
    public SessionPolicy sessionPolicy() {
        return new SessionPolicy();
    }

    @Bean
    public TokenPolicy tokenPolicy(
            @Value("${app.security.jwt.access-token-ttl-seconds:900}") long accessTokenTtlSeconds,
            @Value("${app.security.jwt.refresh-token-ttl-seconds:604800}") long refreshTokenTtlSeconds) {
        return new TokenPolicy(Duration.ofSeconds(accessTokenTtlSeconds), Duration.ofSeconds(refreshTokenTtlSeconds));
    }

    @Bean
    public UserRegistrationPolicy userRegistrationPolicy() {
        return new UserRegistrationPolicy();
    }
}
