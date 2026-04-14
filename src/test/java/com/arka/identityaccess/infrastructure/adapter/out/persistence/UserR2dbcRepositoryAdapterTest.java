package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.exception.AccountNotFoundException;
import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.identity.entity.AuthenticationAttempt;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.UserRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveCredentialRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveRoleRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserCredentialRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserLoginAttemptRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserRepository;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserRoleRepository;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class UserR2dbcRepositoryAdapterTest {

    @Mock
    private ReactiveUserRepository reactiveUserRepository;

    @Mock
    private ReactiveUserCredentialRepository reactiveUserCredentialRepository;

    @Mock
    private ReactiveCredentialRepository reactiveCredentialRepository;

    @Mock
    private ReactiveUserRoleRepository reactiveUserRoleRepository;

    @Mock
    private ReactiveRoleRepository reactiveRoleRepository;

    @Mock
    private ReactiveUserLoginAttemptRepository reactiveUserLoginAttemptRepository;

    @Mock
    private UserRowMapper userRowMapper;

    @Test
    void shouldLoadAuthorizationSnapshotFromDatabaseWithNormalizedValues() {
        UserR2dbcRepositoryAdapter adapter = new UserR2dbcRepositoryAdapter(
                reactiveUserRepository,
                reactiveUserCredentialRepository,
                reactiveCredentialRepository,
                reactiveUserRoleRepository,
                reactiveRoleRepository,
                reactiveUserLoginAttemptRepository,
                userRowMapper);

        when(reactiveUserRepository.findById("usr-1"))
                .thenReturn(Mono.just(new UserRow(
                        "usr-1",
                        "user@arka.com",
                        "ACTIVE",
                        0,
                        Instant.now(),
                        Instant.now())));
        when(reactiveUserRoleRepository.findActiveRoleCodesByUserId("usr-1"))
                .thenReturn(Flux.just(" org_owner ", "ORG_OWNER", "ORG_ADMIN", " "));
        when(reactiveUserRoleRepository.findActivePermissionCodesByUserId("usr-1"))
                .thenReturn(Flux.just(" iam.user.read ", "iam.user.read", "iam.user.create", " "));

        AccessProfile snapshot = adapter.loadAuthorizationSnapshot(AccountId.of("usr-1")).block();

        assertEquals("user@arka.com", snapshot.email().value());
        assertEquals(Set.of("ORG_OWNER", "ORG_ADMIN"), snapshot.roleCodeValues());
        assertEquals(Set.of("iam.user.read", "iam.user.create"), snapshot.permissionCodeValues());
    }

    @Test
    void shouldFailWhenLoadingAuthorizationSnapshotForMissingUser() {
        UserR2dbcRepositoryAdapter adapter = new UserR2dbcRepositoryAdapter(
                reactiveUserRepository,
                reactiveUserCredentialRepository,
                reactiveCredentialRepository,
                reactiveUserRoleRepository,
                reactiveRoleRepository,
                reactiveUserLoginAttemptRepository,
                userRowMapper);

        when(reactiveUserRepository.findById("missing")).thenReturn(Mono.empty());
        when(reactiveUserRoleRepository.findActiveRoleCodesByUserId("missing")).thenReturn(Flux.empty());
        when(reactiveUserRoleRepository.findActivePermissionCodesByUserId("missing")).thenReturn(Flux.empty());

        assertThrows(AccountNotFoundException.class, () -> adapter.loadAuthorizationSnapshot(AccountId.of("missing")).block());
    }

    @Test
    void shouldRecordLoginAttemptAndUpdateFailedLoginCount() {
        UserR2dbcRepositoryAdapter adapter = new UserR2dbcRepositoryAdapter(
                reactiveUserRepository,
                reactiveUserCredentialRepository,
                reactiveCredentialRepository,
                reactiveUserRoleRepository,
                reactiveRoleRepository,
                reactiveUserLoginAttemptRepository,
                userRowMapper);

        AccountId userId = AccountId.of("usr-1");
        AuthenticationAttempt attempt = AuthenticationAttempt.failure(
                userId,
                EmailAddress.of("user@arka.com"),
                Instant.parse("2026-01-01T00:00:00Z"),
                "10.0.0.1",
                "invalid_credentials");

        when(reactiveUserLoginAttemptRepository.insert(
                        anyString(), anyString(), anyString(), anyBoolean(), any(Instant.class), any(Instant.class)))
                .thenReturn(Mono.just(1));
        when(reactiveUserRepository.updateFailedLoginCount("usr-1", false, attempt.occurredAt()))
                .thenReturn(Mono.just(1));

        adapter.recordLoginAttempt(userId, attempt).block();

        verify(reactiveUserRepository).updateFailedLoginCount("usr-1", false, attempt.occurredAt());
    }

    @Test
    void shouldFailRecordingLoginAttemptWhenUserDoesNotExist() {
        UserR2dbcRepositoryAdapter adapter = new UserR2dbcRepositoryAdapter(
                reactiveUserRepository,
                reactiveUserCredentialRepository,
                reactiveCredentialRepository,
                reactiveUserRoleRepository,
                reactiveRoleRepository,
                reactiveUserLoginAttemptRepository,
                userRowMapper);

        AccountId userId = AccountId.of("missing");
        AuthenticationAttempt attempt = AuthenticationAttempt.success(
                userId,
                EmailAddress.of("missing@arka.com"),
                Instant.parse("2026-01-01T00:00:00Z"),
                "10.0.0.1");

        when(reactiveUserLoginAttemptRepository.insert(
                        anyString(), anyString(), anyString(), anyBoolean(), any(Instant.class), any(Instant.class)))
                .thenReturn(Mono.just(1));
        when(reactiveUserRepository.updateFailedLoginCount("missing", true, attempt.occurredAt()))
                .thenReturn(Mono.just(0));

        assertThrows(AccountNotFoundException.class, () -> adapter.recordLoginAttempt(userId, attempt).block());
    }
}
