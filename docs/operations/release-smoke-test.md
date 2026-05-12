# Release Smoke Test

Use this smoke test against a clean environment before publishing a release candidate.

## Automated Script

```bash
BASE_URL=http://localhost:8080 scripts/smoke/identity-access-release-smoke.sh
```

## Coverage

The script verifies:

- `GET /.well-known/jwks.json`
- `POST /api/v1/auth/register-primary`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/introspect`
- `POST /api/v1/admin/iam/accounts`
- `GET /api/v1/admin/iam/accounts/{accountId}/permissions`

## Assumption

`register-primary` is intended for a clean environment. If the primary account already exists, use an existing `SYSTEM_ADMIN` account to run the administrative checks manually.
