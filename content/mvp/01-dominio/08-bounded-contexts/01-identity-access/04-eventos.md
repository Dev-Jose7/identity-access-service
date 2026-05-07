---
title: "Eventos"
linkTitle: "5. Eventos"
weight: 5
url: "/mvp/dominio/contextos-delimitados/identidad-acceso/eventos/"
---

## Marco de eventos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Eventos emitidos/consumidos por `identity-access`.

## Eventos emitidos
_Responde: que hechos publica este contexto para otros consumidores._

### UserLoggedIn
_Esta subseccion detalla userloggedin dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `correlationId`, `tenantId`, `userId`, `sessionId`, `roles`, `expiresAt`.
- Semantica:
  - significa autenticacion exitosa con sesion activa.
  - no significa privilegio irrestricto.
- Consumidores:
  - `reporting`, `security-monitoring`.
- Idempotencia/orden:
  - dedupe por `eventId`; orden por `userId`.

### AuthFailed
_Esta subseccion detalla authfailed dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `tenantId`, `emailHash`, `reasonCode`, `ip`.
- Semantica:
  - significa intento fallido de autenticacion.
  - no significa bloqueo automatico permanente.
- Consumidores:
  - `security-monitoring`.
- Idempotencia:
  - dedupe por `eventId`.

### SessionRefreshed
_Esta subseccion detalla sessionrefreshed dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `tenantId`, `sessionId`, `userId`, `oldAccessJti`, `newAccessJti`.
- Semantica:
  - rotacion valida del par de tokens de una sesion vigente.
- Consumidores:
  - sin consumidores externos obligatorios en MVP.

### SessionRevoked
_Esta subseccion detalla sessionrevoked dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `tenantId`, `sessionId`, `userId`, `reasonCode`.
- Semantica:
  - significa sesion invalida para nuevos comandos.
- Consumidores:
  - `api-gateway-service`, `reporting`, `order` cuando el flujo protegido requiere reaccion asincrona local.

### SessionsRevokedByUser
_Esta subseccion detalla sessionsrevokedbyuser dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `tenantId`, `userId`, `revokedCount`, `reason`.
- Semantica:
  - revocacion masiva de sesiones activas por usuario.
- Consumidores:
  - `api-gateway-service`, `security-monitoring`.

### RoleAssigned
_Esta subseccion detalla roleassigned dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `userId`, `roleId`, `roleCode`, `changedBy`.
- Semantica:
  - cambio efectivo de autorizacion.
- Consumidores:
  - `order`, `directory`, `reporting`.

### UserBlocked
_Esta subseccion detalla userblocked dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `userId`, `reason`, `revokedSessions`.
- Semantica:
  - usuario bloqueado operativamente y sesiones revocadas.
- Consumidores:
  - `order`, `notification`, `reporting`, `security-monitoring`.

## Eventos consumidos
_Responde: que hechos externos consume este contexto y hasta donde puede reaccionar._
- `OrganizationSuspended` (desde `directory`) para bloquear usuarios del tenant referenciado y cerrar acceso operativo.
