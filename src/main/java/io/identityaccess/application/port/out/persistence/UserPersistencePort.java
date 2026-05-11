package io.identityaccess.application.port.out.persistence;

import io.identityaccess.domain.model.user.UserAggregate;
import io.identityaccess.domain.model.user.entity.UserLoginAttempt;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import io.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserPersistencePort {

    Mono<UserAggregate> loadForLogin(EmailAddress email);

    Mono<Void> recordLoginAttempt(UserLoginAttempt attempt);

    Mono<AuthorizationSnapshot> loadAuthorizationSnapshot(UserId userId);

    Mono<Boolean> existsByEmail(EmailAddress email);

    Mono<Boolean> existsById(UserId userId);

    Mono<Boolean> existsByRoleCode(String roleCode);

    Mono<Boolean> wouldBlockLastActiveExclusiveRoleHolder(UserId userId);

    Mono<UserStatusSnapshot> loadStatus(UserId userId);

    Flux<AccountRecord> listAccounts();

    Mono<UserAggregate> create(UserAggregate user, String initialRoleCode, String assignedBy);

    Mono<RoleAssignmentOutcome> assignRole(UserId userId, String roleCode, String assignedBy, Instant now);

    Mono<UserStatusSnapshot> block(UserId userId, Instant now);

    Mono<UserStatusSnapshot> unblock(UserId userId, Instant now);

    record AuthorizationSnapshot(String email, Set<String> roles, Set<String> permissions) {}

    record UserStatusSnapshot(String userId, String email, String status) {}

    record RoleAssignmentOutcome(String userId, String roleCode, boolean assigned) {}

    record AccountRecord(
            String userId,
            String email,
            String status,
            int failedLoginCount,
            Instant createdAt,
            Instant updatedAt,
            Set<String> roles) {}
}
