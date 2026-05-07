---
title: "Eventos"
linkTitle: "5. Eventos"
weight: 5
url: "/mvp/dominio/contextos-delimitados/inventario/eventos/"
---

## Marco de eventos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Eventos emitidos/consumidos por `inventory`.

## Eventos emitidos
_Responde: que hechos publica este contexto para otros consumidores._

### StockInitialized
_Esta subseccion detalla stockinitialized dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `stockId`, `warehouseId`, `sku`, `initialQty`.
- Consumidores:
  - `reporting`.

### StockAdjusted
_Esta subseccion detalla stockadjusted dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `stockId`, `sku`, `fromQty`, `toQty`, `reason`.
- Consumidores:
  - `reporting`.

### StockIncreased
_Esta subseccion detalla stockincreased dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `stockId`, `sku`, `qty`, `newPhysicalQty`.
- Consumidores:
  - `reporting`.

### StockDecreased
_Esta subseccion detalla stockdecreased dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `stockId`, `sku`, `qty`, `newPhysicalQty`.
- Consumidores:
  - `reporting`.

### StockUpdated
_Esta subseccion detalla stockupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `traceId`, `correlationId`, `stockId`, `warehouseId`, `sku`, `physicalQty`, `reservedQty`, `availableQty`.
- Semantica:
  - snapshot coherente de stock tras mutacion.
- Consumidores:
  - `order`, `catalog`, `reporting`.

### StockReserved
_Esta subseccion detalla stockreserved dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `reservationId`, `stockId`, `sku`, `qty`, `expiresAt`, `cartId`.
- Consumidores:
  - `order`, `reporting`.

### StockReservationExtended
_Esta subseccion detalla stockreservationextended dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `reservationId`, `sku`, `newExpiresAt`.
- Consumidores:
  - `order`.

### StockReservationConfirmed
_Esta subseccion detalla stockreservationconfirmed dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `reservationId`, `orderId`, `qty`.
- Consumidores:
  - `order`, `reporting`.

### StockReservationReleased
_Esta subseccion detalla stockreservationreleased dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `reservationId`, `reasonCode`, `qty`.
- Consumidores:
  - `order`, `reporting`.

### StockReservationExpired
_Esta subseccion detalla stockreservationexpired dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `reservationId`, `sku`, `qty`, `reasonCode`.
- Consumidores:
  - `order`, `notification`, `reporting`.

### SkuReconciled
_Esta subseccion detalla skureconciled dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `sku`, `result`, `sourceEvent`.
- Consumidores:
  - `catalog`, `reporting`.

### LowStockDetected
_Esta subseccion detalla lowstockdetected dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `stockId`, `sku`, `availableQty`, `reorderPoint`.
- Consumidores:
  - `notification`, `reporting`.

## Eventos consumidos
_Responde: que hechos externos consume este contexto y hasta donde puede reaccionar._
- `VariantCreated`, `VariantUpdated`, `VariantDiscontinued`, `ProductRetired` (desde `catalog`).
- `solicitar_validacion_checkout` (comando sync interno desde `order`) para validacion de lote de reservas.
- `inventory` no consume eventos `OrderCreated` ni `OrderCancelled` en `MVP`; la integracion con `order` es sync para reserva/confirmacion/liberacion/validacion y async solo desde `inventory` hacia `order`.
