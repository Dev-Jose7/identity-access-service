package io.identityaccess.infrastructure.config;

import io.identityaccess.domain.service.PasswordPolicy;
import io.identityaccess.domain.service.SessionPolicy;
import io.identityaccess.domain.service.TokenPolicy;
import io.identityaccess.domain.service.UserRegistrationPolicy;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
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
    public UserRegistrationPolicy userRegistrationPolicy(
            @Value("${app.iam.registration.public-enabled:true}") boolean publicRegistrationEnabled,
            @Value("${app.iam.registration.primary-enabled:true}") boolean primaryRegistrationEnabled,
            @Value("${app.iam.registration.primary-initial-role-code:SYSTEM_ADMIN}") String primaryInitialRoleCode,
            @Value("${app.iam.registration.public-default-role-code:ACCOUNT_USER}") String publicDefaultRoleCode,
            @Value("${app.iam.registration.admin-default-role-code:ACCOUNT_USER}") String adminDefaultRoleCode,
            @Value("${app.iam.registration.protected-role-codes:SYSTEM_ADMIN}") String protectedRoleCodes) {
        return new UserRegistrationPolicy(
                publicRegistrationEnabled,
                primaryRegistrationEnabled,
                primaryInitialRoleCode,
                publicDefaultRoleCode,
                adminDefaultRoleCode,
                parseCsv(protectedRoleCodes));
    }

    private Set<String> parseCsv(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
