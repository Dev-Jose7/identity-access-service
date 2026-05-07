---
title: "E2E - servicio-pedidos"
linkTitle: "3. E2E"
weight: 3
url: "/mvp/calidad/pruebas/servicio-pedidos/e2e/"
---

## Objetivo
Validar journeys comerciales de pedidos de punta a punta, con dependencias reales de checkout, pagos, notificaciones y reporting.

## Alcance E2E
- ciclo completo carrito -> checkout -> pedido.
- ciclo de estado del pedido dentro del baseline MVP.
- pagos manuales y estado de pago.
- eventos y politicas de reaccion por integraciones.
- seguridad tenant/rol y regionalizacion obligatoria.

## Escenarios E2E priorizados
| ID | Escenario | Flujo | Resultado esperado | Trazabilidad |
|---|---|---|---|---|
| ORD-E2E-001 | checkout exitoso crea pedido | carrito valido -> validations -> confirm | `OrderCreated` con `status=PENDING_APPROVAL` | FR-004, RN-ORD-01 |
| ORD-E2E-002 | confirmacion comercial de pedido | `PENDING_APPROVAL -> CONFIRMED` | `OrderConfirmed` y `OrderStatusChanged` | FR-006, RN-ORD-03 |
| ORD-E2E-003 | rechazo por reserva expirada | reserva vence antes de confirm | `conflicto_checkout`/`reserva_expirada` sin crear pedido | FR-004 |
| ORD-E2E-004 | rechazo por falta de politica pais | checkout sin policy vigente | `configuracion_pais_no_disponible` + auditoria | FR-011, NFR-011 |
| ORD-E2E-005 | edicion/cancelacion solo en estados permitidos | pedido en distintos estados | permite/bloquea segun matriz de transiciones | FR-005, I-ORD-02 |
| ORD-E2E-006 | bloqueo de estados reservados | intentar transicionar a `READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED` | rechazo `transicion_estado_invalida` | baseline MVP |
| ORD-E2E-007 | pago manual parcial y total | registrar pagos sucesivos validos | `paymentStatus` pasa `PENDING -> PARTIALLY_PAID -> PAID` | FR-010, I-PAY-01 |
| ORD-E2E-008 | pago duplicado no duplica efecto | reintento con misma referencia | rechazo/control idempotente sin doble aplicacion | FR-010, RN-PAY-02 |
| ORD-E2E-009 | variant discontinued impacta carrito | catalog emite `VariantDiscontinued` | item invalido/removido, checkout protegido | FR-004 |
| ORD-E2E-010 | user blocked contiene riesgo operativo | IAM emite `UserBlocked` | cancela/no permite mutaciones en pedidos no terminales segun politica | FR-009 |
| ORD-E2E-011 | cart abandoned dispara notificacion | scheduler detecta abandono | `CartAbandonedDetected` + flujo notificacion no bloqueante | FR-008, NFR-007 |
| ORD-E2E-012 | seguridad tenant en APIs de pedidos | actor tenant A consulta/muta tenant B | rechazo `acceso_cruzado_detectado` | NFR-005 |
| ORD-E2E-013 | resiliencia outbox ante falla broker | mutacion exitosa con publicacion fallida | decision persiste + outbox reintenta | NFR-006 |
| ORD-E2E-014 | trazabilidad tecnica completa | ejecutar checkout+pago+status | cadena `request -> db -> outbox -> evento` correlacionada | NFR-006, NFR-009 |

## Criterio de exito E2E
- Escenarios `ORD-E2E-001..014` disenados para ejecucion reproducible; el estado final requiere corrida y evidencia.
- En corrida de certificacion, los flujos criticos (`001`, `002`, `004`, `007`, `012`) deben ejecutarse sin desviaciones semanticas.
- Estados reservados de fulfillment permanecen fuera del baseline operativo.

## Evidencia minima por corrida
- Reporte por escenario con estado (`Implementado`/`Ejecutado`/`Validado con evidencia`).
- Extractos de `order_audit`, `outbox_event`, `processed_event` y snapshots correlacionados.
- Confirmacion de contratos API/eventos `v1` sin ruptura para consumidores.
