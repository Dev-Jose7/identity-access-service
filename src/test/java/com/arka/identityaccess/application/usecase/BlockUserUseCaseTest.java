package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.command.BlockUserCommand;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.BlockUserResult;
import com.arka.identityaccess.application.usecase.command.BlockUserUseCase;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.entity.AccountCredential;
import com.arka.identityaccess.domain.identity.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class BlockUserUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldBlockUserAndPublishEventWhenStatusChanges() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        BlockUserCommand command = new BlockUserCommand("usr-10", "actor-1", "fraud");

        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.loadById(AccountId.of("usr-10"))).thenReturn(Mono.just(testUser("usr-10")));
        when(userPersistencePort.block(any(), eq(now)))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@arka.com", "BLOCKED")));
        when(securityAuditPort.recordUserBlocked("actor-1", "usr-10", "fraud", true)).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        BlockUserResult result = useCase.handle(command).block();

        assertEquals("usr-10", result.userId());
        assertEquals("BLOCKED", result.status());
        assertTrue(result.changed());
        verify(outboxPersistencePort).store(any());
    }

    @Test
    void shouldBeIdempotentWhenUserIsAlreadyBlocked() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        BlockUserCommand command = new BlockUserCommand("usr-10", "actor-1", null);

        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.loadById(AccountId.of("usr-10"))).thenReturn(Mono.just(testUser("usr-10", AccountStatus.BLOCKED)));
        when(securityAuditPort.recordUserBlocked("actor-1", "usr-10", "ADMIN_BLOCK", false)).thenReturn(Mono.empty());

        BlockUserResult result = useCase.handle(command).block();

        assertEquals("usr-10", result.userId());
        assertEquals("BLOCKED", result.status());
        assertEquals(false, result.changed());
        verify(outboxPersistencePort, never()).store(any());
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);

        BlockUserCommand command = new BlockUserCommand("usr-10", " ", "fraud");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
        verify(userPersistencePort, never()).loadById(any());
    }

    private AccountAggregate testUser(String userId) {
        return testUser(userId, AccountStatus.ACTIVE);
    }

    private AccountAggregate testUser(String userId, AccountStatus status) {
        AccountId id = AccountId.of(userId);
        EmailAddress email = EmailAddress.of("user@arka.com");
        return AccountAggregate.rehydrate(
                id,
                email,
                true,
                status,
                new AccountCredential("cred-" + userId, id, email, "hash", CredentialStatus.ACTIVE),
                0);
    }
}
