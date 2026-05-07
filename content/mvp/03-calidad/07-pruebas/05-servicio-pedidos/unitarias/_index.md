---
title: "Unitarias - servicio-pedidos"
linkTitle: "1. Unitarias"
weight: 1
url: "/mvp/calidad/pruebas/servicio-pedidos/unitarias/"
---

## Objetivo
Validar invariantes del dominio de pedidos, politicas de transicion y reglas de pago/idempotencia.

## Cobertura objetivo
- `RN-ORD-01`, `RN-ORD-02`, `RN-ORD-03`.
- `RN-PAY-01`, `RN-PAY-02`.
- `I-ORD-01`, `I-ORD-02`, `I-PAY-01`, `I-LOC-01`.
- `D-ORD-01`, `D-ORD-02`, `D-PAY-01`, `D-CROSS-01`.

## Fixtures base sugeridos
- `fixture_order_cart_active_with_items.yaml`
- `fixture_order_checkout_validated.yaml`
- `fixture_order_pending_approval.yaml`
- `fixture_order_confirmed.yaml`
- `fixture_order_payment_pending.yaml`
- `fixture_order_country_policy_resolved.yaml`

## Matriz detallada de casos unitarios
| ID | Escenario | Given | When | Then | Trazabilidad |
|---|---|---|---|---|---|
| ORD-UT-001 | crear pedido desde checkout valido | checkout prevalidado + reservas confirmables | confirmar checkout | pedido en `PENDING_APPROVAL` | FR-004, RN-ORD-01 |
| ORD-UT-002 | no crear pedido con reservas invalidas | reserva expirada/no confirmable | confirmar checkout | error `conflicto_checkout` | FR-004, I-ORD-01 |
| ORD-UT-003 | idempotencia por `checkoutCorrelationId` | misma correlacion + mismo payload | reintentar confirmacion | un solo pedido materializado | RN-ORD-02, NFR-006 |
| ORD-UT-004 | conflicto idempotente en checkout | misma clave + payload distinto | reintentar confirmacion | error `conflicto_idempotencia` | NFR-009 |
| ORD-UT-005 | transicion valida `PENDING_APPROVAL -> CONFIRMED` | pedido en `PENDING_APPROVAL` | confirmar aprobacion | estado `CONFIRMED` | FR-006, RN-ORD-03 |
| ORD-UT-006 | transicion invalida `CANCELLED -> CONFIRMED` | pedido `CANCELLED` | intentar confirmar | error `transicion_estado_invalida` | FR-005, I-ORD-02 |
| ORD-UT-007 | transicion reservada no permitida en MVP | pedido `PENDING_APPROVAL` | intentar `READY_TO_DISPATCH` | error `transicion_estado_invalida` | FR-006 baseline |
| ORD-UT-008 | cancelacion valida en estado permitido | pedido `PENDING_APPROVAL` | cancelar pedido | estado `CANCELLED` | FR-005 |
| ORD-UT-009 | pago manual requiere monto positivo | monto `<= 0` | registrar pago | error `monto_pago_invalido` | FR-010, RN-PAY-01 |
| ORD-UT-010 | referencia de pago duplicada no duplica efecto | pago previo con misma referencia | registrar pago | error/rechazo `pago_duplicado` | FR-010, RN-PAY-02, D-PAY-01 |
| ORD-UT-011 | estado de pago deriva de pagos aplicados | pagos parciales/acumulados | recalcular estado | `PENDING/PARTIALLY_PAID/PAID/OVERPAID_REVIEW` correcto | I-PAY-01, D-ORD-02 |
| ORD-UT-012 | sin politica pais vigente no confirma checkout | country policy ausente/no vigente | confirmar checkout | error `configuracion_pais_no_disponible` | FR-011, I-LOC-01 |
| ORD-UT-013 | acceso cruzado bloqueado | actor tenant A sobre pedido tenant B | validar ownership | error `acceso_cruzado_detectado` | NFR-005, I-ACC-02 |
| ORD-UT-014 | comando mutante sin tenant invalido | comando sin `tenantId` | validar policy | rechazo por aislamiento | NFR-005, D-CROSS-01 |
| ORD-UT-015 | cart abandonado detectado por umbral | carrito inactivo > threshold | evaluar scheduler | marca abandono y emite evento | FR-008 |

## Criterio de exito unitario
- Escenarios `ORD-UT-001..015` en estado `Disenado` o superior, segun corrida y evidencia.
- Reglas de pedido/pago y estados reservados del MVP cubiertos.
- Rechazos semanticos sin side effects indebidos.
