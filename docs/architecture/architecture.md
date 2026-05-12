# Architecture

## Overview

`identity-access-service` is a self-hosted IAM service for account identity, authentication, authorization and authenticated sessions.

The service follows Hexagonal/Clean Architecture:

- Domain owns business language, invariants, aggregate behavior and domain events.
- Application orchestrates use cases and depends on domain plus ports.
- Infrastructure implements persistence, Redis, Kafka, JWT/JWKS and framework adapters.
- Adapter-in HTTP exposes WebFlux controllers and static dashboard assets.

## Package Direction

Allowed dependency direction:

```text
adapter-in -> application -> domain
adapter-out -> application/domain
infrastructure config -> application/domain/adapter-out
```

Forbidden dependency direction:

```text
domain -> application
domain -> infrastructure
domain -> Spring/R2DBC/Redis/Kafka/JWT libraries
```

## Tactical Domain Areas

The domain is organized around one bounded context with internal tactical partitions:

- Identity: accounts, credentials, account status and authentication attempts.
- Access: roles, permissions, access assignments and effective access profile.
- Session: session lifecycle, refresh, revocation and token identifiers.
- Shared: common domain events and exceptions.

## Main Aggregates

- Account: governs account identity, status, credential eligibility and authentication consequences.
- Role: governs role catalog state and granted permissions.
- Permission: governs permission catalog semantics and lifecycle.
- AccountAccess: governs active/revoked role assignments for an account.
- Session: governs authenticated session state, refresh and revocation.

## Application Use Cases

Application use cases orchestrate:

- Public and primary account registration.
- Administrative account creation.
- Login, refresh, logout and token introspection.
- Account block/unblock.
- Role assignment and access profile resolution.
- Role and permission catalog administration.
- Session revocation.

Use cases are responsible for calling ports, transaction boundaries, audit, outbox and rate-limit gates.

## HTTP Adapter-In

Controller responsibilities:

- Validate request contracts.
- Extract authenticated principal when required.
- Invoke application use cases.
- Map use case results to response DTOs.

Controllers must not contain business rules.

## Security

- Spring Security WebFlux protects administrative endpoints.
- `@PreAuthorize` checks use persisted authorities.
- Runtime authorities are resolved from account roles and permissions.
- JWKS exposes public keys for JWT consumers.
- Introspection decodes and validates tokens issued by this service.

## Persistence

PostgreSQL stores:

- Accounts and credentials.
- Authentication attempts and technical audit records.
- Sessions.
- Roles, permissions and grants.
- Account access assignments.
- Outbox events and processed event tracking.

R2DBC adapters map domain/application models to rows. SQL row models stay in infrastructure.

## Redis

Redis backs rate limiting for sensitive public authentication flows. Rate limit concerns stay outside domain.

## Kafka and Outbox

Domain/application events are adapted to outbox records. The relay publishes explicitly mapped event types to Kafka topics.

Unknown event types must fail explicitly; they must not fall back to a business topic.

## Static Dashboard

The dashboard is a convenience UI served by the service for IAM administration. It consumes the same HTTP API and reacts to the authenticated user's permissions.

## Configuration

Configuration is environment-driven through `.env.example`, `application.yml` and profile-specific overrides.

Important groups:

- `APP_DATABASE_*`
- `APP_REDIS_*`
- `APP_KAFKA_*`
- `APP_SECURITY_JWT_*`
- `APP_SECURITY_CORS_*`
- `APP_VERSION`

## Release Assets

Release readiness is supported by:

- `CHANGELOG.md`.
- `SECURITY.md`.
- `CONTRIBUTING.md`.
- `docs/operations/release-checklist.md`.
- `docs/operations/deployment.md`.
- `docs/security/threat-model.md`.
- `docs/operations/kafka-topics.md`.
- `docs/integration/consumer-integration-guide.md`.
