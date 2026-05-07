---
title: "Observabilidad"
linkTitle: "5. Observabilidad"
weight: 5
url: "/mvp/operacion/observabilidad/"
---

## Proposito
Definir el baseline observable de `MVP` para detectar degradacion,
correlacionar incidentes y acelerar recuperacion.

## Senales obligatorias
- Logs estructurados por servicio.
- Metricas de latencia, error-rate, throughput y saturacion.
- Trazas distribuidas en flujos criticos.
- Correlacion extremo a extremo con `traceId` y `correlationId`.

## Campos minimos de observabilidad
| Tipo | Campos minimos |
|---|---|
| Log mutante | `timestamp`, `service`, `operation`, `traceId`, `correlationId`, `tenantId`, `actorId`, `result`, `errorCode` |
| Evento de dominio | `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `correlationId`, `tenantId` |
| Auditoria | `operationRef`, `actorId`, `tenantId`, `decision`, `reason` |

## Dashboards minimos
- Salud por servicio (`up/down`, p95, error-rate).
- Flujo de compra (`checkout`, reservas, `OrderCreated`, `OrderConfirmed`).
- Broker y consumidores (lag, retries, DLQ size).
- Notificaciones (requested/sent/failed/discarded).
- Reporting (lag de ingestion, duracion de job semanal).
- Seguridad (fallos authn/authz, acceso cruzado, revocaciones).

## Observabilidad por flujo critico
| Flujo | Senales minimas |
|---|---|
| Login/refresh/logout | tasa de fallo, latencia, revocaciones, auditoria de seguridad |
| Checkout y pedido | latencia, `stock_insuficiente`, `conflicto_checkout`, eventos `OrderCreated`/`OrderConfirmed` |
| Pago manual | tasa de `pago_duplicado`, latencia, estado de pago agregado |
| Notificacion | `NotificationRequested/Sent/Failed`, retries, DLQ por canal |
| Reporting semanal | tiempo de consolidacion, lag de consumidor, errores de proyeccion |
| Regionalizacion | tasa de `configuracion_pais_no_disponible` y trazabilidad de bloqueos |

## Reglas de datos sensibles en observabilidad
- No loggear payloads sensibles completos.
- Aplicar masking/hashing en identificadores sensibles.
- Mantener solo metadatos minimos necesarios para diagnostico.

## Relacion con eventos y DLQ
- Toda alerta de lag o DLQ debe enlazar un runbook de respuesta.
- Reprocesos deben conservar `traceId/correlationId` o mapearlos en
  metadata de operacion.

## Referencias
- [SLI, SLO y Alertas](/mvp/operacion/sli-slo-alertas/)
- [Eventos y DLQ](/mvp/operacion/eventos-dlq/)
- [Runbooks](/mvp/operacion/runbooks/)
