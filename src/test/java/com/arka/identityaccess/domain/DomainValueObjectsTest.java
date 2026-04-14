package com.arka.identityaccess.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import org.junit.jupiter.api.Test;

class DomainValueObjectsTest {

    @Test
    void shouldNormalizeEmailToLowercaseAndTrim() {
        EmailAddress email = EmailAddress.of("  USER@ArKa.Com  ");
        assertEquals("user@arka.com", email.value());
        assertEquals("user@arka.com", email.normalized());
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(DomainInvariantViolationException.class, () -> EmailAddress.of("invalid-email"));
    }

    @Test
    void shouldAcceptValidIpv4AndIpv6Literals() {
        assertEquals("10.10.10.10", ClientIp.of("10.10.10.10").value());
        assertEquals("2001:db8::1", ClientIp.of("2001:db8::1").value());
    }

    @Test
    void shouldRejectInvalidIpLiteral() {
        assertThrows(DomainInvariantViolationException.class, () -> ClientIp.of("localhost"));
        assertThrows(DomainInvariantViolationException.class, () -> ClientIp.of("2001:::1"));
        assertThrows(DomainInvariantViolationException.class, () -> ClientIp.of("1:2:3:4:5:6:7:8:9"));
        assertThrows(DomainInvariantViolationException.class, () -> ClientIp.of("2001:db8:zzzz::1"));
    }

    @Test
    void shouldRejectInvalidUserIdFormat() {
        assertThrows(DomainInvariantViolationException.class, () -> AccountId.of("x"));
        assertThrows(DomainInvariantViolationException.class, () -> AccountId.of("user id with spaces"));
    }

    @Test
    void shouldAcceptUuidBasedSessionIdentifiers() {
        assertEquals(
                "11111111-1111-1111-1111-111111111111",
                SessionId.of("11111111-1111-1111-1111-111111111111").value());
        assertEquals(
                "22222222-2222-2222-2222-222222222222",
                AccessJti.of("22222222-2222-2222-2222-222222222222").value());
        assertEquals(
                "33333333-3333-3333-3333-333333333333",
                RefreshJti.of("33333333-3333-3333-3333-333333333333").value());
    }

    @Test
    void shouldRejectInvalidSessionIdentifiers() {
        assertThrows(DomainInvariantViolationException.class, () -> SessionId.of("ses-1"));
        assertThrows(DomainInvariantViolationException.class, () -> AccessJti.of("acc-1"));
        assertThrows(DomainInvariantViolationException.class, () -> RefreshJti.of("ref-1"));
    }
}
