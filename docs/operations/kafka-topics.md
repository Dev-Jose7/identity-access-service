# Kafka Topics

`identity-access-service` publishes domain integration events through the outbox relay. Production Kafka clusters commonly disable topic auto-creation, so topics must exist before enabling the relay.

## Create Topics With Docker Compose

Use this mode for the bundled local Kafka service:

```bash
KAFKA_TOPICS_CREATE_MODE=docker-compose scripts/operations/create-kafka-topics.sh
```

The script runs `kafka-topics.sh` inside the `kafka` Compose service and defaults to `kafka:9092`.

## Create Topics With A Kafka CLI

Use this mode for external clusters:

```bash
KAFKA_TOPICS_CREATE_MODE=cli \
KAFKA_BOOTSTRAP_SERVERS=<broker:9092> \
KAFKA_TOPIC_PARTITIONS=3 \
KAFKA_TOPIC_REPLICATION_FACTOR=3 \
scripts/operations/create-kafka-topics.sh
```

## Topic Names

The script reads the same topic environment variables used by the service:

```env
APP_KAFKA_TOPIC_SESSION_OPENED=iam.session-opened.v1
APP_KAFKA_TOPIC_ACCOUNT_REGISTERED=iam.account-registered.v1
APP_KAFKA_TOPIC_AUTH_FAILED=iam.auth-failed.v1
APP_KAFKA_TOPIC_SESSION_REFRESHED=iam.session-refreshed.v1
APP_KAFKA_TOPIC_SESSION_REVOKED=iam.session-revoked.v1
APP_KAFKA_TOPIC_ROLE_ASSIGNED_TO_ACCOUNT=iam.role-assigned-to-account.v1
APP_KAFKA_TOPIC_ACCOUNT_BLOCKED=iam.account-blocked.v1
APP_KAFKA_TOPIC_ACCOUNT_UNBLOCKED=iam.account-unblocked.v1
APP_KAFKA_TOPIC_ROLE_CREATED=iam.role-created.v1
APP_KAFKA_TOPIC_ROLE_UPDATED=iam.role-updated.v1
APP_KAFKA_TOPIC_ROLE_DISABLED=iam.role-disabled.v1
APP_KAFKA_TOPIC_PERMISSION_CREATED=iam.permission-created.v1
APP_KAFKA_TOPIC_PERMISSION_UPDATED=iam.permission-updated.v1
APP_KAFKA_TOPIC_PERMISSION_DISABLED=iam.permission-disabled.v1
APP_KAFKA_TOPIC_PERMISSION_GRANTED_TO_ROLE=iam.permission-granted-to-role.v1
APP_KAFKA_TOPIC_PERMISSION_REVOKED_FROM_ROLE=iam.permission-revoked-from-role.v1
```

If a topic name is customized for the service, export the same variable before running the script.
