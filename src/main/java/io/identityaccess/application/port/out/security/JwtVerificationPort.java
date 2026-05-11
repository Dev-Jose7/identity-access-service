package io.identityaccess.application.port.out.security;

import java.util.List;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface JwtVerificationPort {

    Mono<VerificationResult> verify(String token);

    record VerificationResult(
            boolean valid,
            String invalidReason,
            String subject,
            String sessionId,
            String tokenType,
            String issuer,
            List<String> audience,
            Long issuedAtEpochSecond,
            Long expiresAtEpochSecond,
            String jti,
            String email,
            Set<String> roles,
            Set<String> permissions) {

        public static VerificationResult invalid(String reason) {
            return new VerificationResult(
                    false,
                    reason,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    Set.of(),
                    Set.of());
        }

        public static VerificationResult valid(
                String subject,
                String sessionId,
                String tokenType,
                String issuer,
                List<String> audience,
                Long issuedAtEpochSecond,
                Long expiresAtEpochSecond,
                String jti,
                String email,
                Set<String> roles,
                Set<String> permissions) {
            return new VerificationResult(
                    true,
                    null,
                    subject,
                    sessionId,
                    tokenType,
                    issuer,
                    audience == null ? List.of() : List.copyOf(audience),
                    issuedAtEpochSecond,
                    expiresAtEpochSecond,
                    jti,
                    email,
                    roles == null ? Set.of() : Set.copyOf(roles),
                    permissions == null ? Set.of() : Set.copyOf(permissions));
        }
    }
}
