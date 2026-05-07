---
title: "Politicas"
linkTitle: "6. Politicas"
weight: 6
url: "/mvp/dominio/contextos-delimitados/inventario/politicas/"
---

## Marco de politicas
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Politicas de reaccion de `inventory`.

## Politicas (if event -> then command)
_Responde: que reacciones automatizadas ejecuta el contexto frente a eventos relevantes._
| Trigger (evento) | Condicion | Accion (comando) | Observaciones |
|---|---|---|---|
| `VariantDiscontinued` (catalog) | SKU activa en inventory | `reconciliar_sku` (bloquear) | evita nuevas reservas |
| `ProductRetired` (catalog) | SKU asociada al producto sigue operativa | `reconciliar_sku` (bloquear) | bloquea operacion nueva sobre SKUs del producto |
| `timer_reserva_vencida` | TTL vencido | `expirar_reservas` | lote configurable por entorno |

Nota de aplicacion:
- `inventory` no consume `OrderCreated` ni `OrderCancelled` en `MVP`.
- La validacion de checkout y el ciclo de reservas con `order` se resuelven por contratos sync; los eventos de `inventory` hacia `order` quedan para expiracion, confirmacion y trazabilidad operacional.

## Retries / compensacion
_Responde: como maneja este contexto reintentos y compensaciones sin romper su modelo._
- Retry de consumo/publicacion: 3-5 intentos con backoff+jitter.
- Compensacion: si publicacion falla, mantener outbox pendiente y reintentar.

## Timeouts
_Responde: que limites temporales gobiernan las interacciones de este contexto._
- crear reserva: 300 ms objetivo.
- confirmar reserva: 500 ms objetivo.
- validacion checkout: 300 ms objetivo por lote.
