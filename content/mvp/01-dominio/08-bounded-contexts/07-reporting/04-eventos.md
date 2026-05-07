---
title: "Eventos"
linkTitle: "5. Eventos"
weight: 5
url: "/mvp/dominio/contextos-delimitados/reporteria/eventos/"
---

## Marco de eventos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Eventos emitidos/consumidos por `reporting`.

## Eventos emitidos
_Responde: que hechos publica este contexto para otros consumidores._

### AnalyticFactUpdated
_Esta subseccion detalla analyticfactupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `sourceEventId`, `factType`, `factPayload`.
- Semantica:
  - hecho derivado incorporado a vista.
- Consumidores:
  - consumo interno de reporting.

### WeeklyReportGenerated
_Esta subseccion detalla weeklyreportgenerated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `weekId`, `reportType`, `locationRef`.
- Semantica:
  - reporte generado para periodo y tipo.
- Consumidores:
  - `notification`.

## Eventos consumidos
_Responde: que hechos externos consume este contexto y hasta donde puede reaccionar._
- `OrderCreated`, `OrderConfirmed`, `OrderStatusChanged`, `OrderPaymentRegistered`, `OrderPaymentStatusUpdated`, `CartAbandonedDetected` (desde `order`).
- `StockUpdated`, `StockReserved`, `StockReservationExpired`, `LowStockDetected` (desde `inventory`).
- `ProductUpdated`, `PriceUpdated`, `PriceScheduled` (desde `catalog`).
- `OrganizationProfileUpdated`, `CountryOperationalPolicyConfigured`, `ContactRegistered`, `ContactUpdated`, `PrimaryContactChanged` (desde `directory`).
- `NotificationRequested`, `NotificationSent`, `NotificationFailed`, `NotificationDiscarded` (desde `notification`).
