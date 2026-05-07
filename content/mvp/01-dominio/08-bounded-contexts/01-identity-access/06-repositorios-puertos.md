---
title: "Repositorios y Puertos"
linkTitle: "7. Repositorios y Puertos"
weight: 7
url: "/mvp/dominio/contextos-delimitados/identidad-acceso/repositorios-puertos/"
---

## Marco de puertos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir puertos de entrada/salida y contratos de persistencia/integracion de `identity-access`.

## Puertos de entrada (in)
_Responde: que puertos de entrada exponen casos de uso o capacidades del contexto._
| Port | Tipo | Metodos clave | Uso |
|---|---|---|---|
| `LoginCommandUseCase` | in | `execute(LoginCommand)` | autenticacion interactiva |
| `RefreshSessionCommandUseCase` | in | `execute(RefreshSessionCommand)` | rotacion de tokens y continuidad de sesion |
| `LogoutCommandUseCase` | in | `execute(LogoutCommand)` | cierre de sesion individual |
| `AssignRoleCommandUseCase` | in | `execute(AssignRoleCommand)` | administracion de autorizacion |
| `BlockUserCommandUseCase` | in | `execute(BlockUserCommand)` | contencion operativa del usuario |
| `RevokeSessionsCommandUseCase` | in | `execute(RevokeSessionsCommand)` | revocacion masiva de sesiones |
| `IntrospectTokenQueryUseCase` | in | `execute(IntrospectTokenQuery)` | consulta de estado de token |
| `ListUserSessionsQueryUseCase` | in | `execute(ListUserSessionsQuery)` | consulta administrativa de sesiones |
| `GetUserPermissionsQueryUseCase` | in | `execute(GetUserPermissionsQuery)` | resolucion de permisos efectivos |

## Puertos de salida (out)
_Responde: que puertos de salida necesita el contexto para colaborar con otros sistemas._
| Port | Tipo | Metodos clave | Uso |
|---|---|---|---|
| `UserPersistencePort` | out | `findByEmail`, `findById`, `save` | persistencia de usuario, credencial e intentos |
| `SessionPersistencePort` | out | `create`, `findActiveByJti`, `revokeBySessionId`, `revokeAllByUser`, `listByUser` | ciclo de sesion |
| `RolePersistencePort` | out | `assignRole`, `revokeRole`, `findPermissions` | asignaciones y permisos efectivos |
| `OutboxPersistencePort` | out | `savePending`, `markPublished`, `fetchPending` | publicacion confiable de eventos |
| `ProcessedEventPersistencePort` | out | `exists`, `save` | dedupe de consumidores |
| `PasswordHashPort` | out | `hash`, `verify` | verificacion y rotacion de password |
| `JwtSigningPort` | out | `issueAccessToken`, `issueRefreshToken` | emision de JWT |
| `JwtVerificationPort` | out | `verify` | verificacion criptografica de JWT |
| `JwksProviderPort` | out | `resolvePublicKey` | resolucion de llaves por `kid` |
| `SecurityAuditPort` | out | `record` | evidencia tecnica de seguridad |
| `DomainEventPublisherPort` | out | `publish` | publicacion al broker desde relay/outbox |
| `SessionCachePort` | out | `putIntrospection`, `getIntrospection`, `evictByJti`, `evictByUser` | cache de sesion/introspeccion |
| `SecurityRateLimitPort` | out | `consumeLoginBudget`, `isBlocked`, `reset` | control anti abuso |
| `ClockPort` | out | `now` | tiempo consistente de dominio |
| `CorrelationIdProviderPort` | out | `current`, `generate` | correlacion end-to-end |

## Contratos de consistencia
_Responde: que contratos preservan consistencia entre puertos, repositorios y reglas locales._
- `login`: persiste evidencia tecnica y la sesion resultante dentro de la misma decision de negocio antes de publicar eventos.
- `block user`: persiste bloqueo y revocacion de sesiones antes de responder exito.
- Toda mutacion de seguridad que publica hechos externos escribe primero en `outbox_event`.
- Las consultas administrativas materializan `PrincipalContext` local y no delegan la autorizacion contextual al gateway.
