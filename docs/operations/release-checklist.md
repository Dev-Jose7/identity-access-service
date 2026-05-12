# Release Checklist

Use this checklist before publishing a stable release.

## 1. Repository State

- Confirm the branch is the intended release branch.
- Confirm `git status --short` only contains expected release changes.
- Confirm `CHANGELOG.md` has an entry for the release date and version.
- Confirm `README.md`, integration docs and operational docs match the current behavior.

## 2. Versioning

- Confirm `APP_VERSION` in `.env.example` matches the release.
- Confirm `app.version` default in `src/main/resources/application.yml` matches the release.
- Confirm OpenAPI reports the release version.
- Confirm no unintended `SNAPSHOT` version remains in release metadata.

## 3. Static Checks

```bash
docker compose config --quiet
git diff --check
bash -n scripts/operations/create-kafka-topics.sh
```

## 4. Test Suite

```bash
./gradlew clean test integrationTest bootJar --no-daemon
```

The release must not proceed if unit tests, integration tests or packaging fail.

## 5. Docker Build

```bash
docker compose build identity-access-service
```

Confirm the image builds from the deterministic boot jar artifact.

## 6. Local Runtime Smoke

```bash
docker compose up -d postgres redis kafka
./scripts/operations/create-kafka-topics.sh
docker compose up -d identity-access-service
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8080/v3/api-docs | jq '.info.version'
```

Expected OpenAPI version: the release version.

## 7. Functional Smoke

Run or follow:

```bash
BASE_URL=http://localhost:8080 scripts/smoke/identity-access-release-smoke.sh
```

At minimum verify:

- JWKS returns the active RSA public key.
- Primary administrator registration works once in a fresh database.
- Login returns access and refresh tokens.
- Introspection returns active token metadata.
- Refresh rotates tokens.
- Logout revokes the session.
- Administrative endpoints enforce permissions.

## 8. Kafka and Outbox

- Confirm all configured topics exist.
- Confirm `iam.auth-failed.v1` exists if authentication failure events are enabled.
- Confirm no unknown event type maps to a business topic by fallback.
- Confirm failed publication records `retry_count` and `last_error`.

## 9. Production Readiness

- Confirm production RSA keys are not development keys.
- Confirm production passwords/secrets are not committed.
- Confirm schema bootstrap is disabled by default for production.
- Confirm external PostgreSQL, Redis and Kafka endpoints are configured.
- Confirm CORS origins are explicit and environment-appropriate.

## 10. Cleanup

```bash
docker compose down --remove-orphans
```

Use `-v` only when intentionally deleting local PostgreSQL/Redis/Kafka data.
