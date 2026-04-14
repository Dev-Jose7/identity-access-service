package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.exception.SessionNotFoundException;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.SessionRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.SessionRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveSessionRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SessionR2dbcRepositoryAdapterTest {

    @Mock
    private ReactiveSessionRepository reactiveSessionRepository;

    @Mock
    private SessionRowMapper sessionRowMapper;

    @Test
    void shouldCreateSessionWhenInsertSucceeds() {
        SessionR2dbcRepositoryAdapter adapter = new SessionR2dbcRepositoryAdapter(reactiveSessionRepository, sessionRowMapper);
        SessionAggregate aggregate = activeSession();
        SessionRow row = rowFrom(aggregate);

        when(sessionRowMapper.toRow(aggregate)).thenReturn(row);
        when(reactiveSessionRepository.insert(
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(1));

        SessionAggregate created = adapter.create(aggregate).block();
        assertEquals(aggregate.id().value(), created.id().value());
    }

    @Test
    void shouldFailUpdateWhenSessionDoesNotExist() {
        SessionR2dbcRepositoryAdapter adapter = new SessionR2dbcRepositoryAdapter(reactiveSessionRepository, sessionRowMapper);
        SessionAggregate aggregate = activeSession();
        SessionRow row = rowFrom(aggregate);

        when(sessionRowMapper.toRow(aggregate)).thenReturn(row);
        when(reactiveSessionRepository.update(
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(0));

        assertThrows(SessionNotFoundException.class, () -> adapter.update(aggregate).block());
    }

    @Test
    void shouldFailWhenActiveSessionByRefreshJtiIsMissing() {
        SessionR2dbcRepositoryAdapter adapter = new SessionR2dbcRepositoryAdapter(reactiveSessionRepository, sessionRowMapper);
        String missingRefreshJti = "99999999-9999-9999-9999-999999999999";
        when(reactiveSessionRepository.findActiveByRefreshJti(missingRefreshJti)).thenReturn(Mono.empty());

        assertThrows(
                SessionNotFoundException.class,
                () -> adapter.findActiveByRefreshJti(RefreshJti.of(missingRefreshJti)).block());
    }

    private SessionAggregate activeSession() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return SessionAggregate.rehydrate(
                SessionId.of("11111111-1111-1111-1111-111111111111"),
                AccountId.of("usr-1"),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("127.0.0.1"),
                AccessJti.of("22222222-2222-2222-2222-222222222222"),
                RefreshJti.of("33333333-3333-3333-3333-333333333333"),
                SessionTimestamps.of(now, now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.ACTIVE);
    }

    private SessionRow rowFrom(SessionAggregate aggregate) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new SessionRow(
                aggregate.id().value(),
                aggregate.userId().value(),
                aggregate.clientDevice().deviceId(),
                aggregate.clientDevice().deviceName(),
                aggregate.clientDevice().deviceType(),
                aggregate.clientIp().value(),
                aggregate.accessJti().value(),
                aggregate.refreshJti().value(),
                aggregate.timestamps().createdAt(),
                aggregate.timestamps().accessTokenExpiresAt(),
                aggregate.timestamps().refreshTokenExpiresAt(),
                now,
                aggregate.status().name(),
                null,
                null,
                now,
                now);
    }
}
