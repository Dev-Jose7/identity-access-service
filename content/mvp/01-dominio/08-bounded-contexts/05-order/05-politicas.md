---
title: "Politicas"
linkTitle: "6. Politicas"
weight: 6
url: "/mvp/dominio/contextos-delimitados/pedidos/politicas/"
---

## Marco de politicas
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Politicas de reaccion de `order`.

## Politicas (if event -> then command)
_Responde: que reacciones automatizadas ejecuta el contexto frente a eventos relevantes._
| Trigger (evento) | Condicion | Accion (comando) | Observaciones |
|---|---|---|---|
| `StockReservationExpired` (inventory) | reserva pertenece a item activo | `remover_item_carrito` o marcar `item_invalido` | evita confirmacion invalida |
| `VariantDiscontinued` (catalog) | item en carrito con SKU afectada | `remover_item_carrito` | evita checkout de SKU no operable |
| `CheckoutAddressValidated` (directory) | resultado asincrono de validacion recibido para `organizationId + addressId` | `actualizar_estado_validacion_checkout` | cierra la conciliacion asincrona de direccion sin reemplazar la validacion sync inicial |
| `UserBlocked` (identity-access) | usuario con pedidos no terminales | `cancelar_pedido` (segun politica) | contencion de riesgo |
| `OrderConfirmed` | pedido confirmado comercialmente | `solicitar_notificacion` (notification) | no bloqueante |
| `OrderPaymentRegistered` | pago valido | `solicitar_notificacion` (notification) | comunica avance de cobro |
| `timer_carrito` | inactividad > umbral | `detectar_carritos_abandonados` | habilita FR-008 |

## Retries / compensacion
_Responde: como maneja este contexto reintentos y compensaciones sin romper su modelo._
- Retry de publicacion de eventos: 3 intentos con backoff.
- Compensacion: si falla checkout en validacion final, no crear pedido parcial.

## Timeouts
_Responde: que limites temporales gobiernan las interacciones de este contexto._
- validacion checkout (dependencias externas): 2 s total.
- registrar pago manual: 1 s objetivo.
