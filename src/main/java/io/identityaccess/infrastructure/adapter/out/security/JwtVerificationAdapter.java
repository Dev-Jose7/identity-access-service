package io.identityaccess.infrastructure.adapter.out.security;

import io.identityaccess.application.port.out.security.JwtVerificationPort;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtVerificationAdapter implements JwtVerificationPort {

    private final JwtRsaKeyProvider jwtRsaKeyProvider;
    private final String issuer;
    private final String audience;
    private final long clockSkewSeconds;

    public JwtVerificationAdapter(
            JwtRsaKeyProvider jwtRsaKeyProvider,
            @Value("${app.security.jwt.issuer:identity-access-service}") String issuer,
            @Value("${app.security.jwt.audience:identity-access-client}") String audience,
            @Value("${app.security.jwt.clock-skew-seconds:60}") long clockSkewSeconds) {
        this.jwtRsaKeyProvider = jwtRsaKeyProvider;
        this.issuer = issuer;
        this.audience = audience;
        this.clockSkewSeconds = clockSkewSeconds;
    }

    @Override
    public Mono<VerificationResult> verify(String token) {
        return Mono.fromSupplier(() -> verifyBlocking(token));
    }

    private VerificationResult verifyBlocking(String token) {
        if (token == null || token.isBlank()) {
            return VerificationResult.invalid("token_missing");
        }
        try {
            SignedJWT signedJwt = SignedJWT.parse(token.trim());
            if (!JWSAlgorithm.RS256.equals(signedJwt.getHeader().getAlgorithm())) {
                return VerificationResult.invalid("invalid_algorithm");
            }

            String kid = signedJwt.getHeader().getKeyID();
            if (kid == null || kid.isBlank()) {
                return VerificationResult.invalid("invalid_kid");
            }
            var verificationKey = jwtRsaKeyProvider.findVerificationPublicKey(kid);
            if (verificationKey.isEmpty()) {
                return VerificationResult.invalid("unknown_kid");
            }
            if (!signedJwt.verify(new RSASSAVerifier(verificationKey.get()))) {
                return VerificationResult.invalid("invalid_signature");
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            String subject = normalize(claims.getSubject());
            String sessionId = normalize(claims.getStringClaim("sid"));
            String tokenType = normalize(claims.getStringClaim("typ"));
            String jwtId = normalize(claims.getJWTID());
            if (subject == null || sessionId == null || tokenType == null || jwtId == null) {
                return VerificationResult.invalid("invalid_claims");
            }

            if (!issuer.equals(claims.getIssuer())) {
                return VerificationResult.invalid("invalid_issuer");
            }
            List<String> tokenAudience = claims.getAudience();
            if (tokenAudience == null || !tokenAudience.contains(audience)) {
                return VerificationResult.invalid("invalid_audience");
            }

            Instant now = Instant.now();
            Instant allowedPast = now.minusSeconds(clockSkewSeconds);
            Instant allowedFuture = now.plusSeconds(clockSkewSeconds);

            if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(allowedPast)) {
                return VerificationResult.invalid("token_expired");
            }
            if (claims.getIssueTime() == null || claims.getIssueTime().toInstant().isAfter(allowedFuture)) {
                return VerificationResult.invalid("invalid_iat");
            }

            String email = normalize(claims.getStringClaim("email"));
            Set<String> roles = extractClaimValues(claims, "roles", true);
            Set<String> permissions = extractClaimValues(claims, "permissions", false);

            return VerificationResult.valid(
                    subject,
                    sessionId,
                    tokenType,
                    claims.getIssuer(),
                    tokenAudience,
                    claims.getIssueTime().toInstant().getEpochSecond(),
                    claims.getExpirationTime().toInstant().getEpochSecond(),
                    jwtId,
                    email,
                    roles,
                    permissions);
        } catch (ParseException | JOSEException exception) {
            return VerificationResult.invalid("malformed_token");
        }
    }

    private Set<String> extractClaimValues(JWTClaimsSet claims, String claimName, boolean normalizeUppercase) throws ParseException {
        List<String> values = claims.getStringListClaim(claimName);
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String sanitized = normalize(value);
            if (sanitized == null) {
                continue;
            }
            String candidate = normalizeUppercase ? sanitized.toUpperCase() : sanitized;
            if (normalizeUppercase && candidate.startsWith("ROLE_")) {
                candidate = candidate.substring("ROLE_".length());
            }
            normalized.add(candidate);
        }
        return Set.copyOf(normalized);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
