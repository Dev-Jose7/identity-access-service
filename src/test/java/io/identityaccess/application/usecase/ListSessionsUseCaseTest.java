package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.result.SessionSummaryResult;
import io.identityaccess.application.usecase.query.ListSessionsUseCase;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ListSessionsUseCaseTest {

    @Mock
    private SessionPersistencePort sessionPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldListSessionsFromPersistencePort() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(clockPort.now()).thenReturn(now);
        when(sessionPersistencePort.expireExpiredSessions(now)).thenReturn(reactor.core.publisher.Mono.just(1L));
        when(sessionPersistencePort.listSessions()).thenReturn(Flux.just(new SessionPersistencePort.SessionRecord(
                "session-1",
                "user-1",
                "ACTIVE",
                "127.0.0.1",
                "device-1",
                "browser",
                "WEB",
                now,
                now.plusSeconds(900),
                now.plusSeconds(604800),
                now,
                null,
                null,
                now,
                now)));

        StepVerifier.create(new ListSessionsUseCase(sessionPersistencePort, clockPort).handle())
                .assertNext(result -> assertSession(result, now))
                .verifyComplete();
    }

    private void assertSession(SessionSummaryResult result, Instant now) {
        assertEquals("session-1", result.sessionId());
        assertEquals("user-1", result.userId());
        assertEquals("ACTIVE", result.status());
        assertEquals("127.0.0.1", result.ipAddress());
        assertEquals(now.plusSeconds(900), result.accessTokenExpiresAt());
    }
}
