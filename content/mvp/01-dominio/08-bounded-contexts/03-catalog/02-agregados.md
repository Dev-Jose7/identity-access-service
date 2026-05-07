---
title: "Agregados"
linkTitle: "3. Agregados"
weight: 3
url: "/mvp/dominio/contextos-delimitados/catalogo/agregados/"
---

## Marco de agregados
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir agregados e invariantes de `catalog`.

## Agregados
_Responde: que agregados protegen consistencia dentro del contexto._

### ProductAggregate
_Esta subseccion detalla productaggregate dentro del contexto del documento._
- Proposito: controlar ciclo de vida comercial del producto.
- Invariantes:
  - producto retirado no admite variantes vendibles nuevas.
  - activacion exige metadata minima completa.
- Errores:
  - `producto_no_activo`, `producto_retirado`.

### VariantAggregate
_Esta subseccion detalla variantaggregate dentro del contexto del documento._
- Proposito: garantizar identidad y estado vendible de SKU.
- Invariantes:
  - `I-CAT-01`: SKU unico por tenant.
  - `SELLABLE` requiere producto `ACTIVE`.
  - variante `DISCONTINUED` no vuelve a `SELLABLE` en MVP.
- Errores:
  - `sku_no_unico`, `variante_no_vendible`, `variante_descontinuada`.

### PriceAggregate
_Esta subseccion detalla priceaggregate dentro del contexto del documento._
- Proposito: preservar precio vigente/programado sin ambiguedad.
- Invariantes:
  - periodos de precio no se solapan por `variantId+currency+priceType`.
  - monto de precio > 0.
- Errores:
  - `precio_invalido`, `periodo_precio_solapado`.

## Reglas de consistencia
_Responde: que invariantes locales debe preservar este artefacto._
- Cambios mutantes registran auditoria y outbox.
- `order` consume `price_snapshot` al confirmar, no consulta retrospectiva.
- Eventos de catalogo deben incluir `tenantId`, `traceId`, `correlationId`.
