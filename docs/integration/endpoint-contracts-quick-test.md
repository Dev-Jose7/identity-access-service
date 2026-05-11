# Contratos de Endpoints Para Pruebas Rápidas

Este documento lista los contratos HTTP del servicio en un orden práctico de ejecución. La idea es que puedas probar un ambiente local limpio justo después de levantar el servicio.

## Índice

- [0. Variables Comunes](#0-variables-comunes)
- [0.1 Consola Web Operativa](#01-consola-web-operativa)
- [1. Health Check](#1-health-check)
- [2. JWKS](#2-jwks)
- [3. Registro de Cuenta Primaria](#3-registro-de-cuenta-primaria)
- [4. Login de la Cuenta Primaria](#4-login-de-la-cuenta-primaria)
- [5. Introspección del Access Token](#5-introspeccion-del-access-token)
- [6. Registro Público de Cuenta Estándar](#6-registro-publico-de-cuenta-estandar)
- [7. Login de Cuenta Pública](#7-login-de-cuenta-publica)
- [8. Refresh de Sesión](#8-refresh-de-sesion)
- [9. Logout de Sesión Actual](#9-logout-de-sesion-actual)
- [10. Crear Cuenta Como Administrador](#10-crear-cuenta-como-administrador)
- [11. Listar Cuentas](#11-listar-cuentas)
- [12. Asignar Rol a una Cuenta](#12-asignar-rol-a-una-cuenta)
- [13. Consultar Permisos Efectivos de una Cuenta](#13-consultar-permisos-efectivos-de-una-cuenta)
- [14. Listar Sesiones](#14-listar-sesiones)
- [15. Bloquear Cuenta](#15-bloquear-cuenta)
- [15.1 Desbloquear Cuenta](#151-desbloquear-cuenta)
- [16. Revocar Sesiones de una Cuenta](#16-revocar-sesiones-de-una-cuenta)
- [17. Listar Roles](#17-listar-roles)
- [18. Crear Rol](#18-crear-rol)
- [19. Actualizar Rol](#19-actualizar-rol)
- [20. Listar Permisos](#20-listar-permisos)
- [21. Crear Permiso](#21-crear-permiso)
- [22. Actualizar Permiso](#22-actualizar-permiso)
- [23. Conceder Permiso a un Rol](#23-conceder-permiso-a-un-rol)
- [24. Consultar Permisos de un Rol](#24-consultar-permisos-de-un-rol)
- [25. Revocar Permiso de un Rol](#25-revocar-permiso-de-un-rol)
- [26. Deshabilitar Permiso](#26-deshabilitar-permiso)
- [27. Deshabilitar Rol](#27-deshabilitar-rol)
- [28. Formatos Comunes de Error](#28-formatos-comunes-de-error)
- [29. Checklist Mínimo de Flujo Feliz](#29-checklist-minimo-de-flujo-feliz)


## Supuestos

- URL base: `http://localhost:8080`
- Herramienta JSON: `jq`
- La base de datos fue inicializada con `01-schema.sql` y `02-seed-roles.sql`.
- `SYSTEM_ADMIN` es exclusivo. Solo el primer registro primario debe funcionar.
- Las entradas protegidas del catálogo, por ejemplo `SYSTEM_ADMIN`, no pueden mutarse libremente desde endpoints administrativos.

<a id="0-variables-comunes"></a>
## 0. Variables Comunes

```bash
BASE_URL="http://localhost:8080"
PRIMARY_EMAIL="primary.admin@example.com"
PRIMARY_PASSWORD="ChangeMe123!"
PUBLIC_EMAIL="public.user@example.com"
PUBLIC_PASSWORD="ChangeMe123!"
ADMIN_CREATED_EMAIL="admin.created@example.com"
ADMIN_CREATED_PASSWORD="ChangeMe123!"
```

Usa este header para endpoints protegidos después de hacer login:

```bash
-H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN"
```

<a id="01-consola-web-operativa"></a>
## 0.1 Consola Web Operativa

Además de Swagger, el servicio expone un dashboard administrativo permission-aware:

```text
http://localhost:8080/console/index.html
```

La consola permite iniciar sesión, consultar el token efectivo, listar cuentas, sesiones, roles y permisos, y ejecutar acciones administrativas según las authorities presentes en el access token. El backend sigue aplicando la autorización real.

<a id="1-health-check"></a>
## 1. Health Check

Endpoint:

```http
GET /actuator/health
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Nota: `GET /actuator/health` pertenece a Actuator y normalmente no se publica como operación OpenAPI propia.

Prueba rápida:

```bash
curl -s "$BASE_URL/actuator/health" | jq
```

Forma esperada:

```json
{
  "status": "UP"
}
```

<a id="2-jwks"></a>
## 2. JWKS

Endpoint:

```http
GET /.well-known/jwks.json
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/jwks-http-controller/jwks
```

Prueba rápida:

```bash
curl -s "$BASE_URL/.well-known/jwks.json" | jq
```

Forma esperada:

```json
{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "use": "sig",
      "kid": "dev-rsa-key-1",
      "alg": "RS256",
      "n": "..."
    }
  ]
}
```

<a id="3-registro-de-cuenta-primaria"></a>
## 3. Registro de Cuenta Primaria

Esta es la primera llamada de negocio en un ambiente limpio. Crea la cuenta administrativa primaria exclusiva con rol `SYSTEM_ADMIN`.

Endpoint:

```http
POST /api/v1/auth/register-primary
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/auth-http-controller/registerPrimary
```

Cuerpo de solicitud:

```json
{
  "email": "primary.admin@example.com",
  "password": "ChangeMe123!"
}
```

Prueba rápida:

```bash
PRIMARY_REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/register-primary" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$PRIMARY_EMAIL\",\"password\":\"$PRIMARY_PASSWORD\"}")

echo "$PRIMARY_REGISTER_RESPONSE" | jq
PRIMARY_USER_ID=$(echo "$PRIMARY_REGISTER_RESPONSE" | jq -r '.userId')
```

Respuesta esperada:

```json
{
  "userId": "uuid",
  "email": "primary.admin@example.com",
  "status": "ACTIVE"
}
```

Conflicto esperado cuando la cuenta primaria ya existe:

```json
{
  "code": "primary_account_already_exists",
  "message": "Primary account already exists"
}
```

<a id="4-login-de-la-cuenta-primaria"></a>
## 4. Login de la Cuenta Primaria

Endpoint:

```http
POST /api/v1/auth/login
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/auth-http-controller/login
```

Cuerpo de solicitud:

```json
{
  "email": "primary.admin@example.com",
  "password": "ChangeMe123!"
}
```

Prueba rápida:

```bash
PRIMARY_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$PRIMARY_EMAIL\",\"password\":\"$PRIMARY_PASSWORD\"}")

echo "$PRIMARY_LOGIN_RESPONSE" | jq
PRIMARY_ACCESS_TOKEN=$(echo "$PRIMARY_LOGIN_RESPONSE" | jq -r '.accessToken')
PRIMARY_REFRESH_TOKEN=$(echo "$PRIMARY_LOGIN_RESPONSE" | jq -r '.refreshToken')
PRIMARY_SESSION_ID=$(echo "$PRIMARY_LOGIN_RESPONSE" | jq -r '.sessionId')
```

Respuesta esperada:

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "sessionId": "uuid",
  "tokenType": "Bearer",
  "accessTokenExpiresAtEpochSecond": 1760000000,
  "refreshTokenExpiresAtEpochSecond": 1760000000
}
```

<a id="5-introspeccion-del-access-token"></a>
## 5. Introspección del Access Token

Este endpoint es público. Valida tokens emitidos por el servicio y responde si siguen activos desde la perspectiva IAM.

Endpoint:

```http
POST /api/v1/auth/introspect
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/token-introspect-controller/introspect
```

Cuerpo de solicitud:

```json
{
  "token": "jwt"
}
```

Prueba rápida:

```bash
curl -s -X POST "$BASE_URL/api/v1/auth/introspect" \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$PRIMARY_ACCESS_TOKEN\"}" | jq
```

Respuesta activa esperada:

```json
{
  "active": true,
  "inactiveReason": null,
  "sub": "user-id",
  "sid": "session-id",
  "typ": "access",
  "iss": "identity-access-service",
  "aud": ["identity-access-client"],
  "iat": 1760000000,
  "exp": 1760000000,
  "jti": "jwt-id",
  "email": "primary.admin@example.com",
  "roles": ["SYSTEM_ADMIN"],
  "permissions": ["iam.account.create"]
}
```

Forma esperada para token inactivo:

```json
{
  "active": false,
  "inactiveReason": "reason",
  "sub": null,
  "sid": null,
  "typ": null,
  "iss": null,
  "aud": [],
  "iat": null,
  "exp": null,
  "jti": null,
  "email": null,
  "roles": [],
  "permissions": []
}
```

<a id="6-registro-publico-de-cuenta-estandar"></a>
## 6. Registro Público de Cuenta Estándar

Crea una cuenta pública no administrativa. Rol por defecto: `ACCOUNT_USER`.

Endpoint:

```http
POST /api/v1/auth/register
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/auth-http-controller/register
```

Cuerpo de solicitud:

```json
{
  "email": "public.user@example.com",
  "password": "ChangeMe123!"
}
```

Prueba rápida:

```bash
PUBLIC_REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$PUBLIC_EMAIL\",\"password\":\"$PUBLIC_PASSWORD\"}")

echo "$PUBLIC_REGISTER_RESPONSE" | jq
PUBLIC_USER_ID=$(echo "$PUBLIC_REGISTER_RESPONSE" | jq -r '.userId')
```

Respuesta esperada:

```json
{
  "userId": "uuid",
  "email": "public.user@example.com",
  "status": "ACTIVE"
}
```

<a id="7-login-de-cuenta-publica"></a>
## 7. Login de Cuenta Pública

Endpoint:

```http
POST /api/v1/auth/login
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/auth-http-controller/login
```

Cuerpo de solicitud:

```json
{
  "email": "public.user@example.com",
  "password": "ChangeMe123!"
}
```

Prueba rápida:

```bash
PUBLIC_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$PUBLIC_EMAIL\",\"password\":\"$PUBLIC_PASSWORD\"}")

echo "$PUBLIC_LOGIN_RESPONSE" | jq
PUBLIC_ACCESS_TOKEN=$(echo "$PUBLIC_LOGIN_RESPONSE" | jq -r '.accessToken')
PUBLIC_REFRESH_TOKEN=$(echo "$PUBLIC_LOGIN_RESPONSE" | jq -r '.refreshToken')
PUBLIC_SESSION_ID=$(echo "$PUBLIC_LOGIN_RESPONSE" | jq -r '.sessionId')
```

<a id="8-refresh-de-sesion"></a>
## 8. Refresh de Sesión

Endpoint:

```http
POST /api/v1/auth/refresh
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/auth-http-controller/refresh
```

Cuerpo de solicitud:

```json
{
  "refreshToken": "jwt"
}
```

Prueba rápida:

```bash
PUBLIC_REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$PUBLIC_REFRESH_TOKEN\"}")

echo "$PUBLIC_REFRESH_RESPONSE" | jq
PUBLIC_ACCESS_TOKEN=$(echo "$PUBLIC_REFRESH_RESPONSE" | jq -r '.accessToken')
PUBLIC_REFRESH_TOKEN=$(echo "$PUBLIC_REFRESH_RESPONSE" | jq -r '.refreshToken')
PUBLIC_SESSION_ID=$(echo "$PUBLIC_REFRESH_RESPONSE" | jq -r '.sessionId')
```

Respuesta esperada:

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "sessionId": "uuid",
  "tokenType": "Bearer",
  "accessTokenExpiresAt": "2026-05-10T00:00:00Z",
  "refreshTokenExpiresAt": "2026-05-17T00:00:00Z"
}
```

<a id="9-logout-de-sesion-actual"></a>
## 9. Logout de Sesión Actual

`logout` requiere autenticación y usa la sesión del bearer token. No requiere cuerpo de solicitud.

Endpoint:

```http
POST /api/v1/auth/logout
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/auth-http-controller/logout
```

Prueba rápida:

```bash
curl -s -X POST "$BASE_URL/api/v1/auth/logout" \
  -H "Authorization: Bearer $PUBLIC_ACCESS_TOKEN" | jq
```

Respuesta esperada:

```json
{
  "sessionId": "uuid",
  "status": "REVOKED"
}
```

<a id="10-crear-cuenta-como-administrador"></a>
## 10. Crear Cuenta Como Administrador

Permiso requerido: `iam.account.create`.

Endpoint:

```http
POST /api/v1/admin/iam/accounts
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/admin-iam-http-controller/createAccount
```

Cuerpo de solicitud con rol explícito:

```json
{
  "email": "admin.created@example.com",
  "password": "ChangeMe123!",
  "roleCode": "ACCESS_MANAGER"
}
```

Cuerpo de solicitud con rol por defecto:

```json
{
  "email": "admin.created@example.com",
  "password": "ChangeMe123!"
}
```

Prueba rápida:

```bash
ADMIN_CREATED_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/admin/iam/accounts" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_CREATED_EMAIL\",\"password\":\"$ADMIN_CREATED_PASSWORD\",\"roleCode\":\"ACCESS_MANAGER\"}")

echo "$ADMIN_CREATED_RESPONSE" | jq
ADMIN_CREATED_USER_ID=$(echo "$ADMIN_CREATED_RESPONSE" | jq -r '.userId')
```

Respuesta esperada:

```json
{
  "userId": "uuid",
  "email": "admin.created@example.com",
  "status": "ACTIVE"
}
```

<a id="11-listar-cuentas"></a>
## 11. Listar Cuentas

Permiso requerido: `iam.account.read`.

Endpoint:

```http
GET /api/v1/admin/iam/accounts
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/admin-iam-http-controller/listAccounts
```

Prueba rápida:

```bash
curl -s "$BASE_URL/api/v1/admin/iam/accounts" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" | jq
```

Elemento esperado en la respuesta:

```json
{
  "userId": "uuid",
  "email": "primary.admin@example.com",
  "status": "ACTIVE",
  "failedLoginCount": 0,
  "createdAt": "2026-05-11T00:00:00Z",
  "updatedAt": "2026-05-11T00:00:00Z",
  "roles": ["SYSTEM_ADMIN"]
}
```

<a id="12-asignar-rol-a-una-cuenta"></a>
## 12. Asignar Rol a una Cuenta

Permiso requerido: `iam.access.assign-role`.

Endpoint:

```http
POST /api/v1/admin/iam/accounts/{userId}/roles
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/admin-iam-http-controller/assignRole
```

Cuerpo de solicitud:

```json
{
  "roleCode": "ACCOUNT_READONLY"
}
```

Prueba rápida:

```bash
curl -s -X POST "$BASE_URL/api/v1/admin/iam/accounts/$ADMIN_CREATED_USER_ID/roles" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"roleCode":"ACCOUNT_READONLY"}' | jq
```

Respuesta esperada:

```json
{
  "userId": "uuid",
  "roleCode": "ACCOUNT_READONLY",
  "assigned": true,
  "status": "ACTIVE"
}
```

<a id="13-consultar-permisos-efectivos-de-una-cuenta"></a>
## 13. Consultar Permisos Efectivos de una Cuenta

Permiso requerido: `iam.permission.read`.

Endpoint:

```http
GET /api/v1/admin/iam/accounts/{userId}/permissions
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/admin-iam-http-controller/getUserPermissions
```

Prueba rápida:

```bash
curl -s "$BASE_URL/api/v1/admin/iam/accounts/$ADMIN_CREATED_USER_ID/permissions" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" | jq
```

Respuesta esperada:

```json
{
  "userId": "uuid",
  "roles": ["ACCESS_MANAGER", "ACCOUNT_READONLY"],
  "permissions": ["iam.permission.read", "iam.role.read"]
}
```

<a id="14-listar-sesiones"></a>
## 14. Listar Sesiones

Permiso requerido: `iam.account.read`.

Nota: antes de listar, el servicio marca como `EXPIRED` las sesiones `ACTIVE` cuyo refresh token ya venció. La expiración del access token no revoca la sesión por sí sola.

Endpoint:

```http
GET /api/v1/admin/iam/sessions
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/session-admin-http-controller/listSessions
```

Prueba rápida:

```bash
curl -s "$BASE_URL/api/v1/admin/iam/sessions" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" | jq
```

Elemento esperado en la respuesta:

```json
{
  "sessionId": "uuid",
  "userId": "uuid",
  "status": "ACTIVE",
  "ipAddress": "127.0.0.1",
  "deviceId": null,
  "deviceName": null,
  "deviceType": null,
  "issuedAt": "2026-05-11T00:00:00Z",
  "accessTokenExpiresAt": "2026-05-11T00:15:00Z",
  "refreshTokenExpiresAt": "2026-05-18T00:00:00Z",
  "lastSeenAt": "2026-05-11T00:00:00Z",
  "revokedAt": null,
  "revocationReason": null,
  "createdAt": "2026-05-11T00:00:00Z",
  "updatedAt": "2026-05-11T00:00:00Z"
}
```

<a id="15-bloquear-cuenta"></a>
## 15. Bloquear Cuenta

Permiso requerido: `iam.account.block`.

Nota: al bloquear una cuenta, el servicio revoca sus sesiones activas para impedir acceso posterior con tokens vigentes.

Endpoint:

```http
POST /api/v1/admin/iam/accounts/{userId}/block
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/admin-iam-http-controller/blockUser
```

Cuerpo de solicitud:

```json
{
  "reason": "Security review"
}
```

Prueba rápida:

```bash
curl -s -X POST "$BASE_URL/api/v1/admin/iam/accounts/$ADMIN_CREATED_USER_ID/block" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Security review"}' | jq
```

Respuesta esperada:

```json
{
  "userId": "uuid",
  "status": "BLOCKED",
  "changed": true
}
```

<a id="151-desbloquear-cuenta"></a>
## 15.1 Desbloquear Cuenta

Permiso requerido: `iam.account.unblock`.

Endpoint:

```http
POST /api/v1/admin/iam/accounts/{userId}/unblock
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/admin-iam-http-controller/unblockUser
```

Cuerpo de solicitud:

```json
{
  "reason": "Administrative unblock"
}
```

Prueba rápida:

```bash
curl -s -X POST "$BASE_URL/api/v1/admin/iam/accounts/$ADMIN_CREATED_USER_ID/unblock" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Administrative unblock"}' | jq
```

Respuesta esperada:

```json
{
  "userId": "uuid",
  "status": "ACTIVE",
  "changed": true
}
```

<a id="16-revocar-sesiones-de-una-cuenta"></a>
## 16. Revocar Sesiones de una Cuenta

Permiso requerido: `iam.session.revoke`.

Endpoint:

```http
POST /api/v1/admin/iam/accounts/{userId}/sessions/revoke
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/session-admin-http-controller/revokeSessions
```

Cuerpo de solicitud:

```json
{
  "reason": "Administrative revocation"
}
```

Prueba rápida:

```bash
curl -s -X POST "$BASE_URL/api/v1/admin/iam/accounts/$ADMIN_CREATED_USER_ID/sessions/revoke" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Administrative revocation"}' | jq
```

Respuesta esperada:

```json
{
  "userId": "uuid",
  "revokedSessions": 1,
  "reason": "Administrative revocation",
  "status": "REVOKED"
}
```

<a id="17-listar-roles"></a>
## 17. Listar Roles

Permiso requerido: `iam.role.read`.

Endpoint:

```http
GET /api/v1/admin/iam/roles
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/listRoles
```

Prueba rápida:

```bash
curl -s "$BASE_URL/api/v1/admin/iam/roles" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" | jq
```

Elemento esperado en la respuesta:

```json
{
  "roleId": "uuid",
  "roleCode": "SYSTEM_ADMIN",
  "description": "System administrator role",
  "status": "ACTIVE",
  "protectedRole": true
}
```

<a id="18-crear-rol"></a>
## 18. Crear Rol

Permiso requerido: `iam.role.create`.

Endpoint:

```http
POST /api/v1/admin/iam/roles
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/createRole
```

Cuerpo de solicitud:

```json
{
  "roleCode": "SUPPORT_AGENT",
  "description": "Support agent role",
  "protectedRole": false
}
```

Prueba rápida:

```bash
CREATE_ROLE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/admin/iam/roles" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"roleCode":"SUPPORT_AGENT","description":"Support agent role","protectedRole":false}')

echo "$CREATE_ROLE_RESPONSE" | jq
SUPPORT_AGENT_ROLE_ID=$(echo "$CREATE_ROLE_RESPONSE" | jq -r '.roleId')
```

Respuesta esperada:

```json
{
  "roleId": "uuid",
  "roleCode": "SUPPORT_AGENT",
  "description": "Support agent role",
  "status": "ACTIVE",
  "protectedRole": false
}
```

<a id="19-actualizar-rol"></a>
## 19. Actualizar Rol

Permiso requerido: `iam.role.update`.

Endpoint:

```http
PATCH /api/v1/admin/iam/roles/{roleId}
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/updateRole
```

Cuerpo de solicitud:

```json
{
  "description": "Updated support agent role"
}
```

Prueba rápida:

```bash
curl -s -X PATCH "$BASE_URL/api/v1/admin/iam/roles/$SUPPORT_AGENT_ROLE_ID" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description":"Updated support agent role"}' | jq
```

<a id="20-listar-permisos"></a>
## 20. Listar Permisos

Permiso requerido: `iam.permission.read`.

Endpoint:

```http
GET /api/v1/admin/iam/permissions
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/listPermissions
```

Prueba rápida:

```bash
curl -s "$BASE_URL/api/v1/admin/iam/permissions" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" | jq
```

Elemento esperado en la respuesta:

```json
{
  "permissionId": "uuid",
  "permissionCode": "iam.account.create",
  "resource": "iam.account",
  "action": "create",
  "scope": "GLOBAL",
  "description": "iam account create",
  "status": "ACTIVE",
  "systemPermission": true
}
```

<a id="21-crear-permiso"></a>
## 21. Crear Permiso

Permiso requerido: `iam.permission.create`.

Endpoint:

```http
POST /api/v1/admin/iam/permissions
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/createPermission
```

Cuerpo de solicitud:

```json
{
  "permissionCode": "support.ticket.read",
  "resource": "support.ticket",
  "action": "read",
  "scope": "GLOBAL",
  "description": "Read support tickets",
  "systemPermission": false
}
```

Prueba rápida:

```bash
CREATE_PERMISSION_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/admin/iam/permissions" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"permissionCode":"support.ticket.read","resource":"support.ticket","action":"read","scope":"GLOBAL","description":"Read support tickets","systemPermission":false}')

echo "$CREATE_PERMISSION_RESPONSE" | jq
SUPPORT_TICKET_PERMISSION_ID=$(echo "$CREATE_PERMISSION_RESPONSE" | jq -r '.permissionId')
```

Respuesta esperada:

```json
{
  "permissionId": "uuid",
  "permissionCode": "support.ticket.read",
  "resource": "support.ticket",
  "action": "read",
  "scope": "GLOBAL",
  "description": "Read support tickets",
  "status": "ACTIVE",
  "systemPermission": false
}
```

<a id="22-actualizar-permiso"></a>
## 22. Actualizar Permiso

Permiso requerido: `iam.permission.update`.

Endpoint:

```http
PATCH /api/v1/admin/iam/permissions/{permissionId}
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/updatePermission
```

Cuerpo de solicitud:

```json
{
  "resource": "support.ticket",
  "action": "read",
  "scope": "GLOBAL",
  "description": "Read support ticket records"
}
```

Prueba rápida:

```bash
curl -s -X PATCH "$BASE_URL/api/v1/admin/iam/permissions/$SUPPORT_TICKET_PERMISSION_ID" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"resource":"support.ticket","action":"read","scope":"GLOBAL","description":"Read support ticket records"}' | jq
```

<a id="23-conceder-permiso-a-un-rol"></a>
## 23. Conceder Permiso a un Rol

Permiso requerido: `iam.role.update`.

Endpoint:

```http
POST /api/v1/admin/iam/roles/{roleId}/permissions
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/grantPermissionToRole
```

Cuerpo de solicitud:

```json
{
  "permissionCode": "support.ticket.read"
}
```

Prueba rápida:

```bash
curl -s -X POST "$BASE_URL/api/v1/admin/iam/roles/$SUPPORT_AGENT_ROLE_ID/permissions" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"permissionCode":"support.ticket.read"}' | jq
```

Respuesta esperada:

```json
{
  "roleId": "uuid",
  "permissionCode": "support.ticket.read",
  "changed": true,
  "status": "ACTIVE"
}
```

<a id="24-consultar-permisos-de-un-rol"></a>
## 24. Consultar Permisos de un Rol

Permiso requerido: `iam.permission.read`.

Endpoint:

```http
GET /api/v1/admin/iam/roles/{roleId}/permissions
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/listPermissionsByRoleId
```

Prueba rápida:

```bash
curl -s "$BASE_URL/api/v1/admin/iam/roles/$SUPPORT_AGENT_ROLE_ID/permissions" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" | jq
```

Elemento esperado en la respuesta:

```json
{
  "permissionId": "uuid",
  "permissionCode": "support.ticket.read",
  "resource": "support.ticket",
  "action": "read",
  "scope": "GLOBAL",
  "description": "Read support tickets",
  "status": "ACTIVE",
  "systemPermission": false
}
```

<a id="25-revocar-permiso-de-un-rol"></a>
## 25. Revocar Permiso de un Rol

Permiso requerido: `iam.role.update`.

Endpoint:

```http
DELETE /api/v1/admin/iam/roles/{roleId}/permissions/{permissionCode}
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/revokePermissionFromRole
```

Prueba rápida:

```bash
curl -s -X DELETE "$BASE_URL/api/v1/admin/iam/roles/$SUPPORT_AGENT_ROLE_ID/permissions/support.ticket.read" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" | jq
```

Respuesta esperada:

```json
{
  "roleId": "uuid",
  "permissionCode": "support.ticket.read",
  "changed": true,
  "status": "ACTIVE"
}
```

<a id="26-deshabilitar-permiso"></a>
## 26. Deshabilitar Permiso

Permiso requerido: `iam.permission.update`.

Endpoint:

```http
POST /api/v1/admin/iam/permissions/{permissionId}/disable
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/disablePermission
```

Prueba rápida:

```bash
curl -s -X POST "$BASE_URL/api/v1/admin/iam/permissions/$SUPPORT_TICKET_PERMISSION_ID/disable" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" | jq
```

Respuesta esperada:

```json
{
  "permissionId": "uuid",
  "permissionCode": "support.ticket.read",
  "resource": "support.ticket",
  "action": "read",
  "scope": "GLOBAL",
  "description": "Read support ticket records",
  "status": "DISABLED",
  "systemPermission": false
}
```

<a id="27-deshabilitar-rol"></a>
## 27. Deshabilitar Rol

Permiso requerido: `iam.role.update`.

Endpoint:

```http
POST /api/v1/admin/iam/roles/{roleId}/disable
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html#/access-catalog-http-controller/disableRole
```

Prueba rápida:

```bash
curl -s -X POST "$BASE_URL/api/v1/admin/iam/roles/$SUPPORT_AGENT_ROLE_ID/disable" \
  -H "Authorization: Bearer $PRIMARY_ACCESS_TOKEN" | jq
```

Respuesta esperada:

```json
{
  "roleId": "uuid",
  "roleCode": "SUPPORT_AGENT",
  "description": "Updated support agent role",
  "status": "DISABLED",
  "protectedRole": false
}
```

<a id="28-formatos-comunes-de-error"></a>
## 28. Formatos Comunes de Error

Los errores de validación o dominio usan esta forma:

```json
{
  "code": "error_code",
  "message": "Human-readable message"
}
```

Los errores de autenticación normalmente devuelven `401`:

```json
{
  "code": "unauthorized",
  "message": "Authentication is required"
}
```

Los errores de autorización normalmente devuelven `403`:

```json
{
  "code": "forbidden",
  "message": "Access denied"
}
```

Los errores por rate limit devuelven `429`:

```json
{
  "code": "rate_limit_exceeded",
  "message": "Rate limit exceeded"
}
```

<a id="29-checklist-minimo-de-flujo-feliz"></a>
## 29. Checklist Mínimo de Flujo Feliz

1. `GET /actuator/health`
2. `GET /.well-known/jwks.json`
3. `POST /api/v1/auth/register-primary`
4. `POST /api/v1/auth/login` con la cuenta primaria
5. `POST /api/v1/auth/introspect` con el access token primario
6. `POST /api/v1/auth/register` para una cuenta normal
7. `POST /api/v1/auth/login` con la cuenta normal
8. `POST /api/v1/auth/refresh` con el refresh token normal
9. `POST /api/v1/auth/logout` con el access token normal
10. `POST /api/v1/admin/iam/accounts` con el access token primario
11. `GET /api/v1/admin/iam/accounts`
12. `POST /api/v1/admin/iam/accounts/{userId}/roles`
13. `GET /api/v1/admin/iam/accounts/{userId}/permissions`
14. `GET /api/v1/admin/iam/sessions`
15. `POST /api/v1/admin/iam/accounts/{userId}/sessions/revoke`
16. `GET /api/v1/admin/iam/roles`
17. `POST /api/v1/admin/iam/roles`
18. `POST /api/v1/admin/iam/permissions`
19. `POST /api/v1/admin/iam/roles/{roleId}/permissions`
20. `GET /api/v1/admin/iam/roles/{roleId}/permissions`
21. `GET /api/v1/admin/iam/permissions`
