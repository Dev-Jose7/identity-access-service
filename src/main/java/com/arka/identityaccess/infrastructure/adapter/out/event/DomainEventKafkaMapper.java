package com.arka.identityaccess.infrastructure.adapter.out.event;

import com.arka.identityaccess.application.port.out.event.DomainEventTopicPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DomainEventKafkaMapper implements DomainEventTopicPort {

    private final String sessionOpenedTopic;
    private final String userRegisteredTopic;
    private final String sessionRefreshedTopic;
    private final String sessionRevokedTopic;
    private final String roleAssignedTopic;
    private final String accessProfileChangedTopic;
    private final String userBlockedTopic;

    public DomainEventKafkaMapper(
            @Value("${app.kafka.topics.session-opened:iam.session-opened.v1}") String sessionOpenedTopic,
            @Value("${app.kafka.topics.user-registered:iam.user-registered.v1}") String userRegisteredTopic,
            @Value("${app.kafka.topics.session-refreshed:iam.session-refreshed.v1}") String sessionRefreshedTopic,
            @Value("${app.kafka.topics.session-revoked:iam.session-revoked.v1}") String sessionRevokedTopic,
            @Value("${app.kafka.topics.role-assigned:iam.user-role-assigned.v1}") String roleAssignedTopic,
            @Value("${app.kafka.topics.access-profile-changed:iam.access-profile-changed.v1}") String accessProfileChangedTopic,
            @Value("${app.kafka.topics.user-blocked:iam.user-blocked.v1}") String userBlockedTopic) {
        this.sessionOpenedTopic = sessionOpenedTopic;
        this.userRegisteredTopic = userRegisteredTopic;
        this.sessionRefreshedTopic = sessionRefreshedTopic;
        this.sessionRevokedTopic = sessionRevokedTopic;
        this.roleAssignedTopic = roleAssignedTopic;
        this.accessProfileChangedTopic = accessProfileChangedTopic;
        this.userBlockedTopic = userBlockedTopic;
    }

    @Override
    public String topicFor(String eventType) {
        return switch (eventType) {
            case "SessionOpened", "UserLoggedIn" -> sessionOpenedTopic;
            case "UserRegistered", "AccountRegistered" -> userRegisteredTopic;
            case "SessionRefreshed" -> sessionRefreshedTopic;
            case "SessionRevoked" -> sessionRevokedTopic;
            case "RoleAssigned", "RoleAssignedToAccount" -> roleAssignedTopic;
            case "AccessProfileChanged" -> accessProfileChangedTopic;
            case "UserBlocked", "AccountBlocked" -> userBlockedTopic;
            default -> throw new IllegalStateException("No Kafka topic mapping defined for event type: " + eventType);
        };
    }
}
