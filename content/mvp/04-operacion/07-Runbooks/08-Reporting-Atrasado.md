---
title: "Reporting Atrasado"
linkTitle: "8. Reporting Atrasado"
weight: 8
url: "/mvp/operacion/runbooks/reporting-atrasado/"
---

## Proposito
Restablecer frescura de reportes sin comprometer consistencia del core.

## Senal de entrada
- SLA de actualizacion semanal incumplido.
- Desfase entre eventos fuente y datasets de reporteria.

## Impacto esperado
Analitica desactualizada para negocio y operaciones.

## Diagnostico inicial
- Medir atraso por fuente de evento.
- Revisar pipelines y jobs de agregacion.

## Contencion
- Comunicar ventana de atraso a stakeholders.
- Priorizar cargas de datasets criticos.

## Recuperacion
- Ejecutar backfill controlado por rango temporal.
- Reprocesar eventos pendientes.

## Verificacion posterior
- Frescura dentro del umbral definido.
- Conteos reconciliados con fuentes.

## Escalamiento
Sev 3 o Sev 2 segun impacto operativo.

## Evidencia a registrar
- timestamp de ultima actualizacion
- lotes reprocesados
- reconciliacion de totales

## Artefactos relacionados
- reporting-service/runtime/03-Casos-de-Uso-en-Ejecucion
- 06-SLI-SLO-y-Alertas
