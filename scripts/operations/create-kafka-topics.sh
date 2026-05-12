#!/usr/bin/env bash
set -euo pipefail

MODE="${KAFKA_TOPICS_CREATE_MODE:-docker-compose}"
PARTITIONS="${KAFKA_TOPIC_PARTITIONS:-1}"
REPLICATION_FACTOR="${KAFKA_TOPIC_REPLICATION_FACTOR:-1}"

if [[ "$MODE" == "docker-compose" ]]; then
  BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-kafka:9092}"
  KAFKA_TOPICS_CMD=(docker compose exec -T kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP_SERVERS")
elif [[ "$MODE" == "cli" ]]; then
  BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"
  KAFKA_TOPICS_BIN="${KAFKA_TOPICS_BIN:-kafka-topics.sh}"
  KAFKA_TOPICS_CMD=("$KAFKA_TOPICS_BIN" --bootstrap-server "$BOOTSTRAP_SERVERS")
else
  echo "Unsupported KAFKA_TOPICS_CREATE_MODE: $MODE. Use docker-compose or cli." >&2
  exit 1
fi

TOPICS=(
  "${APP_KAFKA_TOPIC_SESSION_OPENED:-iam.session-opened.v1}"
  "${APP_KAFKA_TOPIC_ACCOUNT_REGISTERED:-iam.account-registered.v1}"
  "${APP_KAFKA_TOPIC_AUTH_FAILED:-iam.auth-failed.v1}"
  "${APP_KAFKA_TOPIC_SESSION_REFRESHED:-iam.session-refreshed.v1}"
  "${APP_KAFKA_TOPIC_SESSION_REVOKED:-iam.session-revoked.v1}"
  "${APP_KAFKA_TOPIC_ROLE_ASSIGNED_TO_ACCOUNT:-iam.role-assigned-to-account.v1}"
  "${APP_KAFKA_TOPIC_ACCOUNT_BLOCKED:-iam.account-blocked.v1}"
  "${APP_KAFKA_TOPIC_ACCOUNT_UNBLOCKED:-iam.account-unblocked.v1}"
  "${APP_KAFKA_TOPIC_ROLE_CREATED:-iam.role-created.v1}"
  "${APP_KAFKA_TOPIC_ROLE_UPDATED:-iam.role-updated.v1}"
  "${APP_KAFKA_TOPIC_ROLE_DISABLED:-iam.role-disabled.v1}"
  "${APP_KAFKA_TOPIC_PERMISSION_CREATED:-iam.permission-created.v1}"
  "${APP_KAFKA_TOPIC_PERMISSION_UPDATED:-iam.permission-updated.v1}"
  "${APP_KAFKA_TOPIC_PERMISSION_DISABLED:-iam.permission-disabled.v1}"
  "${APP_KAFKA_TOPIC_PERMISSION_GRANTED_TO_ROLE:-iam.permission-granted-to-role.v1}"
  "${APP_KAFKA_TOPIC_PERMISSION_REVOKED_FROM_ROLE:-iam.permission-revoked-from-role.v1}"
)

printf '%s\n' "Creating Kafka topics through mode=$MODE bootstrap=$BOOTSTRAP_SERVERS partitions=$PARTITIONS replicationFactor=$REPLICATION_FACTOR"

for topic in "${TOPICS[@]}"; do
  if [[ -z "$topic" ]]; then
    continue
  fi

  "${KAFKA_TOPICS_CMD[@]}" \
    --create \
    --if-not-exists \
    --topic "$topic" \
    --partitions "$PARTITIONS" \
    --replication-factor "$REPLICATION_FACTOR"
  printf 'OK topic %s\n' "$topic"
done
