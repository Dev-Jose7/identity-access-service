package com.arka.identityaccess.infrastructure.adapter.out.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DomainEventKafkaMapperTest {

    private final DomainEventKafkaMapper mapper = new DomainEventKafkaMapper(
            "iam.session-opened.v1",
            "iam.user-registered.v1",
            "iam.session-refreshed.v1",
            "iam.session-revoked.v1",
            "iam.user-role-assigned.v1",
            "iam.access-profile-changed.v1",
            "iam.user-blocked.v1");

    @Test
    void shouldResolveKnownEventTypeToConfiguredTopic() {
        assertEquals("iam.session-opened.v1", mapper.topicFor("SessionOpened"));
        assertEquals("iam.session-opened.v1", mapper.topicFor("UserLoggedIn"));
        assertEquals("iam.session-refreshed.v1", mapper.topicFor("SessionRefreshed"));
        assertEquals("iam.user-role-assigned.v1", mapper.topicFor("RoleAssigned"));
        assertEquals("iam.user-role-assigned.v1", mapper.topicFor("RoleAssignedToAccount"));
        assertEquals("iam.access-profile-changed.v1", mapper.topicFor("AccessProfileChanged"));
        assertEquals("iam.user-registered.v1", mapper.topicFor("AccountRegistered"));
        assertEquals("iam.user-blocked.v1", mapper.topicFor("AccountBlocked"));
    }

    @Test
    void shouldFailForUnknownEventType() {
        assertThrows(IllegalStateException.class, () -> mapper.topicFor("UnknownEvent"));
    }
}
