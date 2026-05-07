---
title: "Comandos"
linkTitle: "4. Comandos"
weight: 4
url: "/mvp/dominio/contextos-delimitados/pedidos/comandos/"
---

## Marco de comandos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Catalogo de comandos de `order`.

## Lista de comandos
_Responde: que comandos admite el contexto y con que efecto semantico._

### obtener_carrito_activo
_Esta subseccion detalla obtener_carrito_activo dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `organizationId`, `userId`.
- Postcondiciones:
  - carrito vigente con items/reservas.

### agregar_o_actualizar_item_carrito
_Esta subseccion detalla agregar_o_actualizar_item_carrito dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `organizationId`, `userId`, `variantId`, `sku`, `qty`, `warehouseId`, `idempotencyKey`.
- Precondiciones:
  - sesion valida, variante vendible, qty > 0.
- Postcondiciones:
  - item upsert con reserva activa.
- Errores:
  - `stock_insuficiente`, `variante_no_vendible`, `acceso_cruzado_detectado`.

### remover_item_carrito
_Esta subseccion detalla remover_item_carrito dentro del contexto del documento._
- Input esperado:
  - `cartId`, `cartItemId`, `tenantId`, `idempotencyKey`.
- Postcondiciones:
  - item removido y reserva liberada.

### limpiar_carrito
_Esta subseccion detalla limpiar_carrito dentro del contexto del documento._
- Input esperado:
  - `cartId`, `tenantId`, `idempotencyKey`.
- Postcondiciones:
  - carrito sin items y reservas liberadas.

### solicitar_validacion_checkout
_Esta subseccion detalla solicitar_validacion_checkout dentro del contexto del documento._
- Input esperado:
  - `cartId`, `addressId`, `checkoutCorrelationId`, `tenantId`, `idempotencyKey`.
- Precondiciones:
  - carrito no vacio.
- Postcondiciones:
  - resultado `VALID|INVALID` con reasonCodes y snapshot de politica regional cuando es `VALID`.
- Errores:
  - `conflicto_checkout`, `direccion_invalida`, `reserva_expirada`, `configuracion_pais_no_disponible`.

### confirmar_pedido
_Esta subseccion detalla confirmar_pedido dentro del contexto del documento._
- Input esperado:
  - `checkoutCorrelationId`, `tenantId`, `idempotencyKey`.
- Precondiciones:
  - intento checkout valido, reservas vigentes y politica operativa por pais vigente.
- Postcondiciones:
  - pedido creado en `PENDING_APPROVAL` con snapshots inmutables (direccion/precio/politica regional).
  - emite `OrderCreated` (creacion, no confirmacion comercial final).
- Errores:
  - `conflicto_checkout`, `reserva_expirada`, `configuracion_pais_no_disponible`, `conflicto_idempotencia`.

### confirmar_aprobacion_pedido
_Esta subseccion detalla confirmar_aprobacion_pedido dentro del contexto del documento._
- Input esperado:
  - `orderId`, `tenantId`, `actorRole`, `reason`, `idempotencyKey`.
- Precondiciones:
  - pedido en `PENDING_APPROVAL`.
  - transicion permitida y actor autorizado.
- Postcondiciones:
  - pedido en `CONFIRMED`.
  - emite `OrderConfirmed`.
- Errores:
  - `transicion_estado_invalida`, `acceso_cruzado_detectado`.

### cancelar_pedido
_Esta subseccion detalla cancelar_pedido dentro del contexto del documento._
- Input esperado:
  - `orderId`, `tenantId`, `reason`, `idempotencyKey`.
- Precondiciones:
  - transicion permitida.

### cambiar_estado_pedido
_Esta subseccion detalla cambiar_estado_pedido dentro del contexto del documento._
- Input esperado:
  - `orderId`, `targetState`, `reason`, `tenantId`, `actorRole`, `idempotencyKey`.
- Alcance MVP:
  - para `MVP` se usa solo para transiciones de baseline (`PENDING_APPROVAL <-> CONFIRMED`, `* -> CANCELLED`).
  - estados de fulfillment (`READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED`) quedan reservados.
- Errores:
  - `transicion_estado_invalida`, `acceso_cruzado_detectado`.

### registrar_pago_manual
_Esta subseccion detalla registrar_pago_manual dentro del contexto del documento._
- Input esperado:
  - `orderId`, `paymentReference`, `method`, `amount`, `currency`, `receivedAt`, `tenantId`, `idempotencyKey`.
- Precondiciones:
  - pedido existente y referencia unica.
- Postcondiciones:
  - pago registrado y estado de pago recalculado.
- Errores:
  - `pago_duplicado`, `monto_pago_invalido`.

### detectar_carritos_abandonados
_Esta subseccion detalla detectar_carritos_abandonados dentro del contexto del documento._
- Input esperado:
  - `batchSize`, `thresholdMinutes`, `tenantScope`, `operationRef`, `idempotencyKey`.
- Postcondiciones:
  - carritos marcados como `ABANDONED` y evento emitido.
