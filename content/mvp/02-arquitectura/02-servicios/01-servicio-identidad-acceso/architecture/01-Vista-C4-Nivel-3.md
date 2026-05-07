---
title: "Vista C4 Nivel 3"
linkTitle: "1. Vista C4 L3"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-identidad-acceso/arquitectura-interna/vista-c4-nivel-3/"
---

## Proposito
Definir la vista C4 de componente para `identity-access-service`, detallando limites internos, dependencias y responsabilidades tecnicas para implementacion reactiva con Spring WebFlux.

## Alcance y fronteras
- Incluye componentes internos de IAM en la vista componente: `Adapter-in`, `Application service`, `Domain`, `Adapter-out`.
- Incluye unicamente clases de implementacion representativas de cada componente para identificacion rapida.
- Excluye de la vista componente: DTOs, mappers, interfaces de puertos, utilidades y clases de configuracion.
- Incluye dependencias con `api-gateway-service`, `config-server`, `eureka-server`, `kafka-cluster`, `redis-cache` y `IAM DB`.
- Excluye decisiones de codigo de otros servicios core.

## Rol del servicio en el sistema
`identity-access-service` es la autoridad semantica de identidad y sesion del dominio:
- autentica usuarios de organizaciones B2B,
- emite y valida tokens de acceso,
- gestiona sesiones activas/revocadas,
- aplica autorizacion base por rol y `tenantId`,
- publica eventos de seguridad relevantes para auditoria y observabilidad.

## C4 componente del servicio
La vista de componentes se divide por caso de uso para reducir cruces visuales sin perder detalle estructural del servicio. Cada diagrama conserva los mismos grupos (`Adapter-in`, `Application service`, `Domain`, `Adapter-out`) y muestra solo las relaciones relevantes para ese flujo.

{{< tabs groupid="identity-access-c4-l3" >}}
{{< tab title="Login" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    AUTH_CTRL["AuthHttpController"]
  end

  subgraph APP["Application service"]
    LOGIN_UC["LoginUseCase"]
  end

  subgraph DOM["Domain"]
    USER_AGG["UserAggregate"]
    SESSION_AGG["SessionAggregate"]
    TOKEN_POLICY["TokenPolicy"]
    PASSWORD_POLICY["PasswordPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    USER_REPO["UserR2dbcRepositoryAdapter"]
    SESSION_REPO["SessionR2dbcRepositoryAdapter"]
    AUDIT_REPO["AuthAuditR2dbcRepositoryAdapter"]
    BCRYPT_HASHER["BCryptPasswordHasherAdapter"]
    JWT_SIGNER["JwtSignerAdapter"]
    RATE_LIMIT_ADP["SecurityRateLimitRedisAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  AUTH_CTRL --> LOGIN_UC
  LOGIN_UC --> USER_AGG
  LOGIN_UC --> SESSION_AGG
  LOGIN_UC --> PASSWORD_POLICY
  LOGIN_UC --> TOKEN_POLICY
  LOGIN_UC --> TENANT_POLICY

  LOGIN_UC --> USER_REPO
  LOGIN_UC --> SESSION_REPO
  LOGIN_UC --> RATE_LIMIT_ADP
  LOGIN_UC --> BCRYPT_HASHER
  LOGIN_UC --> JWT_SIGNER
  LOGIN_UC --> AUDIT_REPO
  LOGIN_UC --> OUTBOX_ADP
  TOKEN_POLICY --> CLOCK_ADP
  OUTBOX_ADP --> EVENT_PUB

  USER_REPO --> IAMDB["IAM DB (PostgreSQL)"]
  SESSION_REPO --> IAMDB
  AUDIT_REPO --> IAMDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="Refresh" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    AUTH_CTRL["AuthHttpController"]
  end

  subgraph APP["Application service"]
    REFRESH_UC["RefreshSessionUseCase"]
  end

  subgraph DOM["Domain"]
    SESSION_AGG["SessionAggregate"]
    TOKEN_POLICY["TokenPolicy"]
  end

  subgraph OUT["Adapter-out"]
    SESSION_REPO["SessionR2dbcRepositoryAdapter"]
    JWT_SIGNER["JwtSignerAdapter"]
    CACHE_ADP["SessionCacheRedisAdapter"]
  end

  AUTH_CTRL --> REFRESH_UC
  REFRESH_UC --> SESSION_AGG
  REFRESH_UC --> TOKEN_POLICY
  REFRESH_UC --> SESSION_REPO
  REFRESH_UC --> JWT_SIGNER
  REFRESH_UC --> CACHE_ADP

  SESSION_REPO --> IAMDB["IAM DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="Logout" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    AUTH_CTRL["AuthHttpController"]
  end

  subgraph APP["Application service"]
    LOGOUT_UC["LogoutUseCase"]
  end

  subgraph DOM["Domain"]
    SESSION_AGG["SessionAggregate"]
  end

  subgraph OUT["Adapter-out"]
    SESSION_REPO["SessionR2dbcRepositoryAdapter"]
    CACHE_ADP["SessionCacheRedisAdapter"]
  end

  AUTH_CTRL --> LOGOUT_UC
  LOGOUT_UC --> SESSION_AGG
  LOGOUT_UC --> SESSION_REPO
  LOGOUT_UC --> CACHE_ADP

  SESSION_REPO --> IAMDB["IAM DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="Introspect" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    INTROSPECT_CTRL["TokenIntrospectionController"]
  end

  subgraph APP["Application service"]
    INTROSPECT_UC["IntrospectTokenUseCase"]
  end

  subgraph DOM["Domain"]
    SESSION_AGG["SessionAggregate"]
    AUTHZ_POLICY["AuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    SESSION_REPO["SessionR2dbcRepositoryAdapter"]
    ROLE_REPO["RoleR2dbcRepositoryAdapter"]
    JWT_VERIFIER["JwtVerifierAdapter"]
    JWKS_PROVIDER["JwksKeyProviderAdapter"]
  end

  INTROSPECT_CTRL --> INTROSPECT_UC
  INTROSPECT_UC --> SESSION_AGG
  INTROSPECT_UC --> AUTHZ_POLICY
  INTROSPECT_UC --> SESSION_REPO
  INTROSPECT_UC --> ROLE_REPO
  INTROSPECT_UC --> JWT_VERIFIER
  INTROSPECT_UC --> JWKS_PROVIDER

  SESSION_REPO --> IAMDB["IAM DB (PostgreSQL)"]
  ROLE_REPO --> IAMDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="AssignRole" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ADMIN_CTRL["AdminIamHttpController"]
  end

  subgraph APP["Application service"]
    ASSIGN_ROLE_UC["AssignRoleUseCase"]
  end

  subgraph DOM["Domain"]
    ROLE_AGG["RoleAggregate"]
  end

  subgraph OUT["Adapter-out"]
    ROLE_REPO["RoleR2dbcRepositoryAdapter"]
  end

  ADMIN_CTRL --> ASSIGN_ROLE_UC
  ASSIGN_ROLE_UC --> ROLE_AGG
  ASSIGN_ROLE_UC --> ROLE_REPO

  ROLE_REPO --> IAMDB["IAM DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="BlockUser" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ADMIN_CTRL["AdminIamHttpController"]
  end

  subgraph APP["Application service"]
    BLOCK_UC["BlockUserUseCase"]
  end

  subgraph DOM["Domain"]
    USER_AGG["UserAggregate"]
    SESSION_AGG["SessionAggregate"]
  end

  subgraph OUT["Adapter-out"]
    USER_REPO["UserR2dbcRepositoryAdapter"]
    SESSION_REPO["SessionR2dbcRepositoryAdapter"]
  end

  ADMIN_CTRL --> BLOCK_UC
  BLOCK_UC --> USER_AGG
  BLOCK_UC --> SESSION_AGG
  BLOCK_UC --> USER_REPO
  BLOCK_UC --> SESSION_REPO

  USER_REPO --> IAMDB["IAM DB (PostgreSQL)"]
  SESSION_REPO --> IAMDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RevokeSessions" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ADMIN_CTRL["AdminIamHttpController"]
  end

  subgraph APP["Application service"]
    REVOKE_UC["RevokeSessionsUseCase"]
  end

  subgraph DOM["Domain"]
    SESSION_AGG["SessionAggregate"]
  end

  subgraph OUT["Adapter-out"]
    SESSION_REPO["SessionR2dbcRepositoryAdapter"]
    CACHE_ADP["SessionCacheRedisAdapter"]
  end

  ADMIN_CTRL --> REVOKE_UC
  REVOKE_UC --> SESSION_AGG
  REVOKE_UC --> SESSION_REPO
  REVOKE_UC --> CACHE_ADP

  SESSION_REPO --> IAMDB["IAM DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ListUserSessions" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    SESSION_QUERY_CTRL["SessionQueryHttpController"]
  end

  subgraph APP["Application service"]
    LIST_SESSIONS_UC["ListUserSessionsUseCase"]
  end

  subgraph DOM["Domain"]
    SESSION_AGG["SessionAggregate"]
  end

  subgraph OUT["Adapter-out"]
    SESSION_REPO["SessionR2dbcRepositoryAdapter"]
  end

  SESSION_QUERY_CTRL --> LIST_SESSIONS_UC
  LIST_SESSIONS_UC --> SESSION_AGG
  LIST_SESSIONS_UC --> SESSION_REPO

  SESSION_REPO --> IAMDB["IAM DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetUserPermissions" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    SESSION_QUERY_CTRL["SessionQueryHttpController"]
  end

  subgraph APP["Application service"]
    GET_PERMS_UC["GetUserPermissionsUseCase"]
  end

  subgraph DOM["Domain"]
    ROLE_AGG["RoleAggregate"]
    AUTHZ_POLICY["AuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ROLE_REPO["RoleR2dbcRepositoryAdapter"]
    CACHE_ADP["SessionCacheRedisAdapter"]
  end

  SESSION_QUERY_CTRL --> GET_PERMS_UC
  GET_PERMS_UC --> ROLE_AGG
  GET_PERMS_UC --> AUTHZ_POLICY
  GET_PERMS_UC --> ROLE_REPO
  GET_PERMS_UC --> CACHE_ADP

  ROLE_REPO --> IAMDB["IAM DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="OrgSuspended" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    IAM_EVT_CONS["IamEventConsumer"]
    TENANT_EVT_LST["TenantLifecycleEventListener"]
  end

  subgraph APP["Application service"]
    BLOCK_UC["BlockUserUseCase"]
  end

  subgraph DOM["Domain"]
    USER_AGG["UserAggregate"]
    SESSION_AGG["SessionAggregate"]
    SESSION_POLICY["SessionPolicy"]
  end

  subgraph OUT["Adapter-out"]
    DEDUPE_ADP["ProcessedEventR2dbcRepositoryAdapter"]
    USER_REPO["UserR2dbcRepositoryAdapter"]
    SESSION_REPO["SessionR2dbcRepositoryAdapter"]
    CACHE_ADP["SessionCacheRedisAdapter"]
    AUDIT_REPO["AuthAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  IAM_EVT_CONS --> TENANT_EVT_LST
  TENANT_EVT_LST --> DEDUPE_ADP
  TENANT_EVT_LST --> BLOCK_UC
  BLOCK_UC --> USER_AGG
  BLOCK_UC --> SESSION_AGG
  BLOCK_UC --> SESSION_POLICY
  BLOCK_UC --> USER_REPO
  BLOCK_UC --> SESSION_REPO
  BLOCK_UC --> CACHE_ADP
  BLOCK_UC --> AUDIT_REPO
  BLOCK_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  DEDUPE_ADP --> IAMDB["IAM DB (PostgreSQL)"]
  USER_REPO --> IAMDB
  SESSION_REPO --> IAMDB
  AUDIT_REPO --> IAMDB
  CACHE_ADP --> REDIS["redis-cache"]
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="AuthFailedThreshold" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    IAM_EVT_CONS["IamEventConsumer"]
    SECURITY_EVT_LST["SecurityIncidentEventListener"]
  end

  subgraph APP["Application service"]
    BLOCK_UC["BlockUserUseCase"]
  end

  subgraph DOM["Domain"]
    USER_AGG["UserAggregate"]
    SESSION_AGG["SessionAggregate"]
    SESSION_POLICY["SessionPolicy"]
  end

  subgraph OUT["Adapter-out"]
    DEDUPE_ADP["ProcessedEventR2dbcRepositoryAdapter"]
    USER_REPO["UserR2dbcRepositoryAdapter"]
    SESSION_REPO["SessionR2dbcRepositoryAdapter"]
    CACHE_ADP["SessionCacheRedisAdapter"]
    AUDIT_REPO["AuthAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  IAM_EVT_CONS --> SECURITY_EVT_LST
  SECURITY_EVT_LST --> DEDUPE_ADP
  SECURITY_EVT_LST --> BLOCK_UC
  BLOCK_UC --> USER_AGG
  BLOCK_UC --> SESSION_AGG
  BLOCK_UC --> SESSION_POLICY
  BLOCK_UC --> USER_REPO
  BLOCK_UC --> SESSION_REPO
  BLOCK_UC --> CACHE_ADP
  BLOCK_UC --> AUDIT_REPO
  BLOCK_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  DEDUPE_ADP --> IAMDB["IAM DB (PostgreSQL)"]
  USER_REPO --> IAMDB
  SESSION_REPO --> IAMDB
  AUDIT_REPO --> IAMDB
  CACHE_ADP --> REDIS["redis-cache"]
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< /tabs >}}

## Componentes base por capa (vista componente)
| Capa | Clases base | Responsabilidad tecnica |
|---|---|---|
| `Adapter-in` | `AuthHttpController`, `AdminIamHttpController`, `TokenIntrospectionController`, `SessionQueryHttpController`, `IamEventConsumer`, `TenantLifecycleEventListener`, `SecurityIncidentEventListener` | Recibir HTTP/eventos, deduplicar consumo y traducir entradas a casos de uso |
| `Application service` | `LoginUseCase`, `RefreshSessionUseCase`, `LogoutUseCase`, `IntrospectTokenUseCase`, `AssignRoleUseCase`, `BlockUserUseCase`, `RevokeSessionsUseCase`, `ListUserSessionsUseCase`, `GetUserPermissionsUseCase` | Orquestar casos de uso, idempotencia, errores y transiciones del flujo |
| `Domain` | `UserAggregate`, `SessionAggregate`, `RoleAggregate`, `TokenPolicy`, `PasswordPolicy`, `SessionPolicy`, `TenantIsolationPolicy`, `AuthorizationPolicy`, `PermissionResolutionService` | Mantener invariantes y reglas del negocio IAM |
| `Adapter-out` | `UserR2dbcRepositoryAdapter`, `SessionR2dbcRepositoryAdapter`, `RoleR2dbcRepositoryAdapter`, `ProcessedEventR2dbcRepositoryAdapter`, `AuthAuditR2dbcRepositoryAdapter`, `BCryptPasswordHasherAdapter`, `JwtSignerAdapter`, `JwtVerifierAdapter`, `JwksKeyProviderAdapter`, `SessionCacheRedisAdapter`, `SecurityRateLimitRedisAdapter`, `OutboxPersistenceAdapter`, `KafkaDomainEventPublisherAdapter`, `SystemClockAdapter` | Conectar con DB, cache, broker, seguridad criptografica y servicios externos |

## Nota de modelado
- Esta vista componente no detalla estructura de carpetas.
- Esta vista componente lista solo implementaciones de `Adapter-in`, `Application service`, `Domain` y `Adapter-out`.
- DTOs/mappers/interfaces/config se detallan en la vista de codigo.
- El detalle de paquetes/codigo se mantiene en:
  - [02-Vista-de-Codigo.md](/Users/jose/Development/Documentation/arkab2b-docs/content/mvp/02-arquitectura/services/identity-access-service/architecture/02-Vista-de-Codigo.md)

## Dependencias externas permitidas
| Dependencia | Tipo | Uso en IAM | Criticidad |
|---|---|---|---|
| `api-gateway-service` | plataforma | Entrada principal de trafico autenticado | alta |
| `IAM DB (PostgreSQL)` | datos | Fuente de verdad de usuario/sesion/rol | critica |
| `redis-cache` | soporte | Cache de sesiones e introspeccion | media |
| `kafka-cluster` | soporte | Eventos de seguridad y auditoria near-real-time | media |
| `config-server` | plataforma | Configuracion centralizada y llaves activas | alta |
| `eureka-server` | plataforma | Service discovery | media |

## Modelo de autenticacion y autorizacion runtime
| Flujo | Autenticacion | Autorizacion y legitimidad |
|---|---|---|
| `Login`, `Refresh`, `Logout`, `Introspect` | `identity-access-service` resuelve autenticacion o verificacion de sesion directamente con `ReactiveSecurityConfig`, `AuthenticationManagerConfig`, `JwtVerifierAdapter`, `JwksProviderPort` y `JwksKeyProviderAdapter`. | El dominio IAM decide continuidad de sesion, password y emision/revocacion con `PasswordPolicy`, `SessionPolicy` y `TokenPolicy`. |
| HTTP admin/query | `api-gateway-service` autentica el JWT antes de enrutar al servicio. | `identity-access-service` materializa `PrincipalContext` administrativo y resuelve el guard funcional base con `PermissionResolutionService`; despues valida `tenant` y legitimidad del actor con `TenantIsolationPolicy` y `AuthorizationPolicy`. |
| listeners de seguridad | No depende de JWT de usuario. | `TenantLifecycleEventListener` y `SecurityIncidentEventListener` materializan `TriggerContext` mediante `TriggerContextResolver`, validan trigger, dedupe y aplican las politicas de seguridad del dominio antes de bloquear usuarios o revocar sesiones. |


## Modelo de errores y excepciones runtime
| Responsabilidad | Componentes | Aplicacion |
|---|---|---|
| Decision semantica | `Application service`, `Domain service`, `PermissionResolutionService`, `TenantIsolationPolicy`, `AuthorizationPolicy` | Los casos de IAM expresan rechazo temprano y rechazo de decision mediante familias canonicas de acceso/contexto (`ApplicationException`, `AuthorizationDeniedException`, `TenantIsolationException`, `ResourceNotFoundException`) y de decision (`DomainException`, `DomainRuleViolationException`, `ConflictException`) sin filtrar errores tecnicos al actor o trigger. |
| Cierre HTTP | `AuthHttpController`, `AdminIamHttpController`, `TokenIntrospectionController`, `SessionQueryHttpController`, `WebExceptionHandlerConfig` | El adapter-in HTTP traduce la familia semantica o tecnica a un envelope canonico con `errorCode`, `category`, `traceId`, `correlationId` y `timestamp`, incluso cuando IAM es la autoridad que autentica directamente el flujo. |
| Cierre async | `TenantLifecycleEventListener`, `SecurityIncidentEventListener`, `ProcessedEventR2dbcRepositoryAdapter` | Los flujos event-driven tratan duplicados como `noop idempotente`, distinguen fallos retryable/no-retryable y cierran la incidencia por reintento, DLQ o auditoria operativa. |

## Soporte de observabilidad
| Elemento | Componentes principales | Funcion arquitectonica |
|---|---|---|
| Configuracion de metricas y trazas | `ObservabilityConfig` | Expone la configuracion base para `meterRegistry` y `tracingBridge`, dejando preparada la instrumentacion transversal del servicio. |
| Propagacion de correlacion | `CorrelationIdProviderPort`, `CorrelationIdProviderAdapter` | Propagan el `correlationId` entre entrada HTTP, consultas, mutaciones administrativas y flujos asincronos publicados desde outbox. |
| Auditoria de seguridad | `SecurityAuditPort`, `AuthAuditR2dbcRepositoryAdapter`, `ReactiveAuthAuditRepository` | Registran evidencia tecnica y operativa de login, bloqueo, revocacion y demas mutaciones IAM en `auth_audit`. |
| Emision de eventos observables | `OutboxPersistenceAdapter`, `OutboxEventRelayPublisher`, `DomainEventKafkaMapper`, `KafkaDomainEventPublisherAdapter` | Materializan y publican eventos IAM hacia Kafka para integracion, trazabilidad y observabilidad near-real-time. |

Nota:
- Esta vista solo documenta los componentes que habilitan observabilidad dentro de la arquitectura.
- La definicion detallada de metricas, logs, trazas, alertas y dashboards corresponde al pilar de calidad u operacion.

## Canales de eventos (naming canonico)
Convencion aplicada: `<bc>.<event-name>.v<major>`.

| Evento | Topic canonico |
|---|---|
| `UserLoggedIn` | `iam.user-logged-in.v1` |
| `AuthFailed` | `iam.auth-failed.v1` |
| `SessionRefreshed` | `iam.session-refreshed.v1` |
| `SessionRevoked` | `iam.session-revoked.v1` |
| `SessionsRevokedByUser` | `iam.sessions-revoked-by-user.v1` |
| `RoleAssigned` | `iam.user-role-assigned.v1` |
| `UserBlocked` | `iam.user-blocked.v1` |

## Restricciones de diseno
- `MUST`: ninguna regla de autorizacion se resuelve solo en gateway; IAM valida dominio.
- `MUST`: toda operacion mutante de IAM conserva `tenantId` como campo obligatorio.
- `MUST`: operaciones criticas (`login`, `refresh`, `logout`, `revoke`) son idempotentes por clave tecnica.
- `MUST`: verificacion de password con `BCrypt` parametrizable por entorno (work factor).
- `SHOULD`: evitar dependencias sincronas a otros servicios core durante autenticacion.

## Riesgos y trade-offs
- Riesgo: acoplar JWT validation a una sola implementacion de llave.
  - Mitigacion: puerto `JwksKeyProvider` con estrategia de rotacion.
- Trade-off: introspeccion con cache Redis reduce latencia, pero aumenta complejidad operativa.
