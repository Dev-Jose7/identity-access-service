# Threat Model

## Scope

This threat model covers the `identity-access-service` runtime responsible for account identity, authentication, authorization, JWT/JWKS, sessions, audit and outbox publication.

Out of scope:

- Extended user profile data owned by external systems.
- Business workflows such as catalog, inventory, orders or reporting.
- Notification delivery internals.

## Assets

- Account identity and status.
- Password credentials and password hashes.
- Access and refresh tokens.
- RSA signing keys.
- Roles, permissions and access profiles.
- Session state.
- Audit records.
- Outbox events.
- PostgreSQL, Redis and Kafka credentials.

## Trust Boundaries

- Public HTTP boundary for registration, login, refresh, logout and introspection.
- Administrative HTTP boundary protected by permission-based authorization.
- PostgreSQL persistence boundary.
- Redis rate-limit boundary.
- Kafka event publication boundary.
- JWT consumer boundary through JWKS and claims.

## Threats and Controls

| Threat | Control |
| --- | --- |
| Brute force login | Redis rate limiting, failed-attempt tracking, account block support, auth-failed events. |
| Credential stuffing | Per-email and client-IP rate limiting, audit trail, event publication for monitoring. |
| Token forgery | RS256 signatures, JWKS publication, issuer/audience validation by consumers. |
| Token theft | Short-lived access tokens, refresh tokens bound to sessions, session revocation. |
| Refresh abuse | Refresh rate limiting, refresh JTI/session validation, rotation and revocation checks. |
| Stale claims | Session and account state checks through IAM endpoints; access profile is derived from persisted roles and permissions. |
| Privilege escalation | Method security, persisted authorities, role/permission governance, protected system role handling. |
| Blocked account reuse | Blocked accounts are rejected even if previous tokens are cryptographically valid where IAM validates active state. |
| Revoked session reuse | Revoked sessions cannot refresh and are rejected by session-aware flows. |
| Outbox/Kafka failure | Outbox retry count, last error tracking, failed status and explicit topic mapping. |
| Sensitive data leakage | Events exclude passwords, hashes, raw access tokens and raw refresh tokens. |
| Public registration abuse | Public registration rate limiting and primary-administrator uniqueness controls. |
| Secret exposure | Externalized JWT keys and credentials; development keys documented as non-production only. |

## Residual Risks

- JWT consumers that only validate the signature may accept stale claims until token expiration.
- If Redis is unavailable, rate-limit behavior depends on runtime error handling and operational policy.
- If Kafka is unavailable, events remain in outbox until relay retries or marks failure.
- Administrative misuse remains possible if high-privilege credentials are compromised.

## Operational Requirements

- Rotate JWT keys regularly and after suspected compromise.
- Monitor outbox failures and authentication failure spikes.
- Restrict infrastructure network access.
- Keep production secrets out of the repository.
- Use explicit CORS origins in production.
- Review access grants and system roles before each release.
