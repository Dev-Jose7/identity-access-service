# identity-access-service

Identity-access-service is the ArkaB2B microservice responsible for identity, authentication, session management, and access-related capabilities in a B2B context. This repository contains runtime configuration and infrastructure setup for local and containerized execution.

## Overview
- Provides authentication, session lifecycle, and role-based authorization support.
- Exposes security-related endpoints such as login, refresh/logout, token introspection, and JWKS.
- Integrates with PostgreSQL, Redis, and Kafka for persistence, caching, and asynchronous integration.

## Configuration policy (canonical base)
- `.env` is the primary local value source.
- `docker-compose.yml` is the source of truth for local infrastructure.
- `application*.yml` defines profile structure and property consumption.
- Fallbacks are limited to non-sensitive values.
- `prod` does not allow functional fallback for critical secrets/config.

## JWT signing mode
- The service signs JWT with **RSA/RS256** (asymmetric keys).
- Local development keys are in:
  - `src/main/resources/keys/dev-private.pem`
  - `src/main/resources/keys/dev-public.pem`
  - `src/main/resources/keys/dev-legacy-public.pem` (optional rollover verification key)
- `GET /.well-known/jwks.json` exposes all configured verification public keys (active + rollover keys) for gateway/downstream validation.
- Access tokens include `email`, `roles`, and `permissions` claims for distributed authorization/context reconstruction.
- Key rollover is configured with:
  - `APP_SECURITY_JWT_KEY_ID` (active signing `kid`)
  - `APP_SECURITY_JWT_PRIVATE_KEY_PATH` (active private key)
  - `APP_SECURITY_JWT_PUBLIC_KEY_PATH` (active public key)
  - `APP_SECURITY_JWT_ADDITIONAL_PUBLIC_KEYS` (`kid=path,kid2=path2`) for legacy verification keys
- Development keys are only for local use. In production, key material must come from secure secret management (vault/KMS/infra-managed secrets).
- Trust contract (gateway/downstream): `docs/security/gateway-downstream-trust-contract.md`.

## Spring profiles
- `local`: app on host (Gradle/IDE), dependencies in Docker.
- `docker`: app in Docker Compose network.
- `prod`: externalized production configuration.

Files:
- `src/main/resources/application.yml` (common neutral base)
- `src/main/resources/application-local.yml`
- `src/main/resources/application-docker.yml`
- `src/main/resources/application-prod.yml`

Schema bootstrap behavior:
- `local` / `docker`: enabled by default.
- other profiles (`prod`, `qa`, `staging`): disabled by default unless `APP_DATABASE_SCHEMA_INITIALIZE_ON_STARTUP=true` is explicitly set.

## Run local (Gradle + dockerized dependencies)
```bash
cp .env.example .env
set -a
source .env
set +a
docker compose up -d postgres redis kafka
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun --no-daemon
```

PostgreSQL loads the schema from `init-identity-access.sql` on first database initialization.
If you already had an older container/schema, recreate PostgreSQL before retrying:

```bash
docker compose down -v
docker compose up -d postgres redis kafka
```

## Register flows
- Public founder registration: `POST /api/v1/auth/register-founder` (creates the IAM founder user first, assigns `ORG_OWNER`).
- Administrative user creation: `POST /api/v1/admin/iam/users` (requires authenticated role `ORG_OWNER`, `ORG_ADMIN`, or `ARKA_ADMIN`).
- IAM does not persist `organizationId`; Directory stores the organization and links the founder `userId` after this step.

## Rate limiting
- Active endpoints:
  - `POST /api/v1/auth/login`
  - `POST /api/v1/auth/refresh`
  - `POST /api/v1/auth/register-founder`
- Behavior:
  - Requests over threshold are rejected with `HTTP 429`.
  - Error envelope follows service conventions (`code: operacion_no_permitida`).
- Config keys:
  - `APP_REDIS_RATE_LIMIT_LOGIN_RPM`
  - `APP_REDIS_RATE_LIMIT_REFRESH_RPM`
  - `APP_REDIS_RATE_LIMIT_REGISTER_FOUNDER_RPM`

## Run fully containerized
```bash
cp .env.example .env
docker compose up -d --build
```

## Validate local Kafka
```bash
docker compose ps kafka
docker compose logs -f kafka
docker compose exec kafka /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server kafka:9092
```

## API smoke tests
```bash
curl -s http://localhost:8080/.well-known/jwks.json | jq

curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -H 'User-Agent: curl' \
  -d '{
    "email":"admin@arka.local",
    "password":"Admin123!"
  }' | jq

curl -s -X POST http://localhost:8080/api/v1/auth/register-founder \
  -H 'Content-Type: application/json' \
  -d '{
    "email":"new.user@arka.local",
    "password":"NewUser123!"
  }' | jq
```

## Swagger / OpenAPI
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

You can configure these paths with:
- `SPRINGDOC_SWAGGER_UI_PATH`
- `SPRINGDOC_API_DOCS_PATH`

## Responsibility split
- `.env`: concrete local values.
- `docker-compose.yml`: infra topology, published ports, container env.
- `application*.yml`: how app consumes env by profile.
