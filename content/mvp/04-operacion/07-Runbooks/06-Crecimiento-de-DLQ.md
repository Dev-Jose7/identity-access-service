---
title: "Crecimiento de DLQ"
linkTitle: "6. Crecimiento de DLQ"
weight: 6
url: "/mvp/operacion/runbooks/crecimiento-dlq/"
---

## Proposito
Controlar aumento anormal de mensajes en DLQ sin perder trazabilidad.

## Senal de entrada
- Alerta de crecimiento sostenido de DLQ.
- Reintentos agotados en consumidor.

## Senales para priorizacion
| Senal | Prioridad sugerida |
|---|---|
| DLQ creciendo en topico core (order/inventory) | Sev 1/Sev 2 alta |
| DLQ creciendo en topico derivado (reporting/notification) | Sev 2/Sev 3 |
| reingreso recurrente del mismo mensaje | Sev 2 |

## Impacto esperado
Procesamiento atrasado y riesgo de inconsistencia.

## Diagnostico inicial
- Clasificar causa: contrato, datos, dependencia, bug.
- Identificar primer mensaje fallido y patron.

## Decision operativa (si X entonces Y)
| Si | Entonces |
|---|---|
| falla por contrato/version | pausar reproceso y corregir compatibilidad antes de reintentar |
| falla por dependencia transitoria | mantener retry controlado y monitorear drenaje |
| falla por datos invalidados | aislar lote defectuoso y reprocesar lotes validos |

## Contencion
- Pausar reprocesos masivos automaticos.
- Aislar tipo de mensaje defectuoso.

## Recuperacion
- Corregir causa raiz.
- Reprocesar en lotes pequenos con control de idempotencia.
- Confirmar salida progresiva de DLQ.

## Criterio de recuperacion cumplida
- tendencia de DLQ decreciente en dos ventanas consecutivas;
- mensajes reprocesados sin duplicar efectos;
- lag de consumidor en retorno a baseline.

## Verificacion posterior
- DLQ estable o decreciente.
- Sin reingreso recurrente del mismo mensaje.

## Escalamiento
Sev 2, Sev 1 si bloquea flujo core.

## Evidencia a registrar
- conteo DLQ por topico
- lote reprocesado y resultado
- causa raiz confirmada
- decision de reproceso/descarto por tipo de mensaje

## Artefactos relacionados
- 09-Eventos-y-DLQ/02-DLQ-y-Reprocesos
- 09-Eventos-y-DLQ/03-Lag-Reintentos-e-Idempotencia
