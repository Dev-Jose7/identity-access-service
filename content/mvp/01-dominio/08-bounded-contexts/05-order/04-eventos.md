---
title: "Eventos"
linkTitle: "5. Eventos"
weight: 5
url: "/mvp/dominio/contextos-delimitados/pedidos/eventos/"
---

## Marco de eventos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Eventos emitidos/consumidos por `order`.

## Eventos emitidos
_Responde: que hechos publica este contexto para otros consumidores._

### CartItemAdded
_Esta subseccion detalla cartitemadded dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `correlationId`, `tenantId`, `cartId`, `cartItemId`, `sku`, `qty`, `reservationId`.
- Consumidores:
  - `reporting`.

### CartItemUpdated
_Esta subseccion detalla cartitemupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `correlationId`, `tenantId`, `cartId`, `cartItemId`, `sku`, `qty`, `reservationId`.
- Consumidores:
  - `reporting`.

### CartItemRemoved
_Esta subseccion detalla cartitemremoved dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `cartId`, `cartItemId`, `sku`, `reason`.
- Consumidores:
  - `reporting`.

### CartCleared
_Esta subseccion detalla cartcleared dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `cartId`, `reason`.
- Consumidores:
  - `reporting`.

### OrderCheckoutValidationFailed
_Esta subseccion detalla ordercheckoutvalidationfailed dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `checkoutCorrelationId`, `reasonCodes`, `cartId`.
- Consumidores:
  - `reporting`.

### OrderCreated
_Esta subseccion detalla ordercreated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `correlationId`, `tenantId`, `orderId`, `orderNumber`, `organizationId`, `status`, `paymentStatus`, `totals`.
- Semantica:
  - pedido creado en `PENDING_APPROVAL` con reservas confirmadas.
  - no significa confirmacion comercial final.
- Consumidores:
  - `inventory`, `reporting`.

### OrderConfirmed
_Esta subseccion detalla orderconfirmed dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `correlationId`, `tenantId`, `orderId`, `previousStatus`, `newStatus`, `confirmedBy`.
- Semantica:
  - transicion comercial final `PENDING_APPROVAL -> CONFIRMED`.
  - no significa despacho o entrega final.
- Consumidores:
  - `notification`, `reporting`.

### OrderStatusChanged
_Esta subseccion detalla orderstatuschanged dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `orderId`, `previousStatus`, `newStatus`, `reason`.
- Consumidores:
  - `notification`, `reporting`.

### OrderCancelled
_Esta subseccion detalla ordercancelled dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `orderId`, `reason`.
- Consumidores:
  - `inventory`, `notification`, `reporting`.

### OrderPaymentRegistered
_Esta subseccion detalla orderpaymentregistered dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `orderId`, `paymentId`, `paymentReference`, `method`, `amount`, `currency`.
- Consumidores:
  - `notification`, `reporting`.

### OrderPaymentStatusUpdated
_Esta subseccion detalla orderpaymentstatusupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `orderId`, `paymentStatus`, `paidAmount`, `pendingAmount`.
- Consumidores:
  - `notification`, `reporting`.

### CartAbandonedDetected
_Esta subseccion detalla cartabandoneddetected dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `cartId`, `organizationId`, `itemCount`, `thresholdMinutes`.
- Consumidores:
  - `notification`, `reporting`.

## Eventos consumidos
_Responde: que hechos externos consume este contexto y hasta donde puede reaccionar._
- `StockReservationExpired` / `StockReservationConfirmed` (desde `inventory`).
- `VariantDiscontinued` / `PriceUpdated` (desde `catalog`).
- `CheckoutAddressValidated` (desde `directory`).
- `UserBlocked` (desde `identity-access`).
