package com.arka.identityaccess.infrastructure.config;

import com.arka.identityaccess.domain.access.service.InitialAccessProvisioningPolicy;
import com.arka.identityaccess.domain.access.service.AccessAssignmentPolicy;
import com.arka.identityaccess.domain.identity.service.AuthenticationSecurityPolicy;
import com.arka.identityaccess.domain.identity.service.CredentialPolicy;
import com.arka.identityaccess.domain.session.service.SessionPolicy;
import com.arka.identityaccess.domain.session.service.TokenPolicy;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainPolicyConfig {

    @Bean
    public CredentialPolicy credentialPolicy(
            @Value("${app.security.password.min-length:8}") int minPasswordLength) {
        return new CredentialPolicy(minPasswordLength);
    }

    @Bean
    public AuthenticationSecurityPolicy authenticationSecurityPolicy(
            @Value("${app.security.authentication.max-failed-attempts-before-block:5}") int maxFailedAttemptsBeforeBlock) {
        return new AuthenticationSecurityPolicy(maxFailedAttemptsBeforeBlock);
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
    public InitialAccessProvisioningPolicy initialAccessProvisioningPolicy() {
        return new InitialAccessProvisioningPolicy();
    }

    @Bean
    public AccessAssignmentPolicy accessAssignmentPolicy() {
        return new AccessAssignmentPolicy();
    }
}
