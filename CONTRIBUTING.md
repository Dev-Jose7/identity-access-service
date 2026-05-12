# Contributing

## Development Requirements

- Java 21.
- Docker Desktop or compatible Docker daemon.
- Docker Compose v2.
- Gradle wrapper included in the repository.

## Local Setup

```bash
cp .env.example .env
docker compose up -d postgres redis kafka
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun --no-daemon
```

## Validation Commands

Run the main verification suite before submitting changes:

```bash
./gradlew clean test integrationTest bootJar --no-daemon
```

Useful additional checks:

```bash
docker compose config --quiet
git diff --check
bash -n scripts/operations/create-kafka-topics.sh
```

## Architecture Rules

- Domain must not depend on application, infrastructure, Spring, R2DBC, Redis, Kafka or JWT libraries.
- Application orchestrates use cases and depends on domain and ports.
- Infrastructure implements ports and owns framework, persistence, messaging and crypto integrations.
- HTTP controllers are adapter-in only and must not contain business rules.
- Authentication and authorization decisions must use persisted roles and permissions.
- Sensitive mutations must keep audit and outbox behavior coherent.

## Testing Expectations

Add or update tests when changing:

- Domain invariants, policies or events.
- Use cases.
- Security configuration or authorities.
- R2DBC adapters and SQL mappings.
- JWT/JWKS behavior.
- Outbox relay or Kafka mapping.
- Redis-backed rate limiting.
- HTTP contracts or dashboard behavior.

## Commit Style

Use concise conventional messages when possible:

- `feat: ...`
- `fix: ...`
- `docs: ...`
- `test: ...`
- `refactor: ...`
- `chore: ...`

## Release Flow

- Prepare the release branch.
- Update version-related configuration.
- Update `CHANGELOG.md`.
- Run the release checklist in `docs/operations/release-checklist.md`.
- Build and smoke-test the Docker image.
- Tag the release only after validation passes.
