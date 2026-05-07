package com.arka.identityaccess.infrastructure.adapter.out.security;

import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtSignerAdapter implements JwtSigningPort {

    private final JwtRsaKeyProvider jwtRsaKeyProvider;
    private final String issuer;
    private final String audience;

    public JwtSignerAdapter(
            JwtRsaKeyProvider jwtRsaKeyProvider,
            @Value("${app.security.jwt.issuer:identity-access-service}") String issuer,
            @Value("${app.security.jwt.audience:arka-b2b}") String audience) {
        this.jwtRsaKeyProvider = jwtRsaKeyProvider;
        this.issuer = issuer;
        this.audience = audience;
    }

    @Override
    public Mono<String> signAccessToken(SessionAggregate session, String email, Set<String> roles, Set<String> permissions) {
        return Mono.fromSupplier(() -> encodeAccessToken(session, email, roles, permissions));
    }

    @Override
    public Mono<String> signRefreshToken(SessionAggregate session) {
        return Mono.fromSupplier(() -> encodeRefreshToken(session));
    }

    private String encodeAccessToken(SessionAggregate session, String email, Set<String> roles, Set<String> permissions) {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(session.userId().value())
                .jwtID(session.accessJti().value())
                .issueTime(Date.from(session.timestamps().createdAt()))
                .expirationTime(Date.from(session.timestamps().accessTokenExpiresAt()))
                .claim("sid", session.id().value())
                .claim("typ", "access")
                .claim("email", normalizeEmail(email))
                .claim("roles", normalizeRoles(roles))
                .claim("permissions", normalizePermissions(permissions))
                .build();
        return sign(claimsSet);
    }

    private String encodeRefreshToken(SessionAggregate session) {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(session.userId().value())
                .jwtID(session.refreshJti().value())
                .issueTime(Date.from(session.timestamps().createdAt()))
                .expirationTime(Date.from(session.timestamps().refreshTokenExpiresAt()))
                .claim("sid", session.id().value())
                .claim("typ", "refresh")
                .build();
        return sign(claimsSet);
    }

    private String sign(JWTClaimsSet claimsSet) {
        try {
            JWSSigner signer = new RSASSASigner(jwtRsaKeyProvider.privateKey());
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .type(JOSEObjectType.JWT)
                            .keyID(jwtRsaKeyProvider.keyId())
                            .build(),
                    claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Unable to sign JWT token", exception);
        }
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.trim().toUpperCase())
                .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private Set<String> normalizePermissions(Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Set.of();
        }
        return permissions.stream()
                .filter(permission -> permission != null && !permission.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email is required for access token claim");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
