# identity-access-service

## ES

### Descripción

`identity-access-service` es un microservicio IAM autoalojado para sistemas que necesitan autenticación, sesiones, JWT/JWKS, autorización basada en roles/permisos, catálogo RBAC dinámico, auditoría técnica, rate limiting e integración por outbox.

El servicio está diseñado para integrarse como servicio interno o dependencia desplegable dentro de una plataforma. No está diseñado como SaaS hospedado.

Guía de integración para sistemas consumidores:

- [docs/integration/consumer-integration-guide.md](docs/integration/consumer-integration-guide.md)
- [docs/integration/endpoint-contracts-quick-test.md](docs/integration/endpoint-contracts-quick-test.md)

Documentación de gobierno, seguridad y operación:

- [CHANGELOG.md](CHANGELOG.md)
- [LICENSE](LICENSE)
- [SECURITY.md](SECURITY.md)
- [CONTRIBUTING.md](CONTRIBUTING.md)
- [docs/architecture/architecture.md](docs/architecture/architecture.md)
- [docs/security/threat-model.md](docs/security/threat-model.md)
- [docs/operations/deployment.md](docs/operations/deployment.md)
- [docs/operations/release-checklist.md](docs/operations/release-checklist.md)

### Licencia

Este proyecto se distribuye bajo Elastic License 2.0. Esta licencia permite usar, copiar, distribuir y modificar el software, pero restringe ofrecerlo a terceros como servicio hospedado o gestionado que exponga una parte sustancial de su funcionalidad.

Consulta el texto completo en [LICENSE](LICENSE).

### Capacidades principales

- Registro público controlado de cuentas.
- Creación administrativa de cuentas.
- Login con email/password y apertura de sesión.
- Refresh token con rotación de tokens.
- Logout y revocación de sesiones.
- Introspección de tokens emitidos por el servicio.
- Firma JWT con RSA/RS256.
- Publicación JWKS en `/.well-known/jwks.json`.
- Roles y permisos dinámicos persistidos en PostgreSQL.
- Authorities resueltas desde `user_role_assignment`, `role`, `permission` y `role_permission`.
- Endpoints administrativos para cuentas, roles, permisos, asignación de roles, bloqueo y revocación de sesiones.
- Rate limiting con Redis para endpoints públicos sensibles.
- Auditoría técnica de seguridad.
- Outbox persistido y relay hacia Kafka.

### Requisitos

Para ejecutar en modo local con la aplicación en host:

- Java 21.
- Docker Desktop o Docker daemon activo.
- Gradle wrapper incluido (`./gradlew`).
- Puertos disponibles: `8080`, `5432`, `6379`, `9092`.

Para ejecutar todo en Docker Compose:

- Docker Desktop o Docker daemon activo.
- Docker Compose v2.
- Puertos disponibles: `8080`, `5432`, `6379`, `9092`.

Infraestructura usada por el servicio:

- PostgreSQL 15.
- Redis 7 con password.
- Apache Kafka 3.8.0.

### Política de configuración

- `.env` es la fuente local principal de valores.
- `.env.example` contiene la plantilla completa para desarrollo.
- `docker-compose.yml` es la fuente de verdad para infraestructura local.
- `application.yml`, `application-local.yml` y `application-docker.yml` definen cómo se consumen las propiedades.
- `APP_VERSION` controla la versión publicada en OpenAPI.
- En `local` y `docker`, el bootstrap de schema está habilitado por defecto.
- En `prod`, el bootstrap de schema queda deshabilitado salvo que `APP_DATABASE_SCHEMA_INITIALIZE_ON_STARTUP=true` se configure explícitamente.
- Las llaves RSA de desarrollo no deben usarse en producción.

### Perfiles

- `local`: la aplicación corre en el host y PostgreSQL/Redis/Kafka corren en Docker Compose.
- `docker`: la aplicación y sus dependencias corren en Docker Compose.
- `prod`: entorno externalizado; requiere secretos y endpoints reales.

### Configuración rápida

Crea el archivo `.env`:

```bash
cp .env.example .env
```

Si vas a correr la aplicación desde el host, carga las variables:

```bash
set -a
source .env
set +a
```

### Levantar solo infraestructura local

Usa este modo cuando quieras correr la aplicación con Gradle en tu máquina:

```bash
docker compose up -d postgres redis kafka
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun --no-daemon
```

Verifica salud del servicio:

```bash
curl -s http://localhost:8080/actuator/health | jq
```

Verifica JWKS:

```bash
curl -s http://localhost:8080/.well-known/jwks.json | jq
```

### Levantar todo con Docker Compose

Usa este modo cuando quieras correr servicio e infraestructura en contenedores:

```bash
cp .env.example .env
docker compose up -d --build
```

Ver logs del servicio:

```bash
docker compose logs -f identity-access-service
```

Apagar contenedores sin borrar datos:

```bash
docker compose down
```

Recrear desde cero borrando volúmenes y datos locales:

```bash
docker compose down -v
docker compose up -d --build
```

### Base de datos y schema

PostgreSQL carga los scripts iniciales desde la carpeta:

```text
src/main/resources/db/init/
```

Cuando se levanta `postgres` con Docker Compose, la carpeta se monta en:

```text
/docker-entrypoint-initdb.d/
```

PostgreSQL ejecuta los archivos `.sql` en orden alfabético:

```text
01-schema.sql
02-seed-roles.sql
```

Si ya existe un volumen anterior de PostgreSQL, Docker no vuelve a ejecutar scripts de inicialización. Para forzar una base limpia:

```bash
docker compose down -v
docker compose up -d postgres redis kafka
```

El schema incluye, entre otras, estas tablas operativas:

- `user_account`
- `credential`
- `credential_password`
- `user_login_attempt`
- `user_session`
- `role`
- `permission`
- `role_permission`
- `role_assignment_policy`
- `user_role_assignment`
- `auth_audit`
- `outbox_event`
- `processed_event`

### Llaves JWT y JWKS

El servicio firma tokens con RSA/RS256.

Llaves de desarrollo incluidas:

- `src/main/resources/keys/dev-private.pem`
- `src/main/resources/keys/dev-public.pem`
- `src/main/resources/keys/dev-rollover-public.pem`

Propiedades principales:

```env
APP_SECURITY_JWT_ALGORITHM=RS256
APP_SECURITY_JWT_KEY_ID=dev-rsa-key-1
APP_SECURITY_JWT_PRIVATE_KEY_PATH=classpath:keys/dev-private.pem
APP_SECURITY_JWT_PUBLIC_KEY_PATH=classpath:keys/dev-public.pem
APP_SECURITY_JWT_ADDITIONAL_PUBLIC_KEYS=
APP_SECURITY_JWT_ISSUER=identity-access-service
APP_SECURITY_JWT_AUDIENCE=identity-access-client
APP_SECURITY_JWT_ACCESS_TTL_SECONDS=900
APP_SECURITY_JWT_REFRESH_TTL_SECONDS=604800
APP_SECURITY_JWT_CLOCK_SKEW_SECONDS=60
APP_SECURITY_JWT_JWKS_PATH=/.well-known/jwks.json
```

`APP_SECURITY_JWT_PRIVATE_KEY_PATH` y `APP_SECURITY_JWT_PUBLIC_KEY_PATH` aceptan rutas `classpath:` o rutas de archivo accesibles por el proceso.

Formato para llaves públicas adicionales de verificación:

```env
APP_SECURITY_JWT_ADDITIONAL_PUBLIC_KEYS=rollover-rsa-key-0=classpath:keys/dev-rollover-public.pem
```

En producción debes montar o inyectar llaves reales desde un gestor de secretos o volumen seguro y cambiar `APP_SECURITY_JWT_KEY_ID`, `APP_SECURITY_JWT_PRIVATE_KEY_PATH` y `APP_SECURITY_JWT_PUBLIC_KEY_PATH`.

### Redis y rate limiting

Redis se usa para cache operativo y rate limiting.

Endpoints con rate limiting activo:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/register-primary`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`

Variables:

```env
APP_REDIS_CACHE_INTROSPECTION_TTL_SECONDS=30
APP_REDIS_CACHE_PERMISSIONS_TTL_SECONDS=30
APP_REDIS_RATE_LIMIT_LOGIN_RPM=10
APP_REDIS_RATE_LIMIT_REFRESH_RPM=30
APP_REDIS_RATE_LIMIT_REGISTRATION_RPM=5
```

Cuando se supera el límite, el servicio responde HTTP `429 Too Many Requests`.

### Kafka y outbox

Las mutaciones relevantes se guardan en `outbox_event` y el relay publica eventos hacia Kafka.
Los fallos de autenticación se publican como `AccountAuthenticationFailed` hacia `APP_KAFKA_TOPIC_AUTH_FAILED` sin incluir contraseñas, hashes ni tokens.

Variables principales:

```env
APP_OUTBOX_RELAY_ENABLED=true
APP_OUTBOX_RELAY_BATCH_SIZE=200
APP_OUTBOX_RELAY_POLL_INTERVAL_MS=2000
APP_OUTBOX_RELAY_MAX_RETRIES=3
APP_IDEMPOTENCY_CONSUMER_NAME=identity-access-service
```

Topics configurables:

```env
APP_KAFKA_TOPIC_SESSION_OPENED=iam.session-opened.v1
APP_KAFKA_TOPIC_ACCOUNT_REGISTERED=iam.account-registered.v1
APP_KAFKA_TOPIC_AUTH_FAILED=iam.auth-failed.v1
APP_KAFKA_TOPIC_SESSION_REFRESHED=iam.session-refreshed.v1
APP_KAFKA_TOPIC_SESSION_REVOKED=iam.session-revoked.v1
APP_KAFKA_TOPIC_ROLE_ASSIGNED_TO_ACCOUNT=iam.role-assigned-to-account.v1
APP_KAFKA_TOPIC_ACCOUNT_BLOCKED=iam.account-blocked.v1
APP_KAFKA_TOPIC_ACCOUNT_UNBLOCKED=iam.account-unblocked.v1
APP_KAFKA_TOPIC_ROLE_CREATED=iam.role-created.v1
APP_KAFKA_TOPIC_ROLE_UPDATED=iam.role-updated.v1
APP_KAFKA_TOPIC_ROLE_DISABLED=iam.role-disabled.v1
APP_KAFKA_TOPIC_PERMISSION_CREATED=iam.permission-created.v1
APP_KAFKA_TOPIC_PERMISSION_UPDATED=iam.permission-updated.v1
APP_KAFKA_TOPIC_PERMISSION_DISABLED=iam.permission-disabled.v1
APP_KAFKA_TOPIC_PERMISSION_GRANTED_TO_ROLE=iam.permission-granted-to-role.v1
APP_KAFKA_TOPIC_PERMISSION_REVOKED_FROM_ROLE=iam.permission-revoked-from-role.v1
```

En producción no dependas de `auto.create.topics.enable`. Crea los topics antes de habilitar el relay:

```bash
KAFKA_TOPICS_CREATE_MODE=cli \
KAFKA_BOOTSTRAP_SERVERS=<broker:9092> \
KAFKA_TOPIC_PARTITIONS=3 \
KAFKA_TOPIC_REPLICATION_FACTOR=3 \
scripts/operations/create-kafka-topics.sh
```

Con Docker Compose local:

```bash
KAFKA_TOPICS_CREATE_MODE=docker-compose scripts/operations/create-kafka-topics.sh
```

Expiración persistida de sesiones por vencimiento de refresh token:

```env
APP_SESSIONS_EXPIRATION_ENABLED=true
APP_SESSIONS_EXPIRATION_SWEEP_INTERVAL_MS=60000
```

Si no necesitas publicación de eventos en un entorno local puntual, puedes desactivar el relay:

```env
APP_OUTBOX_RELAY_ENABLED=false
```

### Registro y roles iniciales

Variables de registro:

```env
APP_IAM_REGISTRATION_PUBLIC_ENABLED=true
APP_IAM_REGISTRATION_PUBLIC_DEFAULT_ROLE_CODE=ACCOUNT_USER
APP_IAM_REGISTRATION_PRIMARY_ENABLED=true
APP_IAM_REGISTRATION_PRIMARY_INITIAL_ROLE_CODE=SYSTEM_ADMIN
APP_IAM_REGISTRATION_ADMIN_DEFAULT_ROLE_CODE=ACCOUNT_USER
APP_IAM_REGISTRATION_PROTECTED_ROLE_CODES=SYSTEM_ADMIN
```

Semántica por defecto:

- `POST /api/v1/auth/register-primary` crea la cuenta primaria/bootstrap con `SYSTEM_ADMIN`.
- `POST /api/v1/auth/register` crea cuentas públicas normales con `ACCOUNT_USER`.
- La creación administrativa de cuentas usa `ACCOUNT_USER` si no se especifica rol.
- `SYSTEM_ADMIN` es un rol protegido y de asignación exclusiva: solo puede existir una asignación activa.
- Los roles protegidos no se asignan por flujos administrativos normales salvo política explícita.
- Las authorities del principal autenticado se derivan de base de datos, no de listas hardcodeadas.

Roles semilla principales:

- `SYSTEM_ADMIN`
- `ACCESS_ADMIN`
- `ACCESS_MANAGER`
- `ACCOUNT_USER`
- `ACCOUNT_READONLY`

Permisos semilla principales:

- `iam.account.create`
- `iam.account.read`
- `iam.account.update`
- `iam.account.block`
- `iam.account.unblock`
- `iam.access.assign-role`
- `iam.access.revoke-role`
- `iam.session.read`
- `iam.session.revoke`
- `iam.permission.read`
- `iam.role.create`
- `iam.role.read`
- `iam.role.update`
- `iam.permission.create`
- `iam.permission.update`
- `iam.access-profile.read`

### Endpoints públicos

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/register-primary`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/introspect`
- `GET /.well-known/jwks.json`
- `GET /actuator/health`

`logout` requiere usuario autenticado.

### Endpoints administrativos

Cuentas:

- `GET /api/v1/admin/iam/accounts`
- `POST /api/v1/admin/iam/accounts`
- `POST /api/v1/admin/iam/accounts/{accountId}/roles`
- `POST /api/v1/admin/iam/accounts/{accountId}/block`
- `POST /api/v1/admin/iam/accounts/{accountId}/unblock`
- `GET /api/v1/admin/iam/accounts/{accountId}/permissions`
- `GET /api/v1/admin/iam/sessions`
- `POST /api/v1/admin/iam/accounts/{accountId}/sessions/revoke`

Roles y permisos:

- `GET /api/v1/admin/iam/roles`
- `POST /api/v1/admin/iam/roles`
- `PATCH /api/v1/admin/iam/roles/{roleId}`
- `POST /api/v1/admin/iam/roles/{roleId}/disable`
- `GET /api/v1/admin/iam/roles/{roleId}/permissions`
- `POST /api/v1/admin/iam/roles/{roleId}/permissions`
- `DELETE /api/v1/admin/iam/roles/{roleId}/permissions/{permissionCode}`
- `GET /api/v1/admin/iam/permissions`
- `POST /api/v1/admin/iam/permissions`
- `PATCH /api/v1/admin/iam/permissions/{permissionId}`
- `POST /api/v1/admin/iam/permissions/{permissionId}/disable`

### Swagger / OpenAPI

- Dashboard UI: `http://localhost:8080/console/index.html`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Uso con token:

1. Ejecuta login y copia `accessToken`.
2. Abre Swagger UI.
3. Presiona `Authorize`.
4. Ingresa el token como `Bearer <accessToken>` si la UI no agrega el prefijo automáticamente.
5. Ejecuta endpoints administrativos.

### Dashboard operativo

El servicio también expone una consola web estática en `http://localhost:8080/console/index.html`.
Esta UI permite iniciar sesión, inspeccionar tokens, listar cuentas, sesiones, roles y permisos, y ejecutar acciones administrativas según los permisos efectivos del access token.
La autorización real sigue viviendo en el backend; la consola solo oculta o deshabilita acciones cuando el token no contiene la authority requerida.

### Smoke test mínimo

Registrar cuenta inicial:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register-primary \
  -H 'Content-Type: application/json' \
  -d '{
    "email":"admin@example.test",
    "password":"Admin123!"
  }' | jq
```

También existe un smoke test automatizado para entornos limpios:

```bash
BASE_URL=http://localhost:8080 scripts/smoke/identity-access-release-smoke.sh
```

La recuperación break-glass de `SYSTEM_ADMIN` no se expone por HTTP. El procedimiento operativo está documentado en `docs/operations/break-glass-system-admin.md`.

Login:

```bash
TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -H 'User-Agent: curl' \
  -d '{
    "email":"admin@example.test",
    "password":"Admin123!"
  }')

echo "$TOKEN_RESPONSE" | jq
ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.accessToken')
REFRESH_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.refreshToken')
```

Introspect:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/introspect \
  -H 'Content-Type: application/json' \
  -d "{\"token\":\"$ACCESS_TOKEN\"}" | jq
```

Refresh:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/refresh \
  -H 'Content-Type: application/json' \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" | jq
```

Listar roles con Bearer token:

```bash
curl -s http://localhost:8080/api/v1/admin/iam/roles \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq
```

### Tests

Unit tests y slice tests:

```bash
./gradlew clean test --no-daemon
```

Integration tests con infraestructura real vía Testcontainers:

```bash
./gradlew integrationTest --no-daemon
```

Suite completa:

```bash
./gradlew clean test integrationTest --no-daemon
```

`integrationTest` requiere Docker daemon activo y valida:

- schema/seed PostgreSQL.
- adapters R2DBC.
- rate limiter Redis.
- persistencia outbox.
- publicación Kafka del relay outbox.

### Producción

Para producción debes externalizar como mínimo:

- PostgreSQL R2DBC URL, usuario y password.
- Redis host, puerto y password.
- Kafka bootstrap servers.
- Llaves RSA privadas/públicas reales.
- Issuer y audience definitivos.
- Política de bootstrap de schema.
- Niveles de logging.
- Rate limits.
- Topics Kafka.

No uses en producción:

- `classpath:keys/dev-private.pem`
- `classpath:keys/dev-public.pem`
- passwords de `.env.example`
- `APP_DATABASE_SCHEMA_INITIALIZE_ON_STARTUP=true` sin una decisión operativa explícita.

### Troubleshooting

Docker daemon apagado:

```text
Cannot connect to the Docker daemon
```

Solución: inicia Docker Desktop o el daemon de Docker y vuelve a ejecutar el comando.

Conflicto de puertos:

```bash
lsof -i :8080
lsof -i :5432
lsof -i :6379
lsof -i :9092
```

Cambia los puertos en `.env` si ya están ocupados.

Schema viejo o columnas faltantes:

```bash
docker compose down -v
docker compose up -d postgres redis kafka
```

Kafka tarda en quedar listo:

```bash
docker compose logs -f kafka
```

Redis con password:

```bash
docker exec -it identity-access-redis redis-cli -a identity_access ping
```

PostgreSQL shell:

```bash
docker exec -it identity-access-postgres psql -U identity_access -d identity_access
```

Ver eventos outbox pendientes:

```sql
SELECT event_id, event_type, status, retry_count, last_error, occurred_at
FROM outbox_event
ORDER BY occurred_at DESC
LIMIT 20;
```

---

## EN

### Description

`identity-access-service` is a self-hosted IAM microservice for systems that need authentication, sessions, JWT/JWKS, role-based authorization, dynamic RBAC catalogs, technical audit, rate limiting, and outbox-based integration events.

The service is designed to be integrated as an internal service or deployable dependency inside a platform. It is not designed as a hosted SaaS product.

Integration guide for consumer systems:

- [docs/integration/consumer-integration-guide.md](docs/integration/consumer-integration-guide.md)
- [docs/integration/endpoint-contracts-quick-test.md](docs/integration/endpoint-contracts-quick-test.md)

Governance, security and operations documentation:

- [CHANGELOG.md](CHANGELOG.md)
- [LICENSE](LICENSE)
- [SECURITY.md](SECURITY.md)
- [CONTRIBUTING.md](CONTRIBUTING.md)
- [docs/architecture/architecture.md](docs/architecture/architecture.md)
- [docs/security/threat-model.md](docs/security/threat-model.md)
- [docs/operations/deployment.md](docs/operations/deployment.md)
- [docs/operations/release-checklist.md](docs/operations/release-checklist.md)

### License

This project is distributed under Elastic License 2.0. This license allows use, copy, distribution and modification of the software, but restricts offering it to third parties as a hosted or managed service that exposes a substantial set of its functionality.

See the full text in [LICENSE](LICENSE).

### Main capabilities

- Controlled public account registration.
- Administrative account creation.
- Email/password login with session creation.
- Refresh token rotation.
- Logout and session revocation.
- Introspection of tokens issued by the service.
- JWT signing with RSA/RS256.
- JWKS publication at `/.well-known/jwks.json`.
- Dynamic roles and permissions persisted in PostgreSQL.
- Runtime authorities resolved from `user_role_assignment`, `role`, `permission`, and `role_permission`.
- Administrative APIs for accounts, roles, permissions, role assignment, account blocking, and session revocation.
- Redis-backed rate limiting for sensitive public endpoints.
- Technical security audit.
- Persisted outbox and Kafka relay.

### Requirements

To run locally with the application on the host:

- Java 21.
- Docker Desktop or Docker daemon running.
- Included Gradle wrapper (`./gradlew`).
- Available ports: `8080`, `5432`, `6379`, `9092`.

To run everything with Docker Compose:

- Docker Desktop or Docker daemon running.
- Docker Compose v2.
- Available ports: `8080`, `5432`, `6379`, `9092`.

Infrastructure used by the service:

- PostgreSQL 15.
- Redis 7 with password.
- Apache Kafka 3.8.0.

### Configuration policy

- `.env` is the main local source of values.
- `.env.example` contains the full development template.
- `docker-compose.yml` is the source of truth for local infrastructure.
- `application.yml`, `application-local.yml`, and `application-docker.yml` define how properties are consumed.
- `APP_VERSION` controls the version published in OpenAPI.
- In `local` and `docker`, schema bootstrap is enabled by default.
- In `prod`, schema bootstrap is disabled unless `APP_DATABASE_SCHEMA_INITIALIZE_ON_STARTUP=true` is explicitly configured.
- Development RSA keys must not be used in production.

### Profiles

- `local`: the application runs on the host and PostgreSQL/Redis/Kafka run in Docker Compose.
- `docker`: the application and dependencies run in Docker Compose.
- `prod`: externalized environment; requires real secrets and endpoints.

### Quick configuration

Create `.env`:

```bash
cp .env.example .env
```

If running the application from the host, load environment variables:

```bash
set -a
source .env
set +a
```

### Run local infrastructure only

Use this mode when running the application with Gradle on your machine:

```bash
docker compose up -d postgres redis kafka
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun --no-daemon
```

Check service health:

```bash
curl -s http://localhost:8080/actuator/health | jq
```

Check JWKS:

```bash
curl -s http://localhost:8080/.well-known/jwks.json | jq
```

### Run everything with Docker Compose

Use this mode when running the service and infrastructure in containers:

```bash
cp .env.example .env
docker compose up -d --build
```

View service logs:

```bash
docker compose logs -f identity-access-service
```

Stop containers without deleting data:

```bash
docker compose down
```

Recreate from scratch and remove local volumes/data:

```bash
docker compose down -v
docker compose up -d --build
```

### Database and schema

PostgreSQL loads the initialization scripts from this folder:

```text
src/main/resources/db/init/
```

When `postgres` starts through Docker Compose, the folder is mounted at:

```text
/docker-entrypoint-initdb.d/
```

PostgreSQL runs `.sql` files in alphabetical order:

```text
01-schema.sql
02-seed-roles.sql
```

If a previous PostgreSQL volume already exists, Docker will not rerun initialization scripts. To force a clean database:

```bash
docker compose down -v
docker compose up -d postgres redis kafka
```

The schema includes these operational tables, among others:

- `user_account`
- `credential`
- `credential_password`
- `user_login_attempt`
- `user_session`
- `role`
- `permission`
- `role_permission`
- `role_assignment_policy`
- `user_role_assignment`
- `auth_audit`
- `outbox_event`
- `processed_event`

### JWT keys and JWKS

The service signs tokens with RSA/RS256.

Bundled development keys:

- `src/main/resources/keys/dev-private.pem`
- `src/main/resources/keys/dev-public.pem`
- `src/main/resources/keys/dev-rollover-public.pem`

Main properties:

```env
APP_SECURITY_JWT_ALGORITHM=RS256
APP_SECURITY_JWT_KEY_ID=dev-rsa-key-1
APP_SECURITY_JWT_PRIVATE_KEY_PATH=classpath:keys/dev-private.pem
APP_SECURITY_JWT_PUBLIC_KEY_PATH=classpath:keys/dev-public.pem
APP_SECURITY_JWT_ADDITIONAL_PUBLIC_KEYS=
APP_SECURITY_JWT_ISSUER=identity-access-service
APP_SECURITY_JWT_AUDIENCE=identity-access-client
APP_SECURITY_JWT_ACCESS_TTL_SECONDS=900
APP_SECURITY_JWT_REFRESH_TTL_SECONDS=604800
APP_SECURITY_JWT_CLOCK_SKEW_SECONDS=60
APP_SECURITY_JWT_JWKS_PATH=/.well-known/jwks.json
```

`APP_SECURITY_JWT_PRIVATE_KEY_PATH` and `APP_SECURITY_JWT_PUBLIC_KEY_PATH` accept `classpath:` locations or file paths accessible by the process.

Format for additional verification public keys:

```env
APP_SECURITY_JWT_ADDITIONAL_PUBLIC_KEYS=rollover-rsa-key-0=classpath:keys/dev-rollover-public.pem
```

In production, mount or inject real keys from a secret manager or secure volume and change `APP_SECURITY_JWT_KEY_ID`, `APP_SECURITY_JWT_PRIVATE_KEY_PATH`, and `APP_SECURITY_JWT_PUBLIC_KEY_PATH`.

### Redis and rate limiting

Redis is used for operational cache and rate limiting.

Rate-limited endpoints:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/register-primary`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`

Variables:

```env
APP_REDIS_CACHE_INTROSPECTION_TTL_SECONDS=30
APP_REDIS_CACHE_PERMISSIONS_TTL_SECONDS=30
APP_REDIS_RATE_LIMIT_LOGIN_RPM=10
APP_REDIS_RATE_LIMIT_REFRESH_RPM=30
APP_REDIS_RATE_LIMIT_REGISTRATION_RPM=5
```

When the threshold is exceeded, the service responds with HTTP `429 Too Many Requests`.

### Kafka and outbox

Relevant mutations are stored in `outbox_event`, and the relay publishes events to Kafka.
Authentication failures are published as `AccountAuthenticationFailed` to `APP_KAFKA_TOPIC_AUTH_FAILED` without passwords, hashes, or tokens.

Main variables:

```env
APP_OUTBOX_RELAY_ENABLED=true
APP_OUTBOX_RELAY_BATCH_SIZE=200
APP_OUTBOX_RELAY_POLL_INTERVAL_MS=2000
APP_OUTBOX_RELAY_MAX_RETRIES=3
APP_IDEMPOTENCY_CONSUMER_NAME=identity-access-service
```

Configurable topics:

```env
APP_KAFKA_TOPIC_SESSION_OPENED=iam.session-opened.v1
APP_KAFKA_TOPIC_ACCOUNT_REGISTERED=iam.account-registered.v1
APP_KAFKA_TOPIC_AUTH_FAILED=iam.auth-failed.v1
APP_KAFKA_TOPIC_SESSION_REFRESHED=iam.session-refreshed.v1
APP_KAFKA_TOPIC_SESSION_REVOKED=iam.session-revoked.v1
APP_KAFKA_TOPIC_ROLE_ASSIGNED_TO_ACCOUNT=iam.role-assigned-to-account.v1
APP_KAFKA_TOPIC_ACCOUNT_BLOCKED=iam.account-blocked.v1
APP_KAFKA_TOPIC_ACCOUNT_UNBLOCKED=iam.account-unblocked.v1
APP_KAFKA_TOPIC_ROLE_CREATED=iam.role-created.v1
APP_KAFKA_TOPIC_ROLE_UPDATED=iam.role-updated.v1
APP_KAFKA_TOPIC_ROLE_DISABLED=iam.role-disabled.v1
APP_KAFKA_TOPIC_PERMISSION_CREATED=iam.permission-created.v1
APP_KAFKA_TOPIC_PERMISSION_UPDATED=iam.permission-updated.v1
APP_KAFKA_TOPIC_PERMISSION_DISABLED=iam.permission-disabled.v1
APP_KAFKA_TOPIC_PERMISSION_GRANTED_TO_ROLE=iam.permission-granted-to-role.v1
APP_KAFKA_TOPIC_PERMISSION_REVOKED_FROM_ROLE=iam.permission-revoked-from-role.v1
```

In production, do not depend on `auto.create.topics.enable`. Create topics before enabling the relay:

```bash
KAFKA_TOPICS_CREATE_MODE=cli \
KAFKA_BOOTSTRAP_SERVERS=<broker:9092> \
KAFKA_TOPIC_PARTITIONS=3 \
KAFKA_TOPIC_REPLICATION_FACTOR=3 \
scripts/operations/create-kafka-topics.sh
```

With local Docker Compose:

```bash
KAFKA_TOPICS_CREATE_MODE=docker-compose scripts/operations/create-kafka-topics.sh
```

Persisted session expiration when refresh token lifetime ends:

```env
APP_SESSIONS_EXPIRATION_ENABLED=true
APP_SESSIONS_EXPIRATION_SWEEP_INTERVAL_MS=60000
```

If event publishing is not needed in a specific local environment, disable the relay:

```env
APP_OUTBOX_RELAY_ENABLED=false
```

### Registration and initial roles

Registration variables:

```env
APP_IAM_REGISTRATION_PUBLIC_ENABLED=true
APP_IAM_REGISTRATION_PUBLIC_DEFAULT_ROLE_CODE=ACCOUNT_USER
APP_IAM_REGISTRATION_PRIMARY_ENABLED=true
APP_IAM_REGISTRATION_PRIMARY_INITIAL_ROLE_CODE=SYSTEM_ADMIN
APP_IAM_REGISTRATION_ADMIN_DEFAULT_ROLE_CODE=ACCOUNT_USER
APP_IAM_REGISTRATION_PROTECTED_ROLE_CODES=SYSTEM_ADMIN
```

Default semantics:

- `POST /api/v1/auth/register-primary` creates the primary/bootstrap account with `SYSTEM_ADMIN`.
- `POST /api/v1/auth/register` creates normal public accounts with `ACCOUNT_USER`.
- Administrative account creation uses `ACCOUNT_USER` when no role is specified.
- `SYSTEM_ADMIN` is a protected exclusive-assignment role: only one active assignment can exist.
- Protected roles are not assigned by normal administrative flows unless explicitly allowed by policy.
- Authenticated principal authorities are derived from the database, not hardcoded lists.

Main seeded roles:

- `SYSTEM_ADMIN`
- `ACCESS_ADMIN`
- `ACCESS_MANAGER`
- `ACCOUNT_USER`
- `ACCOUNT_READONLY`

Main seeded permissions:

- `iam.account.create`
- `iam.account.read`
- `iam.account.update`
- `iam.account.block`
- `iam.account.unblock`
- `iam.access.assign-role`
- `iam.access.revoke-role`
- `iam.session.read`
- `iam.session.revoke`
- `iam.permission.read`
- `iam.role.create`
- `iam.role.read`
- `iam.role.update`
- `iam.permission.create`
- `iam.permission.update`
- `iam.access-profile.read`

### Public endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/register-primary`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/introspect`
- `GET /.well-known/jwks.json`
- `GET /actuator/health`

`logout` requires an authenticated user.

### Administrative endpoints

Accounts:

- `GET /api/v1/admin/iam/accounts`
- `POST /api/v1/admin/iam/accounts`
- `POST /api/v1/admin/iam/accounts/{accountId}/roles`
- `POST /api/v1/admin/iam/accounts/{accountId}/block`
- `POST /api/v1/admin/iam/accounts/{accountId}/unblock`
- `GET /api/v1/admin/iam/accounts/{accountId}/permissions`
- `GET /api/v1/admin/iam/sessions`
- `POST /api/v1/admin/iam/accounts/{accountId}/sessions/revoke`

Roles and permissions:

- `GET /api/v1/admin/iam/roles`
- `POST /api/v1/admin/iam/roles`
- `PATCH /api/v1/admin/iam/roles/{roleId}`
- `POST /api/v1/admin/iam/roles/{roleId}/disable`
- `GET /api/v1/admin/iam/roles/{roleId}/permissions`
- `POST /api/v1/admin/iam/roles/{roleId}/permissions`
- `DELETE /api/v1/admin/iam/roles/{roleId}/permissions/{permissionCode}`
- `GET /api/v1/admin/iam/permissions`
- `POST /api/v1/admin/iam/permissions`
- `PATCH /api/v1/admin/iam/permissions/{permissionId}`
- `POST /api/v1/admin/iam/permissions/{permissionId}/disable`

### Swagger / OpenAPI

- Dashboard UI: `http://localhost:8080/console/index.html`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Token usage:

1. Run login and copy `accessToken`.
2. Open Swagger UI.
3. Press `Authorize`.
4. Enter the token as `Bearer <accessToken>` if the UI does not add the prefix automatically.
5. Execute administrative endpoints.

### Operations Dashboard

The service also serves a static web console at `http://localhost:8080/console/index.html`.
This UI can log in, introspect tokens, list accounts, sessions, roles and permissions, and execute administrative actions according to the effective permissions in the access token.
The backend remains the real authorization boundary; the console only hides or disables actions when the token does not include the required authority.

### Minimal smoke test

Register initial account:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register-primary \
  -H 'Content-Type: application/json' \
  -d '{
    "email":"admin@example.test",
    "password":"Admin123!"
  }' | jq
```

An automated smoke test is also available for clean environments:

```bash
BASE_URL=http://localhost:8080 scripts/smoke/identity-access-release-smoke.sh
```

`SYSTEM_ADMIN` break-glass recovery is not exposed through HTTP. The operational procedure is documented in `docs/operations/break-glass-system-admin.md`.

Login:

```bash
TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -H 'User-Agent: curl' \
  -d '{
    "email":"admin@example.test",
    "password":"Admin123!"
  }')

echo "$TOKEN_RESPONSE" | jq
ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.accessToken')
REFRESH_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.refreshToken')
```

Introspect:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/introspect \
  -H 'Content-Type: application/json' \
  -d "{\"token\":\"$ACCESS_TOKEN\"}" | jq
```

Refresh:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/refresh \
  -H 'Content-Type: application/json' \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" | jq
```

List roles with Bearer token:

```bash
curl -s http://localhost:8080/api/v1/admin/iam/roles \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq
```

### Tests

Unit tests and slice tests:

```bash
./gradlew clean test --no-daemon
```

Integration tests with real infrastructure through Testcontainers:

```bash
./gradlew integrationTest --no-daemon
```

Full suite:

```bash
./gradlew clean test integrationTest --no-daemon
```

`integrationTest` requires Docker daemon running and validates:

- PostgreSQL schema/seed.
- R2DBC adapters.
- Redis rate limiter.
- Outbox persistence.
- Kafka publishing through the outbox relay.

### Production

For production, externalize at least:

- PostgreSQL R2DBC URL, username, and password.
- Redis host, port, and password.
- Kafka bootstrap servers.
- Real RSA private/public keys.
- Final issuer and audience.
- Schema bootstrap policy.
- Logging levels.
- Rate limits.
- Kafka topics.

Do not use in production:

- `classpath:keys/dev-private.pem`
- `classpath:keys/dev-public.pem`
- passwords from `.env.example`
- `APP_DATABASE_SCHEMA_INITIALIZE_ON_STARTUP=true` without an explicit operational decision.

### Troubleshooting

Docker daemon is not running:

```text
Cannot connect to the Docker daemon
```

Solution: start Docker Desktop or the Docker daemon and rerun the command.

Port conflicts:

```bash
lsof -i :8080
lsof -i :5432
lsof -i :6379
lsof -i :9092
```

Change ports in `.env` if they are already used.

Old schema or missing columns:

```bash
docker compose down -v
docker compose up -d postgres redis kafka
```

Kafka takes time to become ready:

```bash
docker compose logs -f kafka
```

Redis with password:

```bash
docker exec -it identity-access-redis redis-cli -a identity_access ping
```

PostgreSQL shell:

```bash
docker exec -it identity-access-postgres psql -U identity_access -d identity_access
```

Inspect pending outbox events:

```sql
SELECT event_id, event_type, status, retry_count, last_error, occurred_at
FROM outbox_event
ORDER BY occurred_at DESC
LIMIT 20;
```
