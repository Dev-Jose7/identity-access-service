package com.arka.identityaccess.application.port.out.persistence;

import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface UserPersistencePort {

    Mono<UserAggregate> loadForLogin(EmailAddress email);

    Mono<AuthorizationSnapshot> loadAuthorizationSnapshot(UserId userId);

    Mono<Boolean> existsByEmail(EmailAddress email);

    Mono<Boolean> existsById(UserId userId);

    Mono<UserStatusSnapshot> loadStatus(UserId userId);

    Mono<UserAggregate> create(UserAggregate user, String initialRoleCode, String assignedBy);

    Mono<RoleAssignmentOutcome> assignRole(UserId userId, String roleCode, String assignedBy, Instant now);

    Mono<UserStatusSnapshot> block(UserId userId, Instant now);

    record AuthorizationSnapshot(String email, Set<String> roles, Set<String> permissions) {}

    record UserStatusSnapshot(String userId, String email, String status) {}

    record RoleAssignmentOutcome(String userId, String roleCode, boolean assigned) {}
}
