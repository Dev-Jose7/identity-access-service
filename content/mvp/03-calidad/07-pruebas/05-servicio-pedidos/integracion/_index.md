---
title: "Integracion - servicio-pedidos"
linkTitle: "2. Integracion"
weight: 2
url: "/mvp/calidad/pruebas/servicio-pedidos/integracion/"
---

## Objetivo
Validar contratos REST/eventos, persistencia de snapshots y consistencia de integraciones sync/async del servicio de pedidos.

## Dependencias y entornos
- BD Order (cart, cart_item, checkout_attempt, purchase_order, order_line, payment_record, status_history, order_audit, outbox_event, processed_event).
- broker/event bus para publicacion/consumo de eventos.
- dependencias sync: Directory, Catalog, Inventory.
- contexto de seguridad para `tenant_user`, `arka_operator`, `arka_admin`, `system_scheduler`.

## Datos de entrada
- requests/responses de contratos `v1`.
- headers `Authorization`, `Idempotency-Key`, `X-Request-Id` en mutaciones.
- eventos entrantes: `inventory.stock-reservation-*`, `catalog.variant-discontinued`, `catalog.price-updated`, `directory.checkout-address-validated`, `iam.user-blocked`.

## Matriz detallada de casos de integracion
| ID | Escenario | Validacion principal | Evidencia | Trazabilidad |
|---|---|---|---|---|
| ORD-IT-001 | `GET /cart` | recuperar carrito activo scoped por tenant | response coherente con DB | FR-004 |
| ORD-IT-002 | `PUT /cart/items/{sku}` | upsert item + reserva | item persistido + `CartItemAdded/Updated` | FR-004 |
| ORD-IT-003 | `DELETE /cart/items/{cartItemId}` | remover item + liberar reserva | item removido + `CartItemRemoved` | FR-004 |
| ORD-IT-004 | `DELETE /cart` | limpiar carrito completo | `CartCleared` + liberaciones aplicadas | FR-004 |
| ORD-IT-005 | `POST /checkout/validations` valido | validacion sync de direccion+reservas+politica pais | `valid=true` + snapshots armados | FR-004, FR-011 |
| ORD-IT-006 | `POST /checkout/validations` invalido | conflicto por direccion/reserva | `valid=false` + reason codes + `OrderCheckoutValidationFailed` | FR-004 |
| ORD-IT-007 | `POST /checkout/confirm` exitoso | crear pedido desde checkout | `status=PENDING_APPROVAL` + `OrderCreated` + snapshots persistidos | FR-004, RN-ORD-01 |
| ORD-IT-008 | confirmacion idempotente | misma key/correlation + mismo payload | mismo resultado sin duplicar pedido | RN-ORD-02, NFR-009 |
| ORD-IT-009 | conflicto idempotente checkout | misma key + payload distinto | `409 conflicto_idempotencia` | NFR-009 |
| ORD-IT-010 | `POST /{orderId}/status-transitions` valido | transicion operativa permitida | `PENDING_APPROVAL -> CONFIRMED` + `OrderConfirmed`/`OrderStatusChanged` | FR-006 |
| ORD-IT-011 | transicion invalida | estado no permitido | `409 transicion_estado_invalida` + sin side effects | FR-005, I-ORD-02 |
| ORD-IT-012 | bloqueo de estados reservados | intentar `READY_TO_DISPATCH/DISPATCHED/DELIVERED` | rechazo en baseline MVP | FR-006 baseline |
| ORD-IT-013 | `POST /{orderId}/cancel` | cancelacion valida | estado `CANCELLED` + `OrderCancelled` | FR-005 |
| ORD-IT-014 | `POST /{orderId}/payments/manual` valido | registro de pago manual | `OrderPaymentRegistered` + `OrderPaymentStatusUpdated` + recalculo estado | FR-010 |
| ORD-IT-015 | pago duplicado | misma `paymentReference` | rechazo `pago_duplicado` sin doble aplicacion | FR-010, D-PAY-01 |
| ORD-IT-016 | consultas de pedido y pagos | `GET /orders/{id}`, `/payments`, `/timeline` | datos consistentes y ordenados | FR-006, FR-010 |
| ORD-IT-017 | listado de pedidos paginado | `GET /orders?page&size` | pagina, filtros y scope tenant correctos | FR-004 |
| ORD-IT-018 | deteccion de carrito abandonado | `POST /internal/.../detect-abandoned` | `CartAbandonedDetected` + estado actualizado | FR-008 |
| ORD-IT-019 | consumo `inventory.stock-reservation-expired` | ajustar carrito por expiracion | item invalido/removido + `processed_event` | politicas Order |
| ORD-IT-020 | consumo `inventory.stock-reservation-confirmed` | reconciliar trazabilidad de linea | estado sincronizado + `processed_event` | FR-004 |
| ORD-IT-021 | consumo `catalog.variant-discontinued` | bloquear SKU en carrito | item removido/invalido + `processed_event` | FR-004 |
| ORD-IT-022 | consumo `catalog.price-updated` | refrescar pricing en carrito | snapshots actualizados + `processed_event` | FR-004 |
| ORD-IT-023 | consumo `directory.checkout-address-validated` | cerrar validacion asincrona | estado de checkout actualizado + dedupe | FR-004 |
| ORD-IT-024 | consumo `iam.user-blocked` | contencion de riesgo | cancelar pedidos no terminales segun politica + `processed_event` | FR-009 |
| ORD-IT-025 | dedupe evento duplicado | mismo `eventId` dos veces | segundo consumo `noop idempotente` | NFR-009 |
| ORD-IT-026 | seguridad tenant/ownership en mutaciones | actor tenant A sobre recurso tenant B | rechazo 403/409 + sin cambios | NFR-005, I-ACC-02 |
| ORD-IT-027 | bloqueo regional por pais no configurado | `directory` sin politica vigente en resolucion tecnica | `409 configuracion_pais_no_disponible` + auditoria (mapeo de negocio sobre ausencia tecnica) | FR-011, NFR-011 |
| ORD-IT-028 | propagacion `traceId/correlationId` | mutacion + evento emitido | ids en response, audit y outbox | NFR-006 |
| ORD-IT-029 | resiliencia notificacion no bloqueante | falla downstream notification | core pedido/pago permanece consistente | FR-006, NFR-007 |
| ORD-IT-030 | consistencia de contratos eventos | producer contract tests `Order*`/`Cart*` | payload y topicos `v1` conformes en corrida | NFR-009 |

## Criterio de exito integracion
- Escenarios `ORD-IT-001..030` disenados para verificar ausencia de breaking en contratos `v1`.
- Integraciones sync y async disenadas para verificarse con evidencia persistida en corrida.
- En corrida de certificacion, no deben ocurrir mutaciones cross-tenant.
