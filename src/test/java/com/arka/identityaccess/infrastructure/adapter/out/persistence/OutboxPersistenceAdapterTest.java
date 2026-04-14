package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.OutboxRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class OutboxPersistenceAdapterTest {

    @Mock
    private ReactiveOutboxEventRepository reactiveOutboxEventRepository;

    @Mock
    private OutboxRowMapper outboxRowMapper;

    @Test
    void shouldStoreOutboxEventWhenInsertSucceeds() {
        OutboxPersistenceAdapter adapter = new OutboxPersistenceAdapter(reactiveOutboxEventRepository, outboxRowMapper);
        OutboxEventRow row = new OutboxEventRow(
                "evt-1",
                "User",
                "usr-1",
                "UserLoggedIn",
                "{\"ok\":true}",
                "PENDING",
                Instant.now(),
                null,
                0,
                null,
                Instant.now(),
                Instant.now());

        when(outboxRowMapper.toRow(any())).thenReturn(row);
        when(reactiveOutboxEventRepository.insert(
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(1));

        adapter.store(new TestDomainEvent()).block();
    }

    @Test
    void shouldFailWhenInsertDoesNotAffectExactlyOneRow() {
        OutboxPersistenceAdapter adapter = new OutboxPersistenceAdapter(reactiveOutboxEventRepository, outboxRowMapper);
        OutboxEventRow row = new OutboxEventRow(
                "evt-1",
                "User",
                "usr-1",
                "UserLoggedIn",
                "{\"ok\":true}",
                "PENDING",
                Instant.now(),
                null,
                0,
                null,
                Instant.now(),
                Instant.now());

        when(outboxRowMapper.toRow(any())).thenReturn(row);
        when(reactiveOutboxEventRepository.insert(
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(0));

        assertThrows(IllegalStateException.class, () -> adapter.store(new TestDomainEvent()).block());
    }

    private static final class TestDomainEvent implements DomainEvent {

        @Override
        public String eventId() {
            return "evt-1";
        }

        @Override
        public String eventType() {
            return "UserLoggedIn";
        }

        @Override
        public Instant occurredAt() {
            return Instant.parse("2026-01-01T00:00:00Z");
        }

        @Override
        public String aggregateId() {
            return "usr-1";
        }

        @Override
        public String aggregateType() {
            return "User";
        }
    }
}
