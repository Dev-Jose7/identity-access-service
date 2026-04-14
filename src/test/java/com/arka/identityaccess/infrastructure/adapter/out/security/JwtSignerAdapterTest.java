package com.arka.identityaccess.infrastructure.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
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
                AccessProfile.of(
                        AccountId.of("usr-1"),
                        EmailAddress.of("user@arka.com"),
                        Set.of(RoleCode.of("ORG_OWNER")),
                        Set.of(PermissionCode.of("iam.user.create"), PermissionCode.of("iam.user.read")),
                        Instant.parse("2026-01-01T00:00:00Z")))
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
                SessionId.of("11111111-1111-1111-1111-111111111111"),
                AccountId.of("usr-1"),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("22222222-2222-2222-2222-222222222222"),
                RefreshJti.of("33333333-3333-3333-3333-333333333333"),
                SessionTimestamps.of(now, now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.ACTIVE);
    }
}
