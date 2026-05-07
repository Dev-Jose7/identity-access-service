package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.domain.exception.InvalidCredentialsException;
import com.arka.identityaccess.domain.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.exception.RoleInvalidException;
import com.arka.identityaccess.domain.exception.UserNotFoundException;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.enumtype.UserStatus;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.UserRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveCredentialRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveRoleRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserCredentialRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserLoginAttemptRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserRoleAssignmentRepository;
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
    private final ReactiveUserRoleAssignmentRepository reactiveUserRoleAssignmentRepository;
    private final ReactiveRoleRepository reactiveRoleRepository;
    private final ReactiveUserLoginAttemptRepository reactiveUserLoginAttemptRepository;
    private final UserRowMapper userRowMapper;

    public UserR2dbcRepositoryAdapter(
            ReactiveUserRepository reactiveUserRepository,
            ReactiveUserCredentialRepository reactiveUserCredentialRepository,
            ReactiveCredentialRepository reactiveCredentialRepository,
            ReactiveUserRoleAssignmentRepository reactiveUserRoleAssignmentRepository,
            ReactiveRoleRepository reactiveRoleRepository,
            ReactiveUserLoginAttemptRepository reactiveUserLoginAttemptRepository,
            UserRowMapper userRowMapper) {
        this.reactiveUserRepository = reactiveUserRepository;
        this.reactiveUserCredentialRepository = reactiveUserCredentialRepository;
        this.reactiveCredentialRepository = reactiveCredentialRepository;
        this.reactiveUserRoleAssignmentRepository = reactiveUserRoleAssignmentRepository;
        this.reactiveRoleRepository = reactiveRoleRepository;
        this.reactiveUserLoginAttemptRepository = reactiveUserLoginAttemptRepository;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public Mono<UserAggregate> loadForLogin(EmailAddress email) {
        return reactiveUserRepository.findByEmail(email.normalized())
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .flatMap(this::loadSingleUserForLogin);
    }

    @Override
    public Mono<Boolean> existsByEmail(EmailAddress email) {
        return reactiveUserRepository.existsByEmail(email.normalized());
    }

    @Override
    public Mono<Boolean> existsById(UserId userId) {
        return reactiveUserRepository.existsById(userId.value());
    }

    @Override
    public Mono<AuthorizationSnapshot> loadAuthorizationSnapshot(UserId userId) {
        Mono<String> emailMono = reactiveUserRepository.findById(userId.value())
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .map(UserRow::email)
                .map(email -> email == null ? "" : email.trim());

        Mono<Set<String>> rolesMono = reactiveUserRoleAssignmentRepository
                .findActiveRoleCodesByUserId(userId.value())
                .map(roleCode -> roleCode == null ? "" : roleCode.trim().toUpperCase())
                .filter(roleCode -> !roleCode.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .map(Set::copyOf);

        Mono<Set<String>> permissionsMono = reactiveUserRoleAssignmentRepository
                .findActivePermissionCodesByUserId(userId.value())
                .map(permissionCode -> permissionCode == null ? "" : permissionCode.trim())
                .filter(permissionCode -> !permissionCode.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .map(Set::copyOf);

        return Mono.zip(emailMono, rolesMono, permissionsMono)
                .map(tuple -> new AuthorizationSnapshot(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    @Override
    public Mono<UserStatusSnapshot> loadStatus(UserId userId) {
        return reactiveUserRepository.findById(userId.value())
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .map(row -> new UserStatusSnapshot(
                        row.userId(),
                        row.email() == null ? "" : row.email().trim(),
                        normalizeStatus(row.status())));
    }

    @Override
    @Transactional
    public Mono<UserAggregate> create(UserAggregate user, String initialRoleCode, String assignedBy) {
        UserRow userRow = userRowMapper.toRow(user);
        String credentialId = user.credential().credentialId();
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
                        user.id().value(),
                        "PASSWORD",
                        "PRIMARY",
                        "LOCAL",
                        user.credential().status().name(),
                        null,
                        now,
                        now))
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(rowsUpdated)
                        : Mono.error(new IllegalStateException("Credential insert did not affect exactly one row")))
                .then(reactiveUserCredentialRepository.insertPassword(
                        credentialId,
                        user.credential().passwordHash(),
                        "BCRYPT",
                        now,
                        now,
                        now))
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(rowsUpdated)
                        : Mono.error(new IllegalStateException("Credential password insert did not affect exactly one row")))
                .then(reactiveRoleRepository.findByRoleCode(initialRoleCode)
                        .switchIfEmpty(Mono.error(new RoleInvalidException())))
                .flatMap(roleRow -> reactiveUserRoleAssignmentRepository.insert(
                        UUID.randomUUID().toString(),
                        user.id().value(),
                        roleRow.roleId(),
                        "ACTIVE",
                        assignedBy == null || assignedBy.isBlank() ? "SYSTEM" : assignedBy,
                        now,
                        now,
                        now))
                .flatMap(rowsUpdated -> rowsUpdated == 1
                        ? Mono.just(user)
                        : Mono.error(new IllegalStateException("Role assignment insert did not affect exactly one row")));
    }

    @Override
    @Transactional
    public Mono<RoleAssignmentOutcome> assignRole(UserId userId, String roleCode, String assignedBy, Instant now) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        String normalizedAssignedBy = normalizeAssignedBy(assignedBy);

        return reactiveUserRepository.existsById(userId.value())
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new UserNotFoundException()))
                .then(reactiveUserRoleAssignmentRepository.existsActiveRoleAssignment(userId.value(), normalizedRoleCode))
                .flatMap(alreadyAssigned -> alreadyAssigned
                        ? Mono.just(new RoleAssignmentOutcome(userId.value(), normalizedRoleCode, false))
                        : reactiveRoleRepository.findByRoleCode(normalizedRoleCode)
                                .switchIfEmpty(Mono.error(new RoleInvalidException()))
                                .flatMap(roleRow -> reactiveUserRoleAssignmentRepository.insertIgnoreIfExists(
                                        UUID.randomUUID().toString(),
                                        userId.value(),
                                        roleRow.roleId(),
                                        "ACTIVE",
                                        normalizedAssignedBy,
                                        now,
                                        now,
                                        now))
                                .map(rowsUpdated -> new RoleAssignmentOutcome(
                                        userId.value(),
                                        normalizedRoleCode,
                                        rowsUpdated != null && rowsUpdated > 0)));
    }

    @Override
    @Transactional
    public Mono<UserStatusSnapshot> block(UserId userId, Instant now) {
        return loadStatus(userId)
                .flatMap(statusSnapshot -> {
                    if (UserStatus.BLOCKED.name().equalsIgnoreCase(statusSnapshot.status())) {
                        return Mono.just(new UserStatusSnapshot(
                                statusSnapshot.userId(),
                                statusSnapshot.email(),
                                UserStatus.BLOCKED.name()));
                    }
                    return reactiveUserRepository.updateStatus(userId.value(), UserStatus.BLOCKED.name(), now)
                            .flatMap(rowsUpdated -> {
                                if (rowsUpdated == null || rowsUpdated == 0) {
                                    return Mono.error(new UserNotFoundException());
                                }
                                return Mono.just(new UserStatusSnapshot(
                                        statusSnapshot.userId(),
                                        statusSnapshot.email(),
                                        UserStatus.BLOCKED.name()));
                            });
                });
    }

    private Mono<UserAggregate> loadSingleUserForLogin(UserRow userRow) {
        return Mono.zip(
                        Mono.just(userRow),
                        reactiveUserCredentialRepository
                                .findActiveByUserId(userRow.userId())
                                .switchIfEmpty(Mono.error(new InvalidCredentialsException())),
                        reactiveUserLoginAttemptRepository.findRecentByUserId(userRow.userId()).collectList(),
                        reactiveUserRoleAssignmentRepository.findActiveRoleCodesByUserId(userRow.userId()).collectList())
                .map(tuple -> userRowMapper.toAggregate(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()));
    }

    private String normalizeRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new RoleInvalidException();
        }
        return roleCode.trim().toUpperCase();
    }

    private String normalizeAssignedBy(String assignedBy) {
        if (assignedBy == null || assignedBy.isBlank()) {
            throw new OperationNotPermittedException("Actor context is required");
        }
        return assignedBy.trim();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return UserStatus.DISABLED.name();
        }
        return status.trim().toUpperCase();
    }
}
