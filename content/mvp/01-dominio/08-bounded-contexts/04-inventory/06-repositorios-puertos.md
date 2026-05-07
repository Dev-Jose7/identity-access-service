---
title: "Repositorios y Puertos"
linkTitle: "7. Repositorios y Puertos"
weight: 7
url: "/mvp/dominio/contextos-delimitados/inventario/repositorios-puertos/"
---

## Marco de puertos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir puertos de entrada/salida y contratos de persistencia/integracion de `inventory`.

## Puertos de entrada (in)
_Responde: que puertos de entrada exponen casos de uso o capacidades del contexto._
| Port | Tipo | Metodos clave |
|---|---|---|
| `InventoryStockUseCasePort` | in | `inicializarStock`, `ajustarStock`, `incrementarStock`, `decrementarStock`, `ajusteMasivo` |
| `InventoryReservationUseCasePort` | in | `crearReserva`, `extenderReserva`, `confirmarReserva`, `liberarReserva`, `expirarReservas` |
| `InventoryValidationPort` | in | `validarReservasCheckout`, `consultarDisponibilidad`, `obtenerStockPorSku` |
| `InventoryReconcilePort` | in | `reconciliarSku` |

## Puertos de salida (out)
_Responde: que puertos de salida necesita el contexto para colaborar con otros sistemas._
| Port | Tipo | Metodos clave |
|---|---|---|
| `StockRepositoryPort` | out | `findBySku`, `save`, `listByWarehouse` |
| `ReservationRepositoryPort` | out | `findById`, `save`, `findExpirable`, `findByIds` |
| `StockMovementRepositoryPort` | out | `saveMovement` |
| `InventoryAuditPort` | out | `recordSuccess`, `recordFailure` |
| `DistributedLockPort` | out | `acquire`, `release` |
| `OutboxPort` | out | `append`, `listPending`, `markPublished` |
| `CatalogSyncPort` | out | `resolveVariantStatus` |

## Contratos de consistencia
_Responde: que contratos preservan consistencia entre puertos, repositorios y reglas locales._
- Mutaciones de stock/reserva persisten movimiento/auditoria/outbox en la misma unidad de negocio.
- Lock por `tenant+warehouse+sku` durante comandos criticos de reserva.
- Confirmacion de reserva rechaza estado no activo/no vigente.
