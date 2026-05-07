package com.arka.identityaccess.application.mapper.command;

import com.arka.identityaccess.application.command.RefreshSessionCommand;
import com.arka.identityaccess.domain.exception.SessionRefreshNotAllowedException;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.arka.identityaccess.infrastructure.adapter.out.security.JwtRsaKeyProvider;

@Component
public class RefreshSessionCommandAssembler {

    private final JwtRsaKeyProvider jwtRsaKeyProvider;
    private final String issuer;
    private final String audience;
    private final long clockSkewSeconds;

    public RefreshSessionCommandAssembler(
            JwtRsaKeyProvider jwtRsaKeyProvider,
            @Value("${app.security.jwt.issuer:identity-access-service}") String issuer,
            @Value("${app.security.jwt.audience:arka-b2b}") String audience,
            @Value("${app.security.jwt.clock-skew-seconds:60}") long clockSkewSeconds) {
        this.jwtRsaKeyProvider = jwtRsaKeyProvider;
        this.issuer = issuer;
        this.audience = audience;
        this.clockSkewSeconds = clockSkewSeconds;
    }

    public RefreshJti toRefreshJti(RefreshSessionCommand command) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(command.refreshToken());
            if (!JWSAlgorithm.RS256.equals(signedJWT.getHeader().getAlgorithm())) {
                throw new SessionRefreshNotAllowedException();
            }
            String tokenKid = signedJWT.getHeader().getKeyID();
            if (tokenKid == null || tokenKid.isBlank()) {
                throw new SessionRefreshNotAllowedException();
            }
            java.security.interfaces.RSAPublicKey verificationKey = jwtRsaKeyProvider
                    .findVerificationPublicKey(tokenKid)
                    .orElseThrow(SessionRefreshNotAllowedException::new);
            if (!signedJWT.verify(new RSASSAVerifier(verificationKey))) {
                throw new SessionRefreshNotAllowedException();
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            validateRefreshClaims(claimsSet);

            String jti = claimsSet.getJWTID();
            if (jti == null || jti.isBlank()) {
                throw new SessionRefreshNotAllowedException();
            }
            return RefreshJti.of(jti);
        } catch (ParseException | JOSEException exception) {
            throw new SessionRefreshNotAllowedException();
        }
    }

    private void validateRefreshClaims(JWTClaimsSet claimsSet) throws ParseException {
        Instant now = Instant.now();
        Instant allowedPast = now.minusSeconds(clockSkewSeconds);
        Instant allowedFuture = now.plusSeconds(clockSkewSeconds);

        if (claimsSet.getExpirationTime() == null || claimsSet.getExpirationTime().toInstant().isBefore(allowedPast)) {
            throw new SessionRefreshNotAllowedException();
        }
        if (claimsSet.getIssueTime() == null || claimsSet.getIssueTime().toInstant().isAfter(allowedFuture)) {
            throw new SessionRefreshNotAllowedException();
        }

        String tokenType = claimsSet.getStringClaim("typ");
        if (tokenType == null || !"refresh".equalsIgnoreCase(tokenType)) {
            throw new SessionRefreshNotAllowedException();
        }

        String tokenIssuer = claimsSet.getIssuer();
        if (tokenIssuer == null || !issuer.equals(tokenIssuer)) {
            throw new SessionRefreshNotAllowedException();
        }

        List<String> tokenAudience = claimsSet.getAudience();
        if (tokenAudience == null || !tokenAudience.contains(audience)) {
            throw new SessionRefreshNotAllowedException();
        }
    }
}
