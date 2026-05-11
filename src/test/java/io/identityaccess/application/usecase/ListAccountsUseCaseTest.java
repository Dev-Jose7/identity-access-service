package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.AccountSummaryResult;
import io.identityaccess.application.usecase.query.ListAccountsUseCase;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ListAccountsUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Test
    void shouldListAccountsFromPersistencePort() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(userPersistencePort.listAccounts()).thenReturn(Flux.just(new UserPersistencePort.AccountRecord(
                "user-1",
                "admin@example.test",
                "ACTIVE",
                0,
                now,
                now,
                Set.of("SYSTEM_ADMIN"))));

        StepVerifier.create(new ListAccountsUseCase(userPersistencePort).handle())
                .assertNext(result -> assertAccount(result, now))
                .verifyComplete();
    }

    private void assertAccount(AccountSummaryResult result, Instant now) {
        assertEquals("user-1", result.userId());
        assertEquals("admin@example.test", result.email());
        assertEquals("ACTIVE", result.status());
        assertEquals(0, result.failedLoginCount());
        assertEquals(now, result.createdAt());
        assertEquals(Set.of("SYSTEM_ADMIN"), result.roles());
    }
}
