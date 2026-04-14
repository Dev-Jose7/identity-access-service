package com.arka.identityaccess.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.JwksHttpController;
import com.arka.identityaccess.infrastructure.adapter.out.security.JwtRsaKeyProvider;
import com.arka.identityaccess.infrastructure.adapter.out.security.JwtSignerAdapter;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(
        controllers = JwksHttpController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
@Import({JwtRsaKeyProvider.class, JwtSignerAdapter.class})
@TestPropertySource(properties = {
        "app.security.jwt.issuer=identity-access-service",
        "app.security.jwt.audience=arka-b2b",
        "app.security.jwt.key-id=dev-rsa-key-1",
        "app.security.jwt.private-key-path=classpath:keys/dev-private.pem",
        "app.security.jwt.public-key-path=classpath:keys/dev-public.pem",
        "app.security.jwt.additional-public-keys=legacy-rsa-key-0=classpath:keys/dev-legacy-public.pem"
})
class JwtJwksInteroperabilityIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtSignerAdapter jwtSignerAdapter;

    @Autowired
    private JwtRsaKeyProvider jwtRsaKeyProvider;

    @Test
    void shouldIssueAccessTokenAndValidateItExternallyWithPublishedJwks() throws Exception {
        String accessToken = jwtSignerAdapter
                .signAccessToken(
                        testSession(),
                        AccessProfile.of(
                                AccountId.of("usr-1"),
                                EmailAddress.of("user@arka.com"),
                                Set.of(RoleCode.of("ORG_OWNER")),
                                Set.of(PermissionCode.of("iam.user.create"), PermissionCode.of("iam.user.read")),
                                Instant.parse("2026-01-01T00:00:00Z")))
                .block();

        assertNotNull(accessToken);
        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        assertEquals(JWSAlgorithm.RS256, signedJWT.getHeader().getAlgorithm());
        assertEquals("dev-rsa-key-1", signedJWT.getHeader().getKeyID());

        JWKSet jwkSet = readPublishedJwks();
        Set<String> publishedKids = jwkSet.getKeys().stream()
                .map(JWK::getKeyID)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(publishedKids.contains("dev-rsa-key-1"));
        assertTrue(publishedKids.contains("legacy-rsa-key-0"));

        assertTrue(verifyExternally(accessToken, jwkSet));

        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        assertEquals("access", claimsSet.getStringClaim("typ"));
        assertEquals("usr-1", claimsSet.getSubject());
        assertEquals("11111111-1111-1111-1111-111111111111", claimsSet.getStringClaim("sid"));
        assertEquals("user@arka.com", claimsSet.getStringClaim("email"));
        assertNotNull(claimsSet.getJWTID());
        assertNotNull(claimsSet.getIssueTime());
        assertNotNull(claimsSet.getExpirationTime());
        assertEquals(Set.of("ORG_OWNER"), Set.copyOf(claimsSet.getStringListClaim("roles")));
        assertEquals(
                Set.of("iam.user.create", "iam.user.read"),
                Set.copyOf(claimsSet.getStringListClaim("permissions")));
    }

    @Test
    void shouldRejectExternalValidationWhenKidIsUnknownOrSignatureIsTampered() throws Exception {
        String validAccessToken = jwtSignerAdapter
                .signAccessToken(
                        testSession(),
                        AccessProfile.of(
                                AccountId.of("usr-1"),
                                EmailAddress.of("user@arka.com"),
                                Set.of(RoleCode.of("ORG_OWNER")),
                                Set.of(PermissionCode.of("iam.user.create")),
                                Instant.parse("2026-01-01T00:00:00Z")))
                .block();

        assertNotNull(validAccessToken);
        JWKSet jwkSet = readPublishedJwks();

        SignedJWT parsedValidToken = SignedJWT.parse(validAccessToken);
        SignedJWT unknownKidToken = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .type(JOSEObjectType.JWT)
                        .keyID("unknown-kid")
                        .build(),
                parsedValidToken.getJWTClaimsSet());
        unknownKidToken.sign(new RSASSASigner(jwtRsaKeyProvider.privateKey()));

        assertFalse(verifyExternally(unknownKidToken.serialize(), jwkSet));
        assertFalse(verifyExternally(tamperPayload(validAccessToken), jwkSet));
    }

    @Test
    void shouldValidateLegacyTokenWhenLegacyKidExistsInPublishedJwks() throws Exception {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("identity-access-service")
                .audience("arka-b2b")
                .subject("usr-legacy")
                .jwtID("jti-legacy-1")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(900)))
                .claim("sid", "ses-legacy-1")
                .claim("typ", "access")
                .claim("email", "legacy.user@arka.com")
                .claim("roles", Set.of("ORG_ADMIN"))
                .claim("permissions", Set.of("iam.user.read"))
                .build();

        SignedJWT legacyToken = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .type(JOSEObjectType.JWT)
                        .keyID("legacy-rsa-key-0")
                        .build(),
                claimsSet);
        JWSSigner legacySigner = new RSASSASigner(loadLegacyPrivateKey());
        legacyToken.sign(legacySigner);

        assertTrue(verifyExternally(legacyToken.serialize(), readPublishedJwks()));
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

    private JWKSet readPublishedJwks() throws Exception {
        String jwksJson = webTestClient.get()
                .uri("/.well-known/jwks.json")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        if (jwksJson == null || jwksJson.isBlank()) {
            throw new IllegalStateException("JWKS response must not be empty");
        }
        return JWKSet.parse(jwksJson);
    }

    private boolean verifyExternally(String token, JWKSet jwkSet) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String kid = signedJWT.getHeader().getKeyID();
            if (kid == null || kid.isBlank()) {
                return false;
            }
            RSAKey rsaJwk = jwkSet.getKeys().stream()
                    .filter(jwk -> kid.equals(jwk.getKeyID()))
                    .findFirst()
                    .map(jwk -> (RSAKey) jwk)
                    .orElse(null);
            if (rsaJwk == null) {
                return false;
            }
            return signedJWT.verify(new RSASSAVerifier(rsaJwk.toRSAPublicKey()));
        } catch (Exception exception) {
            return false;
        }
    }

    private String tamperPayload(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }
        String payload = parts[1];
        if (payload.isEmpty()) {
            throw new IllegalArgumentException("Invalid JWT payload");
        }
        char last = payload.charAt(payload.length() - 1);
        char replacement = last == 'A' ? 'B' : 'A';
        parts[1] = payload.substring(0, payload.length() - 1) + replacement;
        return String.join(".", parts);
    }

    private RSAPrivateKey loadLegacyPrivateKey() {
        try {
            ClassPathResource resource = new ClassPathResource("keys/dev-legacy-private.pem");
            String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String base64 = pem
                    .replaceAll("-----BEGIN [A-Z ]+-----", "")
                    .replaceAll("-----END [A-Z ]+-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load legacy private key for interoperability test", exception);
        }
    }
}
