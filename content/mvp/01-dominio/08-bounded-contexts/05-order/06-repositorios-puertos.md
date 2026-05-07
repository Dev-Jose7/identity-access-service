---
title: "Repositorios y Puertos"
linkTitle: "7. Repositorios y Puertos"
weight: 7
url: "/mvp/dominio/contextos-delimitados/pedidos/repositorios-puertos/"
---

## Marco de puertos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir puertos de entrada/salida y contratos de persistencia/integracion de `order`.

## Puertos de entrada (in)
_Responde: que puertos de entrada exponen casos de uso o capacidades del contexto._
| Port | Tipo | Metodos clave |
|---|---|---|
| `CartUseCasePort` | in | `obtenerCarritoActivo`, `agregarOActualizarItem`, `removerItem`, `limpiarCarrito` |
| `CheckoutUseCasePort` | in | `solicitarValidacionCheckout`, `confirmarPedido` |
| `OrderCicloDeVidaUseCasePort` | in | `cancelarPedido`, `cambiarEstadoPedido` |
| `PaymentUseCasePort` | in | `registrarPagoManual`, `listarPagosPedido` |
| `OrderQueryPort` | in | `listarPedidos`, `obtenerDetallePedido`, `obtenerTimeline` |
| `OrderSchedulerPort` | in | `detectarCarritosAbandonados` |

## Puertos de salida (out)
_Responde: que puertos de salida necesita el contexto para colaborar con otros sistemas._
| Port | Tipo | Metodos clave |
|---|---|---|
| `CartRepositoryPort` | out | `findActiveByUser`, `save`, `findCandidatesForAbandonment` |
| `CartItemRepositoryPort` | out | `findByCartId`, `save`, `removeById`, `findByReservationId` |
| `CheckoutAttemptRepositoryPort` | out | `findByCorrelationId`, `save` |
| `OrderRepositoryPort` | out | `findById`, `save`, `findByCheckoutCorrelation` |
| `OrderLineRepositoryPort` | out | `saveAll`, `findByOrderId` |
| `PaymentRepositoryPort` | out | `existsByReference`, `save`, `sumValidatedAmount`, `listByOrderId` |
| `OrderStatusHistoryPort` | out | `append`, `listByOrderId` |
| `OrderAuditPort` | out | `recordSuccess`, `recordFailure` |
| `InventoryClientPort` | out | `createOrExtendReservation`, `validateReservations`, `confirmReservation`, `releaseReservation` |
| `CatalogClientPort` | out | `resolveVariant`, `getCurrentPrice` |
| `DirectoryClientPort` | out | `validateAddress` |
| `IdentityAccessClientPort` | out | `validateSessionPermissions` |
| `OutboxPort` | out | `append`, `listPending`, `markPublished` |

## Contratos de consistencia
_Responde: que contratos preservan consistencia entre puertos, repositorios y reglas locales._
- `confirmarPedido` persiste pedido/lineas/historial y outbox de forma consistente.
- `registrarPagoManual` recalcula estado de pago en la misma unidad de negocio.
- mutaciones criticas requieren `Idempotency-Key` valido.
