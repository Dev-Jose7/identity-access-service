---
title: "Anti-Corrupcion"
linkTitle: "8. Anti-Corrupcion"
weight: 8
url: "/mvp/dominio/contextos-delimitados/inventario/anti-corrupcion/"
---

## Marco de traduccion
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Documentar ACLs semanticas de `inventory` con BCs externos.

## Mapeos principales
_Responde: que traducciones principales hace el contexto al cruzar sus fronteras._
| Upstream/Downstream | Termino externo | Termino en inventory | Regla |
|---|---|---|---|
| `catalog -> inventory` | `VariantCreated/VariantUpdated` | `sku referenciada` | sincroniza metadata operativa necesaria para stock/reserva |
| `catalog -> inventory` | `VariantDiscontinued/ProductRetired` | `sku bloqueada` | rechaza nuevas reservas |
| `order -> inventory` | `confirmar_pedido` | `confirmar_reserva` | consume reserva activa por `orderId` |
| `inventory -> order` | `StockReservationExpired` | `item_no_reservable` | carrito debe ajustarse |
| `inventory -> reporting` | `StockUpdated` | `hecho_abastecimiento` | derivado analitico |

## Normalizacion de errores
_Responde: como traduce este contexto errores externos a su lenguaje canonico._
| Error tecnico | Error canonico de dominio |
|---|---|
| lock timeout | `contencion_stock` |
| insufficient availability | `stock_insuficiente` |
| expired reservation status | `reserva_expirada` |
| negative stock mutation | `stock_negativo_invalido` |

## Reglas ACL
_Responde: que reglas gobiernan la capa ACL para evitar contaminacion semantica._
- `order` no muta directamente `stock_fisico`; solo via comandos de reserva/confirmacion/liberacion.
- `catalog` no determina disponibilidad reservable.
- `reporting` consume eventos sin mutar tablas de inventory.
