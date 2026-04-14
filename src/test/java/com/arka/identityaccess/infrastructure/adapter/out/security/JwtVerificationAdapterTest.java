package com.arka.identityaccess.infrastructure.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.identityaccess.application.port.out.security.JwtVerificationPort;
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
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class JwtVerificationAdapterTest {

    @Test
    void shouldVerifySignedAccessToken() {
        JwtRsaKeyProvider keyProvider = new JwtRsaKeyProvider(
                "RS256",
                "dev-rsa-key-1",
                "classpath:keys/dev-private.pem",
                "classpath:keys/dev-public.pem",
                "",
                new DefaultResourceLoader());
        JwtSignerAdapter signer = new JwtSignerAdapter(keyProvider, "identity-access-service", "arka-b2b");
        JwtVerificationAdapter verifier = new JwtVerificationAdapter(
                keyProvider,
                "identity-access-service",
                "arka-b2b",
                60);

        SessionAggregate session = testSession();
        String token = signer
                .signAccessToken(
                        session,
                        AccessProfile.of(
                                AccountId.of("usr-1"),
                                EmailAddress.of("user@arka.com"),
                                Set.of(RoleCode.of("ORG_OWNER")),
                                Set.of(PermissionCode.of("iam.user.create")),
                                Instant.parse("2026-01-01T00:00:00Z")))
                .block();

        JwtVerificationPort.VerificationResult result = verifier.verify(token).block();

        assertTrue(result.valid());
        assertEquals("usr-1", result.subject());
        assertEquals("11111111-1111-1111-1111-111111111111", result.sessionId());
        assertEquals("access", result.tokenType());
        assertEquals("22222222-2222-2222-2222-222222222222", result.jti());
        assertEquals(Set.of("ORG_OWNER"), result.roles());
        assertEquals(Set.of("iam.user.create"), result.permissions());
    }

    @Test
    void shouldReturnInvalidWhenTokenIsMissing() {
        JwtRsaKeyProvider keyProvider = new JwtRsaKeyProvider(
                "RS256",
                "dev-rsa-key-1",
                "classpath:keys/dev-private.pem",
                "classpath:keys/dev-public.pem",
                "",
                new DefaultResourceLoader());
        JwtVerificationAdapter verifier = new JwtVerificationAdapter(
                keyProvider,
                "identity-access-service",
                "arka-b2b",
                60);

        JwtVerificationPort.VerificationResult result = verifier.verify("   ").block();

        assertEquals(false, result.valid());
        assertEquals("token_missing", result.invalidReason());
    }

    private SessionAggregate testSession() {
        Instant now = Instant.now();
        return SessionAggregate.rehydrate(
                SessionId.of("11111111-1111-1111-1111-111111111111"),
                AccountId.of("usr-1"),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("127.0.0.1"),
                AccessJti.of("22222222-2222-2222-2222-222222222222"),
                RefreshJti.of("33333333-3333-3333-3333-333333333333"),
                SessionTimestamps.of(now, now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.ACTIVE);
    }
}
