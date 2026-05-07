---
title: "Agregados"
linkTitle: "3. Agregados"
weight: 3
url: "/mvp/dominio/contextos-delimitados/inventario/agregados/"
---

## Marco de agregados
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir agregados e invariantes de `inventory`.

## Agregados
_Responde: que agregados protegen consistencia dentro del contexto._

### InventoryAggregate
_Esta subseccion detalla inventoryaggregate dentro del contexto del documento._
- Proposito: preservar integridad de stock por SKU/almacen.
- Invariantes:
  - `I-INV-01`: `stock_fisico >= 0`.
  - `I-INV-02`: `reservas_activas <= stock_fisico`.
- Entidades internas:
  - StockItem, MovimientoStock.
- Errores:
  - `stock_negativo_invalido`, `stock_insuficiente`.

### ReservationAggregate
_Esta subseccion detalla reservationaggregate dentro del contexto del documento._
- Proposito: garantizar reserva consistente para carrito/checkout.
- Invariantes:
  - reserva activa requiere TTL vigente.
  - confirmacion solo sobre reserva activa no expirada.
  - en MVP no hay reserva parcial.
- Transiciones:
  - `activa -> confirmada|expirada|liberada`.
- Errores:
  - `reserva_expirada`, `reserva_no_encontrada`, `conflicto_reserva`.

## Reglas de consistencia
_Responde: que invariantes locales debe preservar este artefacto._
- Ajustes de stock y reservas se auditan con movimiento y outbox.
- Confirmar reserva consume stock reservado y genera evento.
- Expiracion de reserva resta `reservas_activas` y notifica downstream.
