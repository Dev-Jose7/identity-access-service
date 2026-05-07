package com.arka.identityaccess.domain.model.user;

import com.arka.identityaccess.domain.exception.UserNotEnabledException;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.role.RoleCode;
import com.arka.identityaccess.domain.model.user.entity.UserCredential;
import com.arka.identityaccess.domain.model.user.entity.UserLoginAttempt;
import com.arka.identityaccess.domain.model.user.enumtype.UserStatus;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.domain.service.PasswordPolicy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserAggregate {

    private final UserId id;
    private final EmailAddress email;
    private final UserStatus status;
    private final UserCredential credential;
    private final List<UserLoginAttempt> loginAttempts;
    private final Set<String> roles;

    public UserAggregate(
            UserId id,
            EmailAddress email,
            UserStatus status,
            UserCredential credential,
            List<UserLoginAttempt> loginAttempts,
            Set<String> roles) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.credential = credential;
        this.loginAttempts = new ArrayList<>(loginAttempts == null ? List.of() : loginAttempts);
        this.roles = normalizeRoles(roles);
    }

    public static UserAggregate register(EmailAddress email, String passwordHash, Set<String> roles) {
        UserId userId = UserId.newId();
        return new UserAggregate(
                userId,
                email,
                UserStatus.ACTIVE,
                UserCredential.primaryPassword(userId, email, passwordHash),
                List.of(),
                roles);
    }

    public UserId id() { return id; }
    public EmailAddress email() { return email; }
    public UserStatus status() { return status; }
    public UserCredential credential() { return credential; }
    public List<UserLoginAttempt> loginAttempts() { return Collections.unmodifiableList(loginAttempts); }
    public Set<String> roles() { return Collections.unmodifiableSet(roles); }

    public UserLoginAttempt lastAttempt() {
        if (loginAttempts.isEmpty()) {
            return null;
        }
        return loginAttempts.get(loginAttempts.size() - 1);
    }

    public void authenticate(boolean passwordMatches, PasswordPolicy passwordPolicy, ClientIp clientIp, Instant now) {
        if (!status.canLogin()) {
            throw new UserNotEnabledException();
        }
        passwordPolicy.ensureCredentialUsable(credential);
        if (!passwordMatches) {
            loginAttempts.add(UserLoginAttempt.failed(id, clientIp, now));
        }
        passwordPolicy.ensurePasswordMatches(passwordMatches);
        loginAttempts.add(UserLoginAttempt.success(id, clientIp, now));
    }

    public boolean isLoginAllowed() {
        return status.canLogin() && credential != null && credential.isActive();
    }

    public boolean hasRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return false;
        }
        return roles.contains(roleCode.trim().toUpperCase());
    }

    private Set<String> normalizeRoles(Set<String> rawRoles) {
        if (rawRoles == null || rawRoles.isEmpty()) {
            return Set.of(RoleCode.ORG_USER.name());
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String rawRole : rawRoles) {
            if (rawRole == null || rawRole.isBlank()) {
                continue;
            }
            String roleCode = rawRole.trim().toUpperCase();
            if (RoleCode.codes().contains(roleCode)) {
                normalized.add(roleCode);
            }
        }
        if (normalized.isEmpty()) {
            return Set.of(RoleCode.ORG_USER.name());
        }
        return Collections.unmodifiableSet(normalized);
    }
}
