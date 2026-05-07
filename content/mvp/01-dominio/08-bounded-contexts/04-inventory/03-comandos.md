---
title: "Comandos"
linkTitle: "4. Comandos"
weight: 4
url: "/mvp/dominio/contextos-delimitados/inventario/comandos/"
---

## Marco de comandos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Catalogo de comandos de `inventory`.

## Lista de comandos
_Responde: que comandos admite el contexto y con que efecto semantico._

### inicializar_stock
_Esta subseccion detalla inicializar_stock dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `warehouseId`, `sku`, `initialQty`, `reorderPoint`, `idempotencyKey`.
- Precondiciones:
  - SKU operable y almacen valido.
- Postcondiciones:
  - stock base creado.

### ajustar_stock
_Esta subseccion detalla ajustar_stock dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `warehouseId`, `sku`, `targetQty`, `reasonCode`, `sourceRef`, `idempotencyKey`.
- Precondiciones:
  - `targetQty >= 0` y `targetQty >= reservas_activas`.
- Postcondiciones:
  - stock ajustado y movimiento registrado.
- Errores:
  - `stock_negativo_invalido`.

### incrementar_stock
_Esta subseccion detalla incrementar_stock dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `warehouseId`, `sku`, `qty`, `reasonCode`, `idempotencyKey`.

### decrementar_stock
_Esta subseccion detalla decrementar_stock dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `warehouseId`, `sku`, `qty`, `reasonCode`, `idempotencyKey`.
- Precondiciones:
  - resultado final no negativo.

### crear_reserva
_Esta subseccion detalla crear_reserva dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `warehouseId`, `sku`, `cartId`, `qty`, `ttlMinutes`, `idempotencyKey`.
- Precondiciones:
  - disponibilidad suficiente.
- Postcondiciones:
  - reserva `ACTIVE` con TTL.
- Errores:
  - `stock_insuficiente`.

### extender_reserva
_Esta subseccion detalla extender_reserva dentro del contexto del documento._
- Input esperado:
  - `reservationId`, `newTtl`, `idempotencyKey`.
- Precondiciones:
  - reserva activa.

### confirmar_reserva
_Esta subseccion detalla confirmar_reserva dentro del contexto del documento._
- Input esperado:
  - `reservationId`, `orderId`, `correlationId`, `idempotencyKey`.
- Precondiciones:
  - reserva activa no expirada.
- Postcondiciones:
  - reserva `CONFIRMED` + stock reservado consumido.
- Errores:
  - `reserva_expirada`.

### liberar_reserva
_Esta subseccion detalla liberar_reserva dentro del contexto del documento._
- Input esperado:
  - `reservationId`, `reasonCode`, `idempotencyKey`.

### expirar_reservas
_Esta subseccion detalla expirar_reservas dentro del contexto del documento._
- Input esperado:
  - `batchSize`, `tenantScope`, `operationRef`, `idempotencyKey`.
- Postcondiciones:
  - reservas vencidas movidas a `EXPIRED`.

### validar_reservas_checkout
_Esta subseccion detalla validar_reservas_checkout dentro del contexto del documento._
- Input esperado:
  - `cartId`, `reservationIds[]`, `checkoutCorrelationId`.
- Postcondiciones:
  - `valid=true/false` + `reasonCodes`.

### reconciliar_sku
_Esta subseccion detalla reconciliar_sku dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `sku`, `sourceEvent`, `operationRef`.
- Postcondiciones:
  - estado operativo de SKU alineado con catalogo.
