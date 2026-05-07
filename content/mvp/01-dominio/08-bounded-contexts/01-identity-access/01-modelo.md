---
title: "Modelo"
linkTitle: "2. Modelo"
weight: 2
url: "/mvp/dominio/contextos-delimitados/identidad-acceso/modelo/"
---

## Marco del modelo
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Modelo conceptual del BC `identity-access`.

## Entidades principales
_Responde: que entidades estructuran el modelo local._
- `user_account`.
- `credential` / `credential_password`.
- `user_session`.
- `role`.
- `user_role_assignment`.
- `auth_audit` como evidencia tecnica de seguridad.

## Value objects principales
_Responde: que objetos de valor expresan reglas relevantes sin identidad propia._
- `TenantId`.
- `EmailAddress`.
- `PermissionSet`.
- `SessionTokenRef`.
- `RoleCode`.
- `CorrelationId`.

## Estados importantes
_Responde: que estados son relevantes para entender el ciclo local._
| Entidad | Estados permitidos | Estado inicial | Terminales |
|---|---|---|---|
| Usuario | `ACTIVE`, `BLOCKED`, `DISABLED` | `ACTIVE` | `BLOCKED`, `DISABLED` |
| Sesion | `ACTIVE`, `REVOKED`, `EXPIRED` | `ACTIVE` | `REVOKED`, `EXPIRED` |
| Credencial | `ACTIVE`, `ROTATED`, `COMPROMISED` | `ACTIVE` | `COMPROMISED` |
| AsignacionRol | `ACTIVE`, `REVOKED` | `ACTIVE` | `REVOKED` |

## Reglas de negocio nucleo
_Responde: que reglas de negocio sostienen el modelo del contexto._
- Usuario bloqueado o deshabilitado no crea ni refresca sesion activa (`RN-ACC-01`).
- Toda sesion, credencial y asignacion de rol debe conservar `tenant` consistente (`I-ACC-02`).
- En el estado actual del MVP solo existe una credencial `PASSWORD + PRIMARY + LOCAL` activa por usuario.
- Rol asignado y usuario asignado pertenecen al mismo tenant; no existen asignaciones activas cross-tenant.
- Todo hecho critico de seguridad debe dejar evidencia correlacionable en `auth_audit` y, cuando aplique, en `outbox_event`.

## Identidad de agregados
_Responde: como se identifica cada agregado relevante del contexto._
- `UserAggregate(userId, tenantId, email, status, credential, failedAttempts)`.
- `SessionAggregate(sessionId, userId, tenantId, accessJti, refreshJti, status, timestamps)`.
- `RoleAggregate(roleId, tenantId, roleCode, permissions)`.
- `UserRoleAssignment(assignmentId, userId, roleId, tenantId, status)` como entidad de consistencia con identidad propia.

Nota:
- `auth_audit`, `outbox_event` y `processed_event` son registros de soporte tecnico del BC y no agregados core del dominio.
