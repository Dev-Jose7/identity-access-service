package com.arka.identityaccess.infrastructure.adapter.out.event;

import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DomainEventKafkaMapper {

    private final String userLoggedInTopic;
    private final String userRegisteredTopic;
    private final String sessionRefreshedTopic;
    private final String sessionRevokedTopic;
    private final String sessionsRevokedByUserTopic;
    private final String roleAssignedTopic;
    private final String userBlockedTopic;

    public DomainEventKafkaMapper(
            @Value("${app.kafka.topics.user-logged-in:iam.user-logged-in.v1}") String userLoggedInTopic,
            @Value("${app.kafka.topics.user-registered:iam.user-registered.v1}") String userRegisteredTopic,
            @Value("${app.kafka.topics.session-refreshed:iam.session-refreshed.v1}") String sessionRefreshedTopic,
            @Value("${app.kafka.topics.session-revoked:iam.session-revoked.v1}") String sessionRevokedTopic,
            @Value("${app.kafka.topics.sessions-revoked-by-user:iam.sessions-revoked-by-user.v1}") String sessionsRevokedByUserTopic,
            @Value("${app.kafka.topics.role-assigned:iam.user-role-assigned.v1}") String roleAssignedTopic,
            @Value("${app.kafka.topics.user-blocked:iam.user-blocked.v1}") String userBlockedTopic) {
        this.userLoggedInTopic = userLoggedInTopic;
        this.userRegisteredTopic = userRegisteredTopic;
        this.sessionRefreshedTopic = sessionRefreshedTopic;
        this.sessionRevokedTopic = sessionRevokedTopic;
        this.sessionsRevokedByUserTopic = sessionsRevokedByUserTopic;
        this.roleAssignedTopic = roleAssignedTopic;
        this.userBlockedTopic = userBlockedTopic;
    }

    public String topicFor(OutboxEventRow row) {
        return switch (row.eventType()) {
            case "UserRegistered" -> userRegisteredTopic;
            case "SessionRefreshed" -> sessionRefreshedTopic;
            case "SessionRevoked" -> sessionRevokedTopic;
            case "SessionsRevokedByUser" -> sessionsRevokedByUserTopic;
            case "RoleAssigned" -> roleAssignedTopic;
            case "UserBlocked" -> userBlockedTopic;
            default -> userLoggedInTopic;
        };
    }

    public String keyFor(OutboxEventRow row) { return row.aggregateId(); }
    public String payloadFor(OutboxEventRow row) { return row.payload(); }
}
