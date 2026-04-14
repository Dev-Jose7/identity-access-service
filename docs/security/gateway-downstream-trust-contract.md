# Gateway to Downstream Trust Contract

This document defines the distributed JWT trust model for ArkaB2B.

## 1. Identity-access guarantees

`identity-access-service` guarantees:

- JWT is signed with `RS256`.
- JWT header includes `kid`.
- `GET /.well-known/jwks.json` publishes verification public keys for active and rollover keys.
- Access token includes authorization snapshot claims:
  - `email`
  - `roles` (role codes without `ROLE_` prefix)
  - `permissions` (permission codes)

## 2. Gateway mandatory validation

Before routing to downstream services, API Gateway must validate:

- JWS signature using JWKS public key selected by token `kid`.
- `alg == RS256`.
- `kid` exists and resolves to a trusted key.
- `iss` matches configured issuer.
- `aud` contains configured audience.
- `exp` is valid (not expired).
- `iat` is valid (clock skew window).
- `jti` exists.
- `typ == access`.
- token has required claims (`sub`, `sid`, `email`, `roles`, `permissions`).

If any validation fails, request must be rejected.

## 3. Downstream service behavior

Downstream services consume an already validated access token from Gateway and rebuild `SecurityContext` from claims:

- `roles` -> `ROLE_<ROLE_CODE>` authorities
- `permissions` -> direct authorities

This keeps compatibility with Spring Security:

- `hasRole('ORG_OWNER')`
- `hasAuthority('iam.user.create')`

Defense-in-depth recommendation:

- Services handling high-risk actions should also validate JWT signature/claims locally using the same JWKS.

## 4. Required access-token claims

Required claims for downstream authorization:

- `iss`
- `aud`
- `sub`
- `sid`
- `email`
- `jti`
- `iat`
- `exp`
- `typ` (`access`)
- `roles`
- `permissions`

## 5. Rotation model

Supported key lifecycle:

1. One active signing key (`APP_SECURITY_JWT_KEY_ID` + private key path).
2. One or more rollover verification keys (`APP_SECURITY_JWT_ADDITIONAL_PUBLIC_KEYS`).
3. JWKS exposes all verification keys.
4. Validators must select key by `kid`.

This model allows non-breaking key rotation without fallback to symmetric signing.
