---
title: "Modelo"
linkTitle: "2. Modelo"
weight: 2
url: "/mvp/dominio/contextos-delimitados/pedidos/modelo/"
---

## Marco del modelo
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Modelo conceptual del BC `order`.

## Entidades principales
_Responde: que entidades estructuran el modelo local._
- Carrito.
- Item de carrito.
- Intento de checkout.
- Pedido.
- Linea de pedido.
- Pago registrado.
- Historial de estado.

## Value objects principales
_Responde: que objetos de valor expresan reglas relevantes sin identidad propia._
- `CheckoutCorrelationId`.
- `PriceSnapshot`.
- `AddressSnapshot`.
- `Money`.
- `ReservationRef`.

## Estados importantes
_Responde: que estados son relevantes para entender el ciclo local._
| Entidad/Agregado | Estados permitidos | Inicial | Terminales |
|---|---|---|---|
| Carrito | `ACTIVE`, `CHECKOUT_IN_PROGRESS`, `CONVERTED`, `ABANDONED`, `CANCELLED` | `ACTIVE` | `CONVERTED`, `ABANDONED`, `CANCELLED` |
| IntentoCheckout | `INVALID`, `VALID` | `INVALID` | `VALID`, `INVALID` |
| Pedido | `CREATED (interno)`, `PENDING_APPROVAL`, `CONFIRMED`, `CANCELLED`, `READY_TO_DISPATCH (RESERVED)`, `DISPATCHED (RESERVED)`, `DELIVERED (RESERVED)` | `CREATED (interno)` | `CANCELLED` (MVP), `DELIVERED` (RESERVED) |
| Estado de pago | `PENDING`, `PARTIALLY_PAID`, `PAID`, `OVERPAID_REVIEW` | `PENDING` | `PAID`, `OVERPAID_REVIEW` |

Semantica MVP del ciclo de pedido:
- `CREATED` se usa solo como estado transitorio interno durante la construccion del agregado.
- `confirmar_pedido` crea pedido en `PENDING_APPROVAL`.
- `confirmar_aprobacion_pedido` mueve `PENDING_APPROVAL -> CONFIRMED`.
- estados de fulfillment (`READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED`) quedan reservados para evolucion.

## Reglas de negocio nucleo
_Responde: que reglas de negocio sostienen el modelo del contexto._
- Pedido confirmado solo con reservas confirmables (`I-ORD-01`).
- Transiciones de pedido siguen politica de ciclo (`I-ORD-02`).
- Referencia de pago duplicada no duplica efecto (`RN-PAY-02`).
- Carrito abandonado se detecta por ventana configurada.

## Identidad de agregados
_Responde: como se identifica cada agregado relevante del contexto._
- `CartAggregate(cartId, tenantId, organizationId, userId, items[], status)`.
- `OrderAggregate(orderId, orderNumber, checkoutCorrelationId, lines[], orderState, paymentState)`.
- `PaymentAggregate(orderId, paymentRecords[], paymentState)`.
