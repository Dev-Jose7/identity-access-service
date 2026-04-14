package com.arka.identityaccess.application.port.out.persistence;

import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.entity.AuthenticationAttempt;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import java.time.Instant;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface UserPersistencePort {

    Mono<AccountAggregate> loadForLogin(EmailAddress email);

    Mono<AccountAggregate> loadById(AccountId accountId);

    Mono<AccessProfile> loadAuthorizationSnapshot(AccountId accountId);

    Mono<Boolean> existsByEmail(EmailAddress email);

    Mono<Boolean> existsById(AccountId accountId);

    Mono<UserStatusSnapshot> loadStatus(AccountId accountId);

    Mono<Set<ActiveAccessAssignment>> loadActiveAccessAssignments(AccountId accountId);

    Mono<AccountAggregate> create(AccountAggregate account, RoleCode initialRoleCode, String assignedBy);

    Mono<Boolean> canAssignRole(AccountId actorAccountId, RoleCode targetRoleCode);

    Mono<RoleId> resolveRoleIdByCode(RoleCode roleCode);

    Mono<Boolean> assignRole(AccountId accountId, RoleId roleId, String assignedBy, Instant now);

    Mono<UserStatusSnapshot> block(AccountId accountId, Instant now);

    Mono<Void> recordLoginAttempt(AccountId accountId, AuthenticationAttempt attempt);

    record UserStatusSnapshot(String userId, String email, String status) {}

    record ActiveAccessAssignment(String assignmentId, AccountId accountId, RoleId roleId, String assignedBy, Instant assignedAt) {}
}
