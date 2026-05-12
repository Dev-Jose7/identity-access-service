# Security Policy

## Supported Versions

| Version | Supported |
| --- | --- |
| 1.0.x | Yes |
| < 1.0.0 | No |

## Reporting Vulnerabilities

Report suspected vulnerabilities through the private security channel configured by the project owner or repository maintainers. Do not open public issues for secrets, authentication bypasses, token handling defects or privilege escalation findings.

A useful report should include:

- Affected version or commit.
- Environment profile (`local`, `docker`, `prod`) if relevant.
- Reproduction steps.
- Expected and observed behavior.
- Logs or HTTP responses with secrets redacted.
- Assessment of impact.

## Secret Handling

- Never commit production `.env` files, RSA private keys, database passwords, Redis passwords or Kafka credentials.
- The bundled `classpath:keys/dev-*.pem` keys are development-only.
- Production keys must come from a secret manager or a secure mounted volume.
- Rotate JWT signing keys by publishing the new public key through JWKS before making it the active signer.
- Keep `APP_SECURITY_JWT_ADDITIONAL_PUBLIC_KEYS` configured during rollover so old tokens can be verified until they expire.

## JWT and JWKS Policy

- Access tokens are short-lived and signed with RS256.
- Refresh tokens are longer-lived, session-bound and validated against persisted session state.
- Consumers must validate `iss`, `aud`, `exp`, signature and `kid` against JWKS.
- A valid JWT signature is not enough for IAM acceptance: blocked accounts and revoked sessions must still be rejected by this service.

## Rate Limiting

Rate limiting is active for sensitive public flows:

- Login.
- Refresh.
- Public registration.
- Primary administrator registration.

Limits are backed by Redis and configured through `APP_REDIS_RATE_LIMIT_*` properties.

## Operational Hardening

For production:

- Use `SPRING_PROFILES_ACTIVE=prod`.
- Disable schema bootstrap unless running a controlled initialization.
- Provision Kafka topics before starting the relay.
- Use TLS at the ingress or service mesh boundary.
- Restrict PostgreSQL, Redis and Kafka network access to trusted workloads.
- Monitor outbox retry count, failed events, login failures, blocked accounts and 429 responses.
- Keep Swagger/OpenAPI and the dashboard behind trusted access controls if exposed outside an internal network.

## Security-Sensitive Events

The service emits and persists security-relevant facts such as authentication failures, session openings, session refreshes, session revocations, account blocks and access changes. Event payloads must not include passwords, password hashes, raw tokens or refresh tokens.
