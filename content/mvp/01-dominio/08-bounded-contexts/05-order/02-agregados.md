---
title: "Agregados"
linkTitle: "3. Agregados"
weight: 3
url: "/mvp/dominio/contextos-delimitados/pedidos/agregados/"
---

## Marco de agregados
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir agregados e invariantes de `order`.

## Agregados
_Responde: que agregados protegen consistencia dentro del contexto._

### CartAggregate
_Esta subseccion detalla cartaggregate dentro del contexto del documento._
- Proposito: preservar consistencia de intencion de compra antes de confirmar.
- Invariantes:
  - carrito pertenece a una sola organizacion.
  - cada item activo conserva `reservationRef` valido o por validar.
  - no mezcla variantes de tenants distintos.
- Transiciones:
  - `ACTIVE -> CONVERTED` por `confirmar_pedido`.
  - `ACTIVE -> ABANDONED` por politica de inactividad.
- Errores:
  - `cart_not_found`, `item_invalido`, `acceso_cruzado_detectado`.

### OrderAggregate
_Esta subseccion detalla orderaggregate dentro del contexto del documento._
- Proposito: consolidar compromiso de compra y ciclo de pedido.
- Invariantes:
  - `I-ORD-01`: pedido en `PENDING_APPROVAL` o `CONFIRMED` implica reservas confirmadas para todas las lineas.
  - `I-ORD-02`: transiciones de estado solo por politica valida.
  - snapshots de direccion/precio inmutables post-confirmacion.
- Transiciones:
  - `CREATED (interno) -> PENDING_APPROVAL -> CONFIRMED`.
  - `PENDING_APPROVAL|CONFIRMED -> CANCELLED`.
  - `READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED` quedan `RESERVED` fuera de baseline MVP.
  - En MVP no se permite bypass hacia `CONFIRMED`; toda confirmacion comercial pasa por `PENDING_APPROVAL`.
- Errores:
  - `conflicto_checkout`, `reserva_expirada`, `transicion_estado_invalida`.

### PaymentAggregate
_Esta subseccion detalla paymentaggregate dentro del contexto del documento._
- Proposito: reflejar avance de cobro del pedido en MVP.
- Invariantes:
  - `RN-PAY-01`: monto > 0.
  - `RN-PAY-02`: `paymentReference` unica por pedido.
  - `I-PAY-01`: estado de pago deriva de pagos validos vs total.
- Errores:
  - `pago_duplicado`, `monto_pago_invalido`, `estado_pago_inconsistente`.

## Reglas de consistencia
_Responde: que invariantes locales debe preservar este artefacto._
- Confirmacion de pedido y persistencia de lineas/historial deben ser consistentes.
- Falla de publicacion de evento no revierte mutacion de core (outbox pendiente).
- Eventos de ajuste de carrito por reserva expirada/descontinuacion son idempotentes.
