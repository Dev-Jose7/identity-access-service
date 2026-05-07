---
title: "Agregados"
linkTitle: "3. Agregados"
weight: 3
url: "/mvp/dominio/contextos-delimitados/identidad-acceso/agregados/"
---

## Marco de agregados
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir agregados e invariantes de `identity-access`.

## Agregados
_Responde: que agregados protegen consistencia dentro del contexto._

### UserAggregate
_Esta subseccion detalla useraggregate dentro del contexto del documento._
- Proposito: garantizar identidad autenticable coherente por tenant y gobernar el estado funcional del usuario.
- Invariantes:
  - `I-ACC-01`: sesion o refresh solo para usuario habilitado.
  - `I-ACC-02`: `tenantId` del usuario coincide con su credencial y con el contexto del intento de autenticacion.
  - en el MVP existe una sola credencial local primaria activa por usuario.
- Entidades internas:
  - `UserCredential`, `UserLoginAttempt`.
- Transiciones clave:
  - Usuario: `ACTIVE -> BLOCKED|DISABLED`.
  - Credencial: `ACTIVE -> ROTATED|COMPROMISED`.
  - Contador de fallos: incrementa en login fallido y se resetea en login exitoso.
- Errores de dominio:
  - `usuario_no_habilitado`.
  - `credenciales_invalidas`.
  - `operacion_no_permitida`.

### SessionAggregate
_Esta subseccion detalla sessionaggregate dentro del contexto del documento._
- Proposito: gobernar el ciclo de vida de la sesion y de sus JTIs asociados.
- Invariantes:
  - la sesion pertenece a un `userId` y `tenantId` coherentes con el usuario autenticado.
  - una sesion `REVOKED` o `EXPIRED` no vuelve a `ACTIVE`.
  - la rotacion de tokens conserva trazabilidad entre JTIs viejo/nuevo.
- Entidades internas:
  - `SessionTimestamps`, `AccessJti`, `RefreshJti`.
- Transiciones clave:
  - `ACTIVE -> REVOKED` por logout, bloqueo, revocacion administrativa o incidente.
  - `ACTIVE -> EXPIRED` por vigencia temporal.
  - refresh exitoso mantiene la sesion y rota el par de tokens.
- Errores de dominio:
  - `token_expirado_o_revocado`.
  - `sesion_no_encontrada`.
  - `operacion_no_permitida`.

### RoleAggregate
_Esta subseccion detalla roleaggregate dentro del contexto del documento._
- Proposito: preservar la semantica de autorizacion del rol y sus permisos efectivos.
- Invariantes:
  - rol y permisos pertenecen al mismo tenant.
  - una asignacion activa solo puede vincular usuario y rol del mismo tenant.
  - no existen dos asignaciones activas equivalentes para el mismo `userId + roleId + tenantId`.
- Entidades relacionadas:
  - `RolePermission`, `UserRoleAssignment`.
- Transiciones clave:
  - asignacion de rol genera `RoleAssigned`.
  - revocacion de asignacion cambia el ciclo de vida a `REVOKED`.
- Errores de dominio:
  - `rol_invalido`.
  - `acceso_cruzado_detectado`.
  - `operacion_no_permitida`.

## Registros de apoyo
_Responde: que estructuras de soporte complementan la consistencia sin convertirse en agregados core._
- `user_role_assignment` mantiene identidad propia y ciclo de vida operativo, pero su razon de existir es sostener la autorizacion del usuario dentro del tenant.
- `auth_audit` conserva evidencia tecnica de login, bloqueo, revocacion y mutaciones administrativas.
- `outbox_event` y `processed_event` sostienen integracion confiable e idempotencia de consumidores.

## Reglas de consistencia
_Responde: que invariantes locales debe preservar este artefacto._
- Bloqueo de usuario implica revocacion de sesiones activas dentro del mismo caso de uso.
- Login, refresh, logout y mutaciones administrativas relevantes dejan evidencia tecnica y, cuando aplica, evento por outbox.
- Eventos de IAM no exponen datos sensibles de credencial ni material criptografico.
