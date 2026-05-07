---
title: "Unitarias - servicio-inventario"
linkTitle: "1. Unitarias"
weight: 1
url: "/mvp/calidad/pruebas/servicio-inventario/unitarias/"
---

## Objetivo
Validar invariantes de stock y reservas, transiciones de estado y reglas todo-o-nada del dominio Inventory.

## Cobertura objetivo
- `RN-INV-01`, `RN-RES-01`, `RN-RES-02`.
- `I-INV-01`, `I-INV-02`.
- `D-INV-01`, `D-INV-02`.
- `I-ACC-02`, `D-CROSS-01` en validacion contextual.

## Fixtures base sugeridos
- `fixture_inventory_stock_active_with_available_qty.yaml`
- `fixture_inventory_stock_blocked.yaml`
- `fixture_inventory_reservation_active_ttl_valid.yaml`
- `fixture_inventory_reservation_expired.yaml`
- `fixture_inventory_low_stock_threshold.yaml`
- `fixture_inventory_idempotency_record_existing.yaml`

## Matriz detallada de casos unitarios
| ID | Escenario | Given | When | Then | Trazabilidad |
|---|---|---|---|---|---|
| INV-UT-001 | stock fisico nunca negativo | stock con qty conocida | aplicar decremento mayor al disponible | error `stock_negativo_invalido` | FR-002, I-INV-01, D-INV-01 |
| INV-UT-002 | reservas activas no exceden stock fisico | stock con reservas existentes | crear nueva reserva excediendo disponibilidad | error `stock_insuficiente` | FR-004, I-INV-02, D-INV-02 |
| INV-UT-003 | reserva valida con disponibilidad suficiente | stock disponible >= qty | crear reserva | estado `ACTIVE` con `expiresAt` | RN-RES-01 |
| INV-UT-004 | regla todo-o-nada | solicitud multiposicion con una linea insuficiente | crear reservas | rechazo total sin parciales | RN-RES-02, FR-004 |
| INV-UT-005 | extender reserva solo activa | reserva `ACTIVE` | extender TTL | nuevo `expiresAt` mayor al anterior | FR-004 |
| INV-UT-006 | no extender reserva expirada | reserva `EXPIRED` | extender TTL | error `reserva_expirada` | FR-004 |
| INV-UT-007 | confirmar reserva activa | reserva `ACTIVE` y vigente | confirmar reserva | estado `CONFIRMED`, qty comprometida | FR-004 |
| INV-UT-008 | no confirmar reserva expirada | reserva `EXPIRED` | confirmar reserva | error `reserva_expirada` | FR-004 |
| INV-UT-009 | liberar reserva activa resta reserved qty | reserva `ACTIVE` | liberar reserva | estado `RELEASED`, disponibilidad restaurada | FR-004 |
| INV-UT-010 | expirar reserva por TTL | `now > expiresAt` | ejecutar expiracion | estado `EXPIRED` y evento de expiracion | FR-004 |
| INV-UT-011 | deteccion de bajo stock | `availableQty <= reorderPoint` | recalcular estado | `low_stock=true` + seaNal de alerta | FR-003 |
| INV-UT-012 | acceso cruzado en mutacion de stock | actor tenant A sobre stock tenant B | evaluar tenant/ownership | error `acceso_cruzado_detectado` | NFR-005, I-ACC-02 |
| INV-UT-013 | comando mutante sin tenant es invalido | comando sin `tenantId` | validar policy | rechazo por aislamiento | NFR-005, D-CROSS-01 |
| INV-UT-014 | idempotencia semantica en mutacion | misma clave + mismo payload | reintentar comando | resultado equivalente sin doble efecto | NFR-009 |
| INV-UT-015 | conflicto idempotente por payload distinto | misma clave + hash distinto | reintentar comando | error `conflicto_idempotencia` | NFR-009 |

## Criterio de exito unitario
- Escenarios `INV-UT-001..015` en estado `Disenado` o superior, segun corrida y evidencia.
- Invariantes criticos de stock/reserva cubiertos en diseno y pendientes de certificacion por corrida.
- Rechazos semanticos sin mutaciones indebidas.
