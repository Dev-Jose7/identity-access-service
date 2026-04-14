package com.arka.identityaccess.domain.identity.aggregate;

import com.arka.identityaccess.domain.identity.entity.AccountCredential;
import com.arka.identityaccess.domain.identity.entity.AuthenticationAttempt;
import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import com.arka.identityaccess.domain.identity.event.AccountActivatedEvent;
import com.arka.identityaccess.domain.identity.event.AccountAuthenticationFailedEvent;
import com.arka.identityaccess.domain.identity.event.AccountAuthenticationSucceededEvent;
import com.arka.identityaccess.domain.identity.event.AccountBlockedEvent;
import com.arka.identityaccess.domain.identity.event.AccountDisabledEvent;
import com.arka.identityaccess.domain.identity.event.AccountEmailVerifiedEvent;
import com.arka.identityaccess.domain.identity.event.AccountPasswordChangedEvent;
import com.arka.identityaccess.domain.identity.event.AccountReenabledEvent;
import com.arka.identityaccess.domain.identity.event.AccountRegisteredEvent;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.AuthenticationResult;
import com.arka.identityaccess.domain.identity.valueobject.AuthenticationSecurityRules;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountAggregate {

    private final AccountId id;
    private final EmailAddress email;
    private boolean emailVerified;
    private AccountStatus status;
    private AccountCredential credential;
    private int failedAuthenticationCount;
    private final List<DomainEvent> domainEvents;

    private AccountAggregate(
            AccountId id,
            EmailAddress email,
            boolean emailVerified,
            AccountStatus status,
            AccountCredential credential,
            int failedAuthenticationCount,
            List<DomainEvent> domainEvents) {
        if (id == null || email == null || status == null || credential == null) {
            throw new DomainInvariantViolationException("account aggregate is incomplete");
        }
        if (failedAuthenticationCount < 0) {
            throw new DomainInvariantViolationException("failed authentication count cannot be negative");
        }
        this.id = id;
        this.email = email;
        this.emailVerified = emailVerified;
        this.status = status;
        this.credential = credential;
        this.failedAuthenticationCount = failedAuthenticationCount;
        this.domainEvents = new ArrayList<>(domainEvents == null ? List.of() : domainEvents);
    }

    public static AccountAggregate register(
            EmailAddress email,
            String passwordHash,
            boolean requireEmailVerification,
            Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("registration occurredAt is required");
        }
        AccountId accountId = AccountId.newId();
        AccountStatus initialStatus = requireEmailVerification ? AccountStatus.PENDING_VERIFICATION : AccountStatus.ACTIVE;
        boolean emailVerified = !requireEmailVerification;

        List<DomainEvent> events = new ArrayList<>();
        events.add(AccountRegisteredEvent.create(accountId, email, occurredAt));
        if (initialStatus == AccountStatus.ACTIVE) {
            events.add(AccountActivatedEvent.create(accountId, occurredAt));
        }

        return new AccountAggregate(
                accountId,
                email,
                emailVerified,
                initialStatus,
                AccountCredential.primaryPassword(accountId, email, passwordHash),
                0,
                events);
    }

    public static AccountAggregate rehydrate(
            AccountId id,
            EmailAddress email,
            boolean emailVerified,
            AccountStatus status,
            AccountCredential credential,
            int failedAuthenticationCount) {
        return new AccountAggregate(id, email, emailVerified, status, credential, failedAuthenticationCount, List.of());
    }

    public AuthenticationResult authenticate(
            boolean passwordMatches,
            AuthenticationSecurityRules securityRules,
            Instant occurredAt,
            String clientIp) {
        if (occurredAt == null || clientIp == null || clientIp.isBlank()) {
            throw new DomainInvariantViolationException("authentication requires occurredAt and clientIp");
        }
        if (securityRules == null) {
            throw new DomainInvariantViolationException("authentication security rules are required");
        }

        if (!status.canAuthenticate()) {
            return recordAuthenticationFailure("account_not_enabled", occurredAt, clientIp, false, securityRules);
        }
        if (!emailVerified) {
            return recordAuthenticationFailure("email_not_verified", occurredAt, clientIp, false, securityRules);
        }
        if (!credential.isUsable()) {
            return recordAuthenticationFailure("credential_not_usable", occurredAt, clientIp, false, securityRules);
        }
        if (!passwordMatches) {
            return recordAuthenticationFailure("invalid_credentials", occurredAt, clientIp, true, securityRules);
        }

        AuthenticationAttempt attempt = AuthenticationAttempt.success(id, email, occurredAt, clientIp);
        if (securityRules.resetFailuresOnSuccess()) {
            failedAuthenticationCount = 0;
        }
        recordDomainEvent(AccountAuthenticationSucceededEvent.create(id, email, attempt.attemptId(), occurredAt));
        return AuthenticationResult.success(attempt);
    }

    public boolean markEmailVerified(Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("email verification requires occurredAt");
        }
        if (emailVerified) {
            return false;
        }
        emailVerified = true;
        recordDomainEvent(AccountEmailVerifiedEvent.create(id, occurredAt));
        return true;
    }

    public boolean activate(Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("activation requires occurredAt");
        }
        if (!emailVerified) {
            throw new DomainInvariantViolationException("cannot activate account without verified email");
        }
        if (status == AccountStatus.ACTIVE) {
            return false;
        }
        status = AccountStatus.ACTIVE;
        recordDomainEvent(AccountActivatedEvent.create(id, occurredAt));
        return true;
    }

    public boolean block(String reason, Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("account block requires occurredAt");
        }
        if (status == AccountStatus.BLOCKED) {
            return false;
        }
        status = AccountStatus.BLOCKED;
        recordDomainEvent(AccountBlockedEvent.create(id, reason, occurredAt));
        return true;
    }

    public boolean disable(String reason, Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("account disable requires occurredAt");
        }
        if (status == AccountStatus.DISABLED) {
            return false;
        }
        status = AccountStatus.DISABLED;
        recordDomainEvent(AccountDisabledEvent.create(id, reason, occurredAt));
        return true;
    }

    public boolean reenable(Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("account re-enable requires occurredAt");
        }
        if (status == AccountStatus.ACTIVE) {
            return false;
        }
        status = emailVerified ? AccountStatus.ACTIVE : AccountStatus.PENDING_VERIFICATION;
        recordDomainEvent(AccountReenabledEvent.create(id, occurredAt));
        if (status == AccountStatus.ACTIVE) {
            recordDomainEvent(AccountActivatedEvent.create(id, occurredAt));
        }
        return true;
    }

    public void changePassword(String nextPasswordHash, Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("password change requires occurredAt");
        }
        credential = credential.changePassword(nextPasswordHash);
        recordDomainEvent(AccountPasswordChangedEvent.create(id, occurredAt));
    }

    private AuthenticationResult recordAuthenticationFailure(
            String failureCode,
            Instant occurredAt,
            String clientIp,
            boolean candidateForLockout,
            AuthenticationSecurityRules securityRules) {
        AuthenticationAttempt failedAttempt = AuthenticationAttempt.failure(id, email, occurredAt, clientIp, failureCode);
        if (candidateForLockout && securityRules.shouldCountAgainstLockout(failureCode)) {
            failedAuthenticationCount++;
            if (securityRules.shouldBlockAfterFailedAttemptCount(failedAuthenticationCount, status)) {
                status = AccountStatus.BLOCKED;
                recordDomainEvent(AccountBlockedEvent.create(id, securityRules.lockoutReason(), occurredAt));
            }
        }
        recordDomainEvent(AccountAuthenticationFailedEvent.create(id, email, failedAttempt.attemptId(), failureCode, occurredAt));
        return AuthenticationResult.failure(failedAttempt, failureCode);
    }

    private void recordDomainEvent(DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    public AccountId id() {
        return id;
    }

    public EmailAddress email() {
        return email;
    }

    public boolean emailVerified() {
        return emailVerified;
    }

    public AccountStatus status() {
        return status;
    }

    public AccountCredential credential() {
        return credential;
    }

    public int failedAuthenticationCount() {
        return failedAuthenticationCount;
    }

    public boolean isAuthenticationEligible() {
        return status.canAuthenticate() && emailVerified && credential.isUsable();
    }

    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
