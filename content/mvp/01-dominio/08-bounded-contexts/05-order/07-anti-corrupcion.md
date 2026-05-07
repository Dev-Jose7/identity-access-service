---
title: "Anti-Corrupcion"
linkTitle: "8. Anti-Corrupcion"
weight: 8
url: "/mvp/dominio/contextos-delimitados/pedidos/anti-corrupcion/"
---

## Marco de traduccion
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Documentar ACLs semanticas de `order` con BCs externos.

## Mapeos principales
_Responde: que traducciones principales hace el contexto al cruzar sus fronteras._
| Upstream/Downstream | Termino externo | Termino en order | Regla |
|---|---|---|---|
| `inventory -> order` | `StockReserved/StockReservationExpired/StockReservationConfirmed` | `item_reservable/no_reservable/confirmado` | checkout usa estado traducido por item |
| `catalog -> order` | `PriceUpdated` | `price_snapshot` | snapshot inmutable al confirmar pedido |
| `directory -> order` | `AddressUpdated` | `address_snapshot_valida` | validacion en checkout antes de confirmar |
| `identity-access -> order` | `SessionRevoked/UserBlocked` | `operacion_no_permitida` | denegar mutaciones y cancelar segun politica |
| `order -> notification` | `OrderConfirmed/OrderPaymentRegistered` | `solicitud_notificacion` | no bloqueante |
| `order -> reporting` | `OrderConfirmed/OrderPaymentStatusUpdated` | `hecho_venta/hecho_cobro` | eventual consistency |

## Normalizacion de errores
_Responde: como traduce este contexto errores externos a su lenguaje canonico._
| Error tecnico | Error canonico de dominio |
|---|---|
| reservation validate timeout | `conflicto_checkout` |
| reservation expired | `reserva_expirada` |
| unauthorized tenant mismatch | `acceso_cruzado_detectado` |
| duplicate payment ref | `pago_duplicado` |

## Reglas ACL
_Responde: que reglas gobiernan la capa ACL para evitar contaminacion semantica._
- `order` no interpreta disponibilidad como vendible; usa `catalog` y `inventory` por separado.
- `order` no muta entidades de `directory`; solo valida y snapshot.
- consumidores de eventos `order` deben tolerar reintentos y duplicados.
