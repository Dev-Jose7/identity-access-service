---
title: "Eventos"
linkTitle: "5. Eventos"
weight: 5
url: "/mvp/dominio/contextos-delimitados/catalogo/eventos/"
---

## Marco de eventos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Eventos emitidos/consumidos por `catalog`.

## Eventos emitidos
_Responde: que hechos publica este contexto para otros consumidores._

### ProductCreated
_Esta subseccion detalla productcreated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `productId`, `productCode`, `name`, `status`.
- Consumidores:
  - sin consumidores externos activos en `MVP`.

### ProductUpdated
_Esta subseccion detalla productupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `productId`, `status`, `changedFields`.
- Semantica:
  - cambio valido de producto.
- Consumidores:
  - `reporting`.

### ProductActivated
_Esta subseccion detalla productactivated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `productId`, `status`, `activatedAt`.
- Consumidores:
  - sin consumidores externos activos en `MVP`.

### ProductRetired
_Esta subseccion detalla productretired dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `productId`, `status`, `reason`, `retiredAt`.
- Consumidores:
  - `inventory`, `notification`.

### VariantCreated
_Esta subseccion detalla variantcreated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `variantId`, `productId`, `sku`, `status`.
- Consumidores:
  - `inventory`.

### VariantUpdated
_Esta subseccion detalla variantupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `variantId`, `productId`, `changedFields`.
- Consumidores:
  - `inventory`.

### VariantSellabilityChanged
_Esta subseccion detalla variantsellabilitychanged dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `variantId`, `sku`, `sellable`, `sellableFrom`, `sellableUntil`.
- Semantica:
  - cambio de vendible de variante.
- Consumidores:
  - sin consumidores externos activos en `MVP`.

### VariantDiscontinued
_Esta subseccion detalla variantdiscontinued dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `variantId`, `sku`, `reason`, `discontinuedAt`.
- Consumidores:
  - `order`, `inventory`, `notification`.

### PriceUpdated
_Esta subseccion detalla priceupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `priceId`, `variantId`, `sku`, `amount`, `currency`, `effectiveFrom`.
- Consumidores:
  - `order`, `reporting`.

### PriceScheduled
_Esta subseccion detalla pricescheduled dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `scheduleId`, `variantId`, `amount`, `currency`, `effectiveFrom`, `effectiveUntil`.
- Consumidores:
  - `reporting`.

## Eventos consumidos
_Responde: que hechos externos consume este contexto y hasta donde puede reaccionar._
- `StockUpdated` (desde `inventory`) para hint operativo de catalogo.
- `SkuReconciled` (desde `inventory`) para coherencia de estado de variante.
