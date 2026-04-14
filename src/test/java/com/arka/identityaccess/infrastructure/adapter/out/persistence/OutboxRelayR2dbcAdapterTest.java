package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.port.out.persistence.PendingOutboxEvent;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class OutboxRelayR2dbcAdapterTest {

    @Mock
    private ReactiveOutboxEventRepository reactiveOutboxEventRepository;

    @Test
    void shouldMapPendingRowsToApplicationModel() {
        OutboxRelayR2dbcAdapter adapter = new OutboxRelayR2dbcAdapter(reactiveOutboxEventRepository);
        OutboxEventRow row = new OutboxEventRow(
                "evt-1",
                "User",
                "usr-1",
                "UserLoggedIn",
                "{\"ok\":true}",
                "PENDING",
                Instant.now(),
                null,
                2,
                "err",
                Instant.now(),
                Instant.now());

        when(reactiveOutboxEventRepository.findPending(10)).thenReturn(Flux.just(row));

        PendingOutboxEvent pending = adapter.findPending(10).blockFirst();
        assertEquals("evt-1", pending.eventId());
        assertEquals("UserLoggedIn", pending.eventType());
        assertEquals(2, pending.retryCount());
    }

    @Test
    void shouldMarkPublishedWhenRepositoryUpdatesSingleRow() {
        OutboxRelayR2dbcAdapter adapter = new OutboxRelayR2dbcAdapter(reactiveOutboxEventRepository);
        when(reactiveOutboxEventRepository.markPublished(eq("evt-1"), any())).thenReturn(Mono.just(1));

        adapter.markPublished("evt-1", Instant.now()).block();
    }

    @Test
    void shouldFailWhenMarkPublishedDoesNotUpdateRow() {
        OutboxRelayR2dbcAdapter adapter = new OutboxRelayR2dbcAdapter(reactiveOutboxEventRepository);
        when(reactiveOutboxEventRepository.markPublished(eq("evt-1"), any())).thenReturn(Mono.just(0));

        assertThrows(IllegalStateException.class, () -> adapter.markPublished("evt-1", Instant.now()).block());
    }

    @Test
    void shouldMarkFailureAndIncrementRetries() {
        OutboxRelayR2dbcAdapter adapter = new OutboxRelayR2dbcAdapter(reactiveOutboxEventRepository);
        when(reactiveOutboxEventRepository.markFailed(eq("evt-1"), eq("kafka down"), any(), eq(3)))
                .thenReturn(Mono.just(1));

        adapter.markFailed("evt-1", "kafka down", Instant.now(), 3).block();
    }
}
