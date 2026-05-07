---
title: "Degradacion Controlada"
linkTitle: "3. Degradacion Controlada"
weight: 3
url: "/mvp/operacion/continuidad-recuperacion/degradacion-controlada/"
---

## Objetivo
Definir modos de degradacion aceptables del MVP sin romper reglas core.

## Modos permitidos
- priorizacion de endpoints criticos sobre no criticos
- limitacion temporal de operaciones pesadas
- colas diferidas para procesos derivados (notificaciones/reporting)
- procesamiento asincrono diferido de reprocesos no criticos

## Modos no permitidos
- bypass de aislamiento tenant
- fallback global implicito para regionalizacion
- omision de auditoria en operaciones criticas
- desactivar controles de integridad de reservas/pedidos

## Escenarios de degradacion aceptable
| Escenario | Degradacion permitida | Limite operativo |
|---|---|---|
| alta latencia en core | reducir operaciones no criticas y priorizar checkout | mantener creacion de pedido funcional |
| lag de consumidores | pausar reproceso masivo y priorizar topicos core | evitar crecimiento sostenido de DLQ |
| atraso de reporting | diferir consolidacion no critica | no afectar transacciones core |
| falla de proveedor externo de notificacion | reintentar y enrutar fallas sin bloquear pedido | mantener evidencia de eventos y reintentos |

## Activacion y cierre
- Activacion: alerta de severidad alta/critica con impacto confirmado.
- Responsables: incident commander + owner de servicio.
- Cierre: restauracion de metricas en ventana objetivo y validacion de flujos
  core.

## Cierre de degradacion
Solo se cierra cuando metricas y flujos criticos regresan a baseline.
