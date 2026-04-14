package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.AuthAuditRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.AuthAuditRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveAuthAuditRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AuthAuditR2dbcRepositoryAdapterTest {

    @Mock
    private ReactiveAuthAuditRepository reactiveAuthAuditRepository;

    @Mock
    private AuthAuditRowMapper authAuditRowMapper;

    @Test
    void shouldInsertAuditRowForLoginSuccess() {
        AuthAuditR2dbcRepositoryAdapter adapter =
                new AuthAuditR2dbcRepositoryAdapter(reactiveAuthAuditRepository, authAuditRowMapper);

        AuthAuditRow row = new AuthAuditRow(
                "aud-1",
                "LOGIN_SUCCESS",
                "usr-1",
                "11111111-1111-1111-1111-111111111111",
                "127.0.0.1",
                "device-1",
                "SUCCESS",
                "{\"ok\":true}",
                Instant.now());

        when(authAuditRowMapper.toLoginSuccessRow(any(), any())).thenReturn(row);
        when(reactiveAuthAuditRepository.insert(
                        any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(1));

        adapter.recordLoginSuccess(mock(AccountAggregate.class), mock(SessionAggregate.class)).block();
    }

    @Test
    void shouldFailWhenAuditInsertDoesNotAffectOneRow() {
        AuthAuditR2dbcRepositoryAdapter adapter =
                new AuthAuditR2dbcRepositoryAdapter(reactiveAuthAuditRepository, authAuditRowMapper);

        AuthAuditRow row = new AuthAuditRow(
                "aud-1",
                "LOGIN_SUCCESS",
                "usr-1",
                "11111111-1111-1111-1111-111111111111",
                "127.0.0.1",
                "device-1",
                "SUCCESS",
                "{\"ok\":true}",
                Instant.now());

        when(authAuditRowMapper.toLoginSuccessRow(any(), any())).thenReturn(row);
        when(reactiveAuthAuditRepository.insert(
                        any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(0));

        assertThrows(
                IllegalStateException.class,
                () -> adapter.recordLoginSuccess(mock(AccountAggregate.class), mock(SessionAggregate.class)).block());
    }
}
