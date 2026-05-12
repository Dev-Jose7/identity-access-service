# Deployment Guide

This document describes how to deploy `identity-access-service` outside a local developer workflow.

## Runtime Dependencies

Required services:

- PostgreSQL 15 or compatible.
- Redis 7 or compatible.
- Apache Kafka 3.8 or compatible.
- RSA key pair for JWT signing and JWKS publication.

## Recommended Profile

Use the production profile:

```bash
SPRING_PROFILES_ACTIVE=prod
```

The production profile expects externalized configuration and secrets.

## Required Configuration

Minimum required groups:

- Database: `APP_DATABASE_*`.
- Redis: `APP_REDIS_*`.
- Kafka: `APP_KAFKA_*`.
- JWT/JWKS: `APP_SECURITY_JWT_*`.
- CORS: `APP_SECURITY_CORS_ALLOWED_ORIGINS`.
- Version: `APP_VERSION`.

Use `.env.example` as the complete property reference.

## Database

Production environments should run controlled schema migrations or initialization before traffic is routed to the service.

Do not enable runtime schema bootstrap in production unless this is an intentional, controlled initialization:

```bash
APP_DATABASE_SCHEMA_INITIALIZE_ON_STARTUP=false
```

Local and Docker profiles can initialize schema automatically for development.

## Redis

Redis backs rate limiting and must be reachable before authentication traffic is enabled.

Operational expectations:

- Use authentication.
- Restrict network access.
- Monitor Redis availability and latency.
- Configure memory policies appropriate for rate-limit counters.

## Kafka

Provision topics before starting the relay in production-like environments:

```bash
./scripts/operations/create-kafka-topics.sh
```

See `docs/operations/kafka-topics.md` for topic names and environment variables.

## JWT Keys

Production must not use the bundled development keys.

Configure:

```bash
APP_SECURITY_JWT_KEY_ID=prod-key-2026-05
APP_SECURITY_JWT_PRIVATE_KEY_PATH=/run/secrets/jwt-private.pem
APP_SECURITY_JWT_PUBLIC_KEY_PATH=/run/secrets/jwt-public.pem
APP_SECURITY_JWT_ADDITIONAL_PUBLIC_KEYS=old-key=/run/secrets/jwt-old-public.pem
```

The `kid` in issued JWTs must match the active JWKS key. Rollover public keys should remain published until all old tokens expire.

## Startup Order

Recommended order:

1. PostgreSQL available and schema initialized.
2. Redis available.
3. Kafka available and topics provisioned.
4. `identity-access-service` starts.
5. Health and smoke checks pass.
6. Traffic is routed to the service.

## Health Checks

```bash
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8080/.well-known/jwks.json
curl -fsS http://localhost:8080/v3/api-docs
```

## Observability Signals

Monitor at least:

- Login failures.
- 429 rate-limit responses.
- Blocked accounts.
- Revoked sessions.
- Outbox pending and failed events.
- Kafka publication failures.
- Redis connection errors.
- JWT verification failures.

## Rollback

A rollback must preserve database compatibility. Before rolling back:

- Confirm schema changes are backward-compatible.
- Confirm Kafka event consumers can handle events emitted by both versions.
- Keep JWKS rollover keys available for tokens issued by the previous version.
- Do not delete outbox events during rollback.
