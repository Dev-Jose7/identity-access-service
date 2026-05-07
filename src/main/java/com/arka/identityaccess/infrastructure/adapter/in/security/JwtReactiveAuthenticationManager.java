package com.arka.identityaccess.infrastructure.adapter.in.security;

import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.infrastructure.adapter.out.security.JwtRsaKeyProvider;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final SessionPersistencePort sessionPersistencePort;
    private final SecurityPrincipalMapper securityPrincipalMapper;
    private final JwtRsaKeyProvider jwtRsaKeyProvider;
    private final String issuer;
    private final String audience;
    private final long clockSkewSeconds;

    public JwtReactiveAuthenticationManager(
            SessionPersistencePort sessionPersistencePort,
            SecurityPrincipalMapper securityPrincipalMapper,
            JwtRsaKeyProvider jwtRsaKeyProvider,
            @Value("${app.security.jwt.issuer:identity-access-service}") String issuer,
            @Value("${app.security.jwt.audience:arka-b2b}") String audience,
            @Value("${app.security.jwt.clock-skew-seconds:60}") long clockSkewSeconds) {
        this.sessionPersistencePort = sessionPersistencePort;
        this.securityPrincipalMapper = securityPrincipalMapper;
        this.jwtRsaKeyProvider = jwtRsaKeyProvider;
        this.issuer = issuer;
        this.audience = audience;
        this.clockSkewSeconds = clockSkewSeconds;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationTokenRequest tokenRequest)) {
            return Mono.empty();
        }

        JwtValidationResult validationResult = validateToken(tokenRequest.token());
        SessionId sessionId = SessionId.of(validationResult.sessionId());

        return sessionPersistencePort
                .findBySessionId(sessionId)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Session not found")))
                .flatMap(session -> {
                    if (!session.status().isActive()) {
                        return Mono.error(new BadCredentialsException("Session is not active"));
                    }
                    if (!session.userId().value().equals(validationResult.userId())) {
                        return Mono.error(new BadCredentialsException("Token subject does not match session user"));
                    }

                    Set<String> roleCodes = validationResult.roleCodes();
                    Set<String> permissionCodes = validationResult.permissionCodes();
                    Collection<GrantedAuthority> authorities =
                            securityPrincipalMapper.toAuthorities(roleCodes, permissionCodes);
                    IamSecurityPrincipal principal = securityPrincipalMapper.toPrincipal(
                            session.userId().value(),
                            session.id().value(),
                            validationResult.email(),
                            roleCodes);
                    return Mono.just((Authentication) new UsernamePasswordAuthenticationToken(
                            principal,
                            tokenRequest.token(),
                            authorities));
                })
                .onErrorMap(this::mapAuthenticationError);
    }

    private JwtValidationResult validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!JWSAlgorithm.RS256.equals(signedJWT.getHeader().getAlgorithm())) {
                throw new BadCredentialsException("Invalid token algorithm");
            }
            String tokenKid = signedJWT.getHeader().getKeyID();
            if (tokenKid == null || tokenKid.isBlank()) {
                throw new BadCredentialsException("Invalid token key id");
            }
            java.security.interfaces.RSAPublicKey verificationKey = jwtRsaKeyProvider
                    .findVerificationPublicKey(tokenKid)
                    .orElseThrow(() -> new BadCredentialsException("Unknown token key id"));
            if (!signedJWT.verify(new RSASSAVerifier(verificationKey))) {
                throw new BadCredentialsException("Invalid token signature");
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            validateClaims(claimsSet);

            String userId = claimsSet.getSubject();
            String sessionId = claimsSet.getStringClaim("sid");
            String email = claimsSet.getStringClaim("email");
            Set<String> roleCodes = extractStringSetClaim(claimsSet, "roles", true);
            Set<String> permissionCodes = extractStringSetClaim(claimsSet, "permissions", false);

            if (userId == null || userId.isBlank() || sessionId == null || sessionId.isBlank()) {
                throw new BadCredentialsException("Token is missing subject or session id");
            }
            if (email == null || email.isBlank()) {
                throw new BadCredentialsException("Token email claim is missing");
            }

            return new JwtValidationResult(userId, sessionId, email.trim(), roleCodes, permissionCodes);
        } catch (ParseException | JOSEException exception) {
            throw new BadCredentialsException("Token parsing failed", exception);
        }
    }

    private void validateClaims(JWTClaimsSet claimsSet) throws ParseException {
        Instant now = Instant.now();
        Instant allowedPast = now.minusSeconds(clockSkewSeconds);
        Instant allowedFuture = now.plusSeconds(clockSkewSeconds);

        if (claimsSet.getExpirationTime() == null || claimsSet.getExpirationTime().toInstant().isBefore(allowedPast)) {
            throw new BadCredentialsException("Token has expired");
        }
        if (claimsSet.getIssueTime() == null || claimsSet.getIssueTime().toInstant().isAfter(allowedFuture)) {
            throw new BadCredentialsException("Token issue time is invalid");
        }
        if (claimsSet.getJWTID() == null || claimsSet.getJWTID().isBlank()) {
            throw new BadCredentialsException("Token jti is missing");
        }

        String tokenType = claimsSet.getStringClaim("typ");
        if (tokenType == null || !"access".equalsIgnoreCase(tokenType)) {
            throw new BadCredentialsException("Token type is not allowed for authentication");
        }

        String tokenIssuer = claimsSet.getIssuer();
        if (tokenIssuer == null || !issuer.equals(tokenIssuer)) {
            throw new BadCredentialsException("Token issuer is invalid");
        }

        List<String> tokenAudience = claimsSet.getAudience();
        if (tokenAudience == null || !tokenAudience.contains(audience)) {
            throw new BadCredentialsException("Token audience is invalid");
        }
    }

    private AuthenticationException mapAuthenticationError(Throwable throwable) {
        if (throwable instanceof AuthenticationException authenticationException) {
            return authenticationException;
        }
        return new BadCredentialsException("Authentication failed", throwable);
    }

    private Set<String> extractStringSetClaim(JWTClaimsSet claimsSet, String claimName, boolean normalizeUppercase) throws ParseException {
        List<String> values = claimsSet.getStringListClaim(claimName);
        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String normalizedValue = normalizeUppercase
                    ? value.trim().toUpperCase()
                    : value.trim();
            if (normalizeUppercase && normalizedValue.startsWith("ROLE_")) {
                normalizedValue = normalizedValue.substring("ROLE_".length());
            }
            normalized.add(normalizedValue);
        }
        return Set.copyOf(normalized);
    }

    private record JwtValidationResult(
            String userId,
            String sessionId,
            String email,
            Set<String> roleCodes,
            Set<String> permissionCodes) {}
}
