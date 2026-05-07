---
title: "Anti-Corrupcion"
linkTitle: "8. Anti-Corrupcion"
weight: 8
url: "/mvp/dominio/contextos-delimitados/catalogo/anti-corrupcion/"
---

## Marco de traduccion
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Documentar ACLs semanticas entre `catalog` y BCs dependientes.

## Mapeos principales
_Responde: que traducciones principales hace el contexto al cruzar sus fronteras._
| Upstream/Downstream | Termino externo | Termino en catalog | Regla |
|---|---|---|---|
| `order -> catalog` | `resolver_variante_checkout` | `vendible + precio_vigente` | respuesta no incluye disponibilidad |
| `catalog -> order` | `PriceUpdated` | `price_snapshot` | snapshot se congela en confirmacion |
| `catalog -> inventory` | `VariantCreated/VariantUpdated` | `sku referenciada` | inventory sincroniza metadata operativa sin heredar precio |
| `catalog -> inventory` | `VariantDiscontinued/ProductRetired` | `sku bloqueada` | inventory marca no operable para reservas nuevas |
| `catalog -> reporting` | `ProductUpdated/PriceUpdated/PriceScheduled` | `contexto comercial` | reporting proyecta hechos sin mutar core |
| `inventory -> catalog` | `StockUpdated` | `availability_hint` | hint informativo, no verdad transaccional |

## Normalizacion de errores
_Responde: como traduce este contexto errores externos a su lenguaje canonico._
| Error tecnico | Error canonico de dominio |
|---|---|
| duplicate key sku | `sku_no_unico` |
| missing active product | `producto_no_activo` |
| invalid pricing period | `periodo_precio_solapado` |

## Reglas ACL
_Responde: que reglas gobiernan la capa ACL para evitar contaminacion semantica._
- `order` no usa `catalog` para decidir reservas; solo para vendible/precio.
- `catalog` no publica semantica de stock como autoridad.
- Consumidores deben ignorar campos desconocidos en eventos `catalog`.
