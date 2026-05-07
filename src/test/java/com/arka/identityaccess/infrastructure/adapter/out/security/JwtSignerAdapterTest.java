package com.arka.identityaccess.infrastructure.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.model.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.model.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.domain.model.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class JwtSignerAdapterTest {

    @Test
    void shouldSignAccessTokenUsingRs256WithKidAndAuthorizationClaims() throws Exception {
        JwtRsaKeyProvider keyProvider = new JwtRsaKeyProvider(
                "RS256",
                "dev-rsa-key-1",
                "classpath:keys/dev-private.pem",
                "classpath:keys/dev-public.pem",
                "",
                new DefaultResourceLoader());
        JwtSignerAdapter signerAdapter = new JwtSignerAdapter(
                keyProvider,
                "identity-access-service",
                "arka-b2b");

        String token = signerAdapter.signAccessToken(
                testSession(),
                "user@arka.com",
                Set.of("ORG_OWNER"),
                Set.of("iam.user.create", "iam.user.read"))
                .block();

        assertNotNull(token);
        SignedJWT signedJWT = SignedJWT.parse(token);
        assertEquals(JWSAlgorithm.RS256, signedJWT.getHeader().getAlgorithm());
        assertEquals("dev-rsa-key-1", signedJWT.getHeader().getKeyID());
        assertTrue(signedJWT.verify(new RSASSAVerifier(keyProvider.publicKey())));
        assertEquals("access", signedJWT.getJWTClaimsSet().getStringClaim("typ"));
        assertEquals("user@arka.com", signedJWT.getJWTClaimsSet().getStringClaim("email"));
        assertEquals(Set.of("ORG_OWNER"), Set.copyOf(signedJWT.getJWTClaimsSet().getStringListClaim("roles")));
        assertEquals(
                Set.of("iam.user.create", "iam.user.read"),
                Set.copyOf(signedJWT.getJWTClaimsSet().getStringListClaim("permissions")));
    }

    @Test
    void shouldSignRefreshTokenWithoutAuthorizationClaims() throws Exception {
        JwtRsaKeyProvider keyProvider = new JwtRsaKeyProvider(
                "RS256",
                "dev-rsa-key-1",
                "classpath:keys/dev-private.pem",
                "classpath:keys/dev-public.pem",
                "",
                new DefaultResourceLoader());
        JwtSignerAdapter signerAdapter = new JwtSignerAdapter(
                keyProvider,
                "identity-access-service",
                "arka-b2b");

        String token = signerAdapter.signRefreshToken(testSession()).block();

        assertNotNull(token);
        SignedJWT signedJWT = SignedJWT.parse(token);
        assertTrue(signedJWT.verify(new RSASSAVerifier(keyProvider.publicKey())));
        assertEquals("refresh", signedJWT.getJWTClaimsSet().getStringClaim("typ"));
        assertNull(signedJWT.getJWTClaimsSet().getClaim("roles"));
        assertNull(signedJWT.getJWTClaimsSet().getClaim("permissions"));
    }

    private SessionAggregate testSession() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return SessionAggregate.rehydrate(
                SessionId.of("ses-1"),
                UserId.of("usr-1"),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("acc-1"),
                RefreshJti.of("ref-1"),
                SessionTimestamps.of(now, now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.ACTIVE,
                null);
    }
}
