---
title: "Modelo"
linkTitle: "2. Modelo"
weight: 2
url: "/mvp/dominio/contextos-delimitados/catalogo/modelo/"
---

## Marco del modelo
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Modelo conceptual del BC `catalog`.

## Entidades principales
_Responde: que entidades estructuran el modelo local._
- Producto.
- Variante SKU.
- Atributo de variante.
- Precio.
- Programacion de precio.

Nota de modelado:
- `Marca` y `Categoria` existen como taxonomia referencial local de `catalog`.
- En `MVP` no se gobiernan como agregados ni comandos CRUD independientes expuestos por API; se validan como referencias activas al registrar o actualizar producto.

## Value objects principales
_Responde: que objetos de valor expresan reglas relevantes sin identidad propia._
- `SkuRef`.
- `Money`.
- `SellabilityWindow`.
- `PricePeriod`.
- `CategoryRef` / `BrandRef`.

## Estados importantes
_Responde: que estados son relevantes para entender el ciclo local._
| Entidad | Estados permitidos | Estado inicial | Terminales |
|---|---|---|---|
| Producto | `DRAFT`, `ACTIVE`, `RETIRED` | `DRAFT` | `RETIRED` |
| Variante | `DRAFT`, `SELLABLE`, `DISCONTINUED` | `DRAFT` | `DISCONTINUED` |
| Precio | `ACTIVE`, `SCHEDULED`, `EXPIRED` | `ACTIVE|SCHEDULED` | `EXPIRED` |

Nota de vocabulario:
- `PriceAggregate` usa solo `ACTIVE`, `SCHEDULED`, `EXPIRED`.
- `PENDING`, `EXECUTED`, `FAILED`, `CANCELLED` pertenecen a `price_schedule` como estado tecnico de agenda, no al ciclo de vida del precio.

## Reglas de negocio nucleo
_Responde: que reglas de negocio sostienen el modelo del contexto._
- SKU unico por tenant para variante vendible (`I-CAT-01`).
- Producto requiere `brand/category` activas dentro de la taxonomia referencial local.
- Variante vendible requiere producto `ACTIVE`.
- Precio vigente se resuelve por periodo no solapado.
- Descontinuar variante invalida vendible y precio futuro incompatible.

## Identidad de agregados
_Responde: como se identifica cada agregado relevante del contexto._
- `ProductAggregate(productId, tenantId, status, variants[])`.
- `VariantAggregate(variantId, sku, sellableState, attributes[])`.
- `PriceAggregate(priceId, variantId, amount, currency, effectiveWindow)`.
