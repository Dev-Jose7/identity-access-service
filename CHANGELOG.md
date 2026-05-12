# Changelog

All notable changes to this project are documented in this file.

This project uses semantic versioning.

## [1.0.0] - 2026-05-12

### Added

- Stable self-hosted IAM service for authentication, authorization and session lifecycle.
- Public account registration, primary administrator registration and administrative account creation flows.
- Email/password authentication with failed-attempt tracking and security audit records.
- Access token and refresh token lifecycle with RSA/RS256 JWT signing and JWKS publication.
- Token introspection endpoint for tokens issued by this service.
- Dynamic RBAC catalog with accounts, roles, permissions, role-permission grants and access profile resolution.
- Administrative endpoints for account listing, blocking, unblocking, role assignment, session revocation and effective permissions.
- Redis-backed rate limiting for sensitive public authentication flows.
- PostgreSQL schema bootstrap for local and Docker environments.
- Outbox persistence and Kafka relay for security and IAM integration events.
- Authentication failure event publication through outbox/Kafka without sensitive data.
- Operational dashboard for account, session, role and permission management.
- Docker Compose support for PostgreSQL, Redis, Kafka and the service runtime.
- Release smoke script, Kafka topic provisioning script and endpoint quick-test documentation.

### Changed

- OpenAPI version now follows `APP_VERSION` and defaults to `1.0.0`.
- OpenAPI license metadata now references Elastic License 2.0.
- Docker packaging uses deterministic service artifact selection.
- Outbox relay now records retry count, last error and failed status instead of silently swallowing failures.

### Security

- Administrative endpoints are protected by permission-based method security.
- Runtime authorities are derived from persisted roles and permissions.
- JWT keys are externalizable through environment variables and support rollover public keys.
- Blocked accounts and revoked sessions are rejected even if a token is cryptographically valid.
- Authentication failures are tracked as attempts and published as security events without passwords, hashes or tokens.

### Migration Notes

- Provision Kafka topics before enabling the relay in non-local environments. See `docs/operations/kafka-topics.md`.
- Do not use bundled development RSA keys in production.
- For production deployments, disable schema bootstrap unless explicitly running a controlled initialization.
- Review `.env.example` and configure PostgreSQL, Redis, Kafka, JWT keys and CORS for the target environment.

### Breaking Changes

- None. This is the first stable release.
