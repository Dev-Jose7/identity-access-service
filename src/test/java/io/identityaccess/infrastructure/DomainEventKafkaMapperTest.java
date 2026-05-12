package io.identityaccess.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.identityaccess.application.port.out.persistence.OutboxPersistencePort.PendingOutboxEvent;
import io.identityaccess.infrastructure.adapter.out.event.DomainEventKafkaMapper;
import org.junit.jupiter.api.Test;

class DomainEventKafkaMapperTest {

    @Test
    void shouldMapAccountAuthenticationFailedToConfiguredTopic() {
        DomainEventKafkaMapper mapper = mapper();
        PendingOutboxEvent row = row("AccountAuthenticationFailed");

        assertEquals("iam.auth-failed.v1", mapper.topicFor(row));
    }

    @Test
    void shouldRejectUnknownEventTypesWithoutFallback() {
        DomainEventKafkaMapper mapper = mapper();
        PendingOutboxEvent row = row("UnknownEvent");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> mapper.topicFor(row));

        assertEquals("No Kafka topic mapping for domain event type: UnknownEvent", exception.getMessage());
    }

    private DomainEventKafkaMapper mapper() {
        return new DomainEventKafkaMapper(
                "iam.session-opened.v1",
                "iam.account-registered.v1",
                "iam.auth-failed.v1",
                "iam.session-refreshed.v1",
                "iam.session-revoked.v1",
                "iam.role-assigned-to-account.v1",
                "iam.account-blocked.v1",
                "iam.account-unblocked.v1",
                "iam.role-created.v1",
                "iam.role-updated.v1",
                "iam.role-disabled.v1",
                "iam.permission-created.v1",
                "iam.permission-updated.v1",
                "iam.permission-disabled.v1",
                "iam.permission-granted-to-role.v1",
                "iam.permission-revoked-from-role.v1");
    }

    private PendingOutboxEvent row(String eventType) {
        return new PendingOutboxEvent(
                "evt-1",
                "Account",
                "usr-1",
                eventType,
                "{}",
                0);
    }
}
