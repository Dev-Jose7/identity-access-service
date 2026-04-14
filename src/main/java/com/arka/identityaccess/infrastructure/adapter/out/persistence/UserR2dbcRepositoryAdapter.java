package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.exception.RoleInvalidException;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.entity.AuthenticationAttempt;
import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import com.arka.identityaccess.application.exception.AccountNotFoundException;
import com.arka.identityaccess.domain.identity.exception.InvalidCredentialsException;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.UserRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveCredentialRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveRoleRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserCredentialRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserLoginAttemptRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserRoleRepository;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Component
public class UserR2dbcRepositoryAdapter implements UserPersistencePort {

    private final ReactiveUserRepository reactiveUserRepository;
    private final ReactiveUserCredentialRepository reactiveUserCredentialRepository;
    private final ReactiveCredentialRepository reactiveCredentialRepository;
    private final ReactiveUserRoleRepository reactiveUserRoleRepository;
    private final ReactiveRoleRepository reactiveRoleRepository;
    private final ReactiveUserLoginAttemptRepository reactiveUserLoginAttemptRepository;
    private final UserRowMapper userRowMapper;

    public UserR2dbcRepositoryAdapter(
            ReactiveUserRepository reactiveUserRepository,
            ReactiveUserCredentialRepository reactiveUserCredentialRepository,
            ReactiveCredentialRepository reactiveCredentialRepository,
            ReactiveUserRoleRepository reactiveUserRoleRepository,
            ReactiveRoleRepository reactiveRoleRepository,
            ReactiveUserLoginAttemptRepository reactiveUserLoginAttemptRepository,
            UserRowMapper userRowMapper) {
        this.reactiveUserRepository = reactiveUserRepository;
        this.reactiveUserCredentialRepository = reactiveUserCredentialRepository;
        this.reactiveCredentialRepository = reactiveCredentialRepository;
        this.reactiveUserRoleRepository = reactiveUserRoleRepository;
        this.reactiveRoleRepository = reactiveRoleRepository;
        this.reactiveUserLoginAttemptRepository = reactiveUserLoginAttemptRepository;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public Mono<AccountAggregate> loadForLogin(EmailAddress email) {
        return reactiveUserRepository.findByEmail(email.normalized())
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .flatMap(userRow -> loadSingleAccountForLogin(userRow, new InvalidCredentialsException()));
    }

    @Override
    public Mono<AccountAggregate> loadById(AccountId accountId) {
        return reactiveUserRepository.findById(accountId.value())
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(this::loadSingleAccountById);
    }

    @Override
    public Mono<Boolean> existsByEmail(EmailAddress email) {
        return reactiveUserRepository.existsByEmail(email.normalized());
    }

    @Override
    public Mono<Boolean> existsById(AccountId accountId) {
        return reactiveUserRepository.existsById(accountId.value());
    }

    @Override
    public Mono<AccessProfile> loadAuthorizationSnapshot(AccountId accountId) {
        Mono<String> emailMono = reactiveUserRepository.findById(accountId.value())
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .map(UserRow::email)
                .map(email -> email == null ? "" : email.trim());

        Mono<Set<RoleCode>> rolesMono = reactiveUserRoleRepository
                .findActiveRoleCodesByUserId(accountId.value())
                .map(roleCode -> roleCode == null ? "" : roleCode.trim().toUpperCase())
                .filter(roleCode -> !roleCode.isBlank())
                .map(RoleCode::of)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .map(Set::copyOf);

        Mono<Set<PermissionCode>> permissionsMono = reactiveUserRoleRepository
                .findActivePermissionCodesByUserId(accountId.value())
                .map(permissionCode -> permissionCode == null ? "" : permissionCode.trim())
                .filter(permissionCode -> !permissionCode.isBlank())
                .map(PermissionCode::of)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .map(Set::copyOf);

        return Mono.zip(emailMono, rolesMono, permissionsMono)
                .map(tuple -> AccessProfile.of(
                        accountId,
                        EmailAddress.of(tuple.getT1()),
                        tuple.getT2(),
                        tuple.getT3(),
                        Instant.now()));
    }

    @Override
    public Mono<UserStatusSnapshot> loadStatus(AccountId accountId) {
        return reactiveUserRepository.findById(accountId.value())
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .map(row -> new UserStatusSnapshot(
                        row.userId(),
                        row.email() == null ? "" : row.email().trim(),
                        normalizeStatus(row.status())));
    }

    @Override
    public Mono<Set<ActiveAccessAssignment>> loadActiveAccessAssignments(AccountId accountId) {
        return reactiveUserRepository.existsById(accountId.value())
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new AccountNotFoundException()))
                .then(reactiveUserRoleRepository.findActiveAssignmentsByUserId(accountId.value())
                        .map(row -> new ActiveAccessAssignment(
                                row.assignmentId(),
                                accountId,
                                RoleId.of(row.roleId()),
                                row.assignedBy(),
                                row.assignedAt()))
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)))
                .map(Set::copyOf);
    }

    @Override
    @Transactional
    public Mono<AccountAggregate> create(AccountAggregate account, RoleCode initialRoleCode, String assignedBy) {
        UserRow userRow = userRowMapper.toRow(account);
        String credentialId = account.credential().credentialId();
        Instant now = Instant.now();

        return reactiveUserRepository
                .insert(
                        userRow.userId(),
                        userRow.email(),
                        userRow.status(),
                        userRow.failedLoginCount(),
                        now,
                        now)
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(rowsUpdated)
                        : Mono.error(new IllegalStateException("User insert did not affect exactly one row")))
                .then(reactiveCredentialRepository.insert(
                        credentialId,
                        account.id().value(),
                        "PASSWORD",
                        "PRIMARY",
                        "LOCAL",
                        account.credential().status().name(),
                        null,
                        now,
                        now))
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(rowsUpdated)
                        : Mono.error(new IllegalStateException("Credential insert did not affect exactly one row")))
                .then(reactiveUserCredentialRepository.insertPassword(
                        credentialId,
                        account.credential().passwordHash(),
                        "BCRYPT",
                        now,
                        now,
                        now))
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(rowsUpdated)
                        : Mono.error(new IllegalStateException("Credential password insert did not affect exactly one row")))
                .then(reactiveRoleRepository.findByRoleCode(normalizeRoleCode(initialRoleCode))
                        .switchIfEmpty(Mono.error(new RoleInvalidException())))
                .flatMap(roleRow -> reactiveUserRoleRepository.insert(
                        UUID.randomUUID().toString(),
                        account.id().value(),
                        roleRow.roleId(),
                        "ACTIVE",
                        assignedBy == null || assignedBy.isBlank() ? "SYSTEM" : assignedBy,
                        now,
                        now,
                        now))
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(account)
                        : Mono.error(new IllegalStateException("Role assignment insert did not affect exactly one row")));
    }

    @Override
    @Transactional
    public Mono<Boolean> canAssignRole(AccountId actorAccountId, RoleCode targetRoleCode) {
        return reactiveUserRoleRepository.canAssignRoleByPolicy(actorAccountId.value(), normalizeRoleCode(targetRoleCode));
    }

    @Override
    public Mono<RoleId> resolveRoleIdByCode(RoleCode roleCode) {
        return reactiveRoleRepository.findByRoleCode(normalizeRoleCode(roleCode))
                .switchIfEmpty(Mono.error(new RoleInvalidException()))
                .map(role -> RoleId.of(role.roleId()));
    }

    @Override
    @Transactional
    public Mono<Boolean> assignRole(AccountId accountId, RoleId roleId, String assignedBy, Instant now) {
        String normalizedAssignedBy = normalizeAssignedBy(assignedBy);

        return reactiveUserRepository.existsById(accountId.value())
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new AccountNotFoundException()))
                .then(reactiveUserRoleRepository.existsActiveRoleAssignment(accountId.value(), roleId.value()))
                .flatMap(alreadyAssigned -> {
                    if (alreadyAssigned) {
                        return Mono.just(false);
                    }
                    return reactiveUserRoleRepository.insertIgnoreIfExists(
                                    UUID.randomUUID().toString(),
                                    accountId.value(),
                                    roleId.value(),
                                    "ACTIVE",
                                    normalizedAssignedBy,
                                    now,
                                    now,
                                    now)
                            .map(rowsUpdated -> rowsUpdated != null && rowsUpdated > 0);
                });
    }

    @Override
    @Transactional
    public Mono<UserStatusSnapshot> block(AccountId accountId, Instant now) {
        return loadStatus(accountId)
                .flatMap(statusSnapshot -> {
                    if (AccountStatus.BLOCKED.name().equalsIgnoreCase(statusSnapshot.status())) {
                        return Mono.just(new UserStatusSnapshot(
                                statusSnapshot.userId(),
                                statusSnapshot.email(),
                                AccountStatus.BLOCKED.name()));
                    }
                    return reactiveUserRepository.updateStatus(accountId.value(), AccountStatus.BLOCKED.name(), now)
                            .flatMap(rowsUpdated -> {
                                if (rowsUpdated == null || rowsUpdated == 0) {
                                    return Mono.error(new AccountNotFoundException());
                                }
                                return Mono.just(new UserStatusSnapshot(
                                        statusSnapshot.userId(),
                                        statusSnapshot.email(),
                                        AccountStatus.BLOCKED.name()));
                            });
                });
    }

    @Override
    @Transactional
    public Mono<Void> recordLoginAttempt(AccountId accountId, AuthenticationAttempt attempt) {
        if (accountId == null || attempt == null) {
            return Mono.error(new IllegalArgumentException("accountId and attempt are required"));
        }
        if (attempt.accountId() != null && !accountId.value().equals(attempt.accountId().value())) {
            return Mono.error(new IllegalArgumentException("Login attempt accountId does not match aggregate accountId"));
        }

        Instant occurredAt = attempt.occurredAt();
        return reactiveUserLoginAttemptRepository
                .insert(
                        attempt.attemptId(),
                        accountId.value(),
                        attempt.clientIp(),
                        attempt.success(),
                        occurredAt,
                        occurredAt)
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(rowsUpdated)
                        : Mono.error(new IllegalStateException("User login attempt insert did not affect exactly one row")))
                .then(reactiveUserRepository.updateFailedLoginCount(accountId.value(), attempt.success(), occurredAt))
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.<Void>empty()
                        : Mono.error(new AccountNotFoundException()));
    }

    private Mono<AccountAggregate> loadSingleAccountForLogin(UserRow userRow, RuntimeException missingCredentialException) {
        return reactiveUserCredentialRepository
                .findActiveByUserId(userRow.userId())
                .switchIfEmpty(Mono.error(missingCredentialException))
                .map(credentialRow -> userRowMapper.toAggregate(userRow, credentialRow));
    }

    private Mono<AccountAggregate> loadSingleAccountById(UserRow userRow) {
        return reactiveUserCredentialRepository
                .findPrimaryByUserId(userRow.userId())
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .map(credentialRow -> userRowMapper.toAggregate(userRow, credentialRow));
    }

    private String normalizeRoleCode(RoleCode roleCode) {
        if (roleCode == null) {
            throw new RoleInvalidException();
        }
        return roleCode.value();
    }

    private String normalizeAssignedBy(String assignedBy) {
        if (assignedBy == null || assignedBy.isBlank()) {
            throw new OperationNotPermittedException("Actor context is required");
        }
        return assignedBy.trim();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return AccountStatus.DISABLED.name();
        }
        return status.trim().toUpperCase();
    }
}
