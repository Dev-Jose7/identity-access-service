---
title: "Trazabilidad de Eventos"
linkTitle: "9.4 Trazabilidad de Eventos"
weight: 4
url: "/mvp/operacion/eventos-dlq/trazabilidad-eventos/"
---

## Objetivo
Asegurar trazabilidad operativa de extremo a extremo para eventos
publicados, consumidos, reprocesados o descartados.

## Metadata minima
- `eventId`, `eventType`, `eventVersion`, `occurredAt`.
- `tenantId`, `traceId`, `correlationId`.
- `producer`, `consumerRef`, `processingResult`.

## Cadena de trazabilidad esperada
`decision de dominio -> outbox -> broker -> consumidor -> side effect -> auditoria`

## Evidencia operacional requerida
- logs estructurados de publish/consume;
- registro de dedupe o consumo aplicado;
- evidencia de DLQ/replay cuando aplique;
- correlacion de evento con mutacion observada en servicio destino.

## Uso en incidentes
- Todo incidente async debe incluir al menos un `eventId` ancla.
- El postmortem debe indicar si hubo perdida, duplicado o atraso de evento.
