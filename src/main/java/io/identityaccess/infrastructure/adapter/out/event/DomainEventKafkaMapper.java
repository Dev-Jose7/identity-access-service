package io.identityaccess.infrastructure.adapter.out.event;

import io.identityaccess.application.port.out.persistence.OutboxPersistencePort.PendingOutboxEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DomainEventKafkaMapper {

    private final String sessionOpenedTopic;
    private final String accountRegisteredTopic;
    private final String accountAuthenticationFailedTopic;
    private final String sessionRefreshedTopic;
    private final String sessionRevokedTopic;
    private final String roleAssignedToAccountTopic;
    private final String accountBlockedTopic;
    private final String accountUnblockedTopic;
    private final String roleCreatedTopic;
    private final String roleUpdatedTopic;
    private final String roleDisabledTopic;
    private final String permissionCreatedTopic;
    private final String permissionUpdatedTopic;
    private final String permissionDisabledTopic;
    private final String permissionGrantedToRoleTopic;
    private final String permissionRevokedFromRoleTopic;

    public DomainEventKafkaMapper(
            @Value("${app.kafka.topics.session-opened:iam.session-opened.v1}") String sessionOpenedTopic,
            @Value("${app.kafka.topics.account-registered:iam.account-registered.v1}") String accountRegisteredTopic,
            @Value("${app.kafka.topics.auth-failed:iam.auth-failed.v1}") String accountAuthenticationFailedTopic,
            @Value("${app.kafka.topics.session-refreshed:iam.session-refreshed.v1}") String sessionRefreshedTopic,
            @Value("${app.kafka.topics.session-revoked:iam.session-revoked.v1}") String sessionRevokedTopic,
            @Value("${app.kafka.topics.role-assigned-to-account:iam.role-assigned-to-account.v1}") String roleAssignedToAccountTopic,
            @Value("${app.kafka.topics.account-blocked:iam.account-blocked.v1}") String accountBlockedTopic,
            @Value("${app.kafka.topics.account-unblocked:iam.account-unblocked.v1}") String accountUnblockedTopic,
            @Value("${app.kafka.topics.role-created:iam.role-created.v1}") String roleCreatedTopic,
            @Value("${app.kafka.topics.role-updated:iam.role-updated.v1}") String roleUpdatedTopic,
            @Value("${app.kafka.topics.role-disabled:iam.role-disabled.v1}") String roleDisabledTopic,
            @Value("${app.kafka.topics.permission-created:iam.permission-created.v1}") String permissionCreatedTopic,
            @Value("${app.kafka.topics.permission-updated:iam.permission-updated.v1}") String permissionUpdatedTopic,
            @Value("${app.kafka.topics.permission-disabled:iam.permission-disabled.v1}") String permissionDisabledTopic,
            @Value("${app.kafka.topics.permission-granted-to-role:iam.permission-granted-to-role.v1}") String permissionGrantedToRoleTopic,
            @Value("${app.kafka.topics.permission-revoked-from-role:iam.permission-revoked-from-role.v1}") String permissionRevokedFromRoleTopic) {
        this.sessionOpenedTopic = sessionOpenedTopic;
        this.accountRegisteredTopic = accountRegisteredTopic;
        this.accountAuthenticationFailedTopic = accountAuthenticationFailedTopic;
        this.sessionRefreshedTopic = sessionRefreshedTopic;
        this.sessionRevokedTopic = sessionRevokedTopic;
        this.roleAssignedToAccountTopic = roleAssignedToAccountTopic;
        this.accountBlockedTopic = accountBlockedTopic;
        this.accountUnblockedTopic = accountUnblockedTopic;
        this.roleCreatedTopic = roleCreatedTopic;
        this.roleUpdatedTopic = roleUpdatedTopic;
        this.roleDisabledTopic = roleDisabledTopic;
        this.permissionCreatedTopic = permissionCreatedTopic;
        this.permissionUpdatedTopic = permissionUpdatedTopic;
        this.permissionDisabledTopic = permissionDisabledTopic;
        this.permissionGrantedToRoleTopic = permissionGrantedToRoleTopic;
        this.permissionRevokedFromRoleTopic = permissionRevokedFromRoleTopic;
    }

    public String topicFor(PendingOutboxEvent event) {
        return switch (event.eventType()) {
            case "SessionOpened" -> sessionOpenedTopic;
            case "AccountRegistered" -> accountRegisteredTopic;
            case "AccountAuthenticationFailed" -> accountAuthenticationFailedTopic;
            case "SessionRefreshed" -> sessionRefreshedTopic;
            case "SessionRevoked" -> sessionRevokedTopic;
            case "RoleAssignedToAccount" -> roleAssignedToAccountTopic;
            case "AccountBlocked" -> accountBlockedTopic;
            case "AccountUnblocked" -> accountUnblockedTopic;
            case "RoleCreated" -> roleCreatedTopic;
            case "RoleUpdated" -> roleUpdatedTopic;
            case "RoleDisabled" -> roleDisabledTopic;
            case "PermissionCreated" -> permissionCreatedTopic;
            case "PermissionUpdated" -> permissionUpdatedTopic;
            case "PermissionDisabled" -> permissionDisabledTopic;
            case "PermissionGrantedToRole" -> permissionGrantedToRoleTopic;
            case "PermissionRevokedFromRole" -> permissionRevokedFromRoleTopic;
            default -> throw new IllegalStateException("No Kafka topic mapping for domain event type: " + event.eventType());
        };
    }

    public String keyFor(PendingOutboxEvent event) { return event.aggregateId(); }
    public String payloadFor(PendingOutboxEvent event) { return event.payload(); }
}
