---
title: "Eventos"
linkTitle: "5. Eventos"
weight: 5
url: "/mvp/dominio/contextos-delimitados/notificaciones/eventos/"
---

## Marco de eventos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Eventos emitidos/consumidos por `notification`.

## Eventos emitidos
_Responde: que hechos publica este contexto para otros consumidores._

### NotificationRequested
_Esta subseccion detalla notificationrequested dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `correlationId`, `tenantId`, `notificationId`, `sourceEventType`, `channel`, `recipientRef`.
- Semantica:
  - solicitud valida registrada.
- Consumidores:
  - `reporting`.

### NotificationSent
_Esta subseccion detalla notificationsent dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `notificationId`, `providerRef`, `attemptCount`.
- Semantica:
  - envio exitoso.
- Consumidores:
  - `reporting`.

### NotificationFailed
_Esta subseccion detalla notificationfailed dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `notificationId`, `errorCode`, `attemptCount`, `retryable`.
- Semantica:
  - fallo de envio no bloqueante del core.
- Consumidores:
  - `reporting`, `order`.

### NotificationDiscarded
_Esta subseccion detalla notificationdiscarded dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `notificationId`, `reasonCode`, `attemptCount`.
- Semantica:
  - solicitud descartada por no recuperable o max retries.
- Consumidores:
  - `reporting`.

## Eventos consumidos
_Responde: que hechos externos consume este contexto y hasta donde puede reaccionar._
- `OrderConfirmed`, `OrderStatusChanged`, `OrderPaymentRegistered`, `CartAbandonedDetected` (desde `order`).
- `StockReservationExpired`, `LowStockDetected` (desde `inventory`).
- `OrganizationProfileUpdated`, `ContactRegistered`, `ContactUpdated`, `PrimaryContactChanged` (desde `directory`) para resolver destinatario institucional y contexto operativo del tenant.
- `WeeklyReportGenerated` (desde `reporting`).
- `UserBlocked` (desde `identity-access`) para comunicaciones de seguridad si aplica.
