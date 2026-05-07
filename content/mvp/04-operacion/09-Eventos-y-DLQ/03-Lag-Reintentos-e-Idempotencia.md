---
title: "Lag, Reintentos e Idempotencia"
linkTitle: "9.3 Lag, Reintentos e Idempotencia"
weight: 3
url: "/mvp/operacion/eventos-dlq/lag-reintentos-idempotencia/"
---

## Objetivo
Operar backlog y reintentos del plano async manteniendo idempotencia
operacional y tiempos de convergencia aceptables.

## Senales clave
- `consumer_lag` por grupo/topico.
- ratio de retries por consumidor.
- tasa de dedupe (`processed_event` / noop idempotente).
- tiempo de convergencia a estado estable.

## Reglas de reintento
- Retry con backoff para fallos transitorios.
- Sin retry infinito.
- Escalar a DLQ cuando supere umbral de intentos.

## Reglas de idempotencia operacional
- Repeticion de mensaje no debe duplicar mutacion.
- Consumidor debe registrar consumo aplicado o noop.
- Reproceso debe validar no duplicidad de efectos de negocio.

## Acciones ante lag alto
1. Confirmar salud de consumidor y dependencias.
2. Validar si hay cuello de botella en DB/cache/red.
3. Ajustar paralelismo/lote segun runbook.
4. Si persiste, aplicar degradacion controlada y escalamiento.
