---
title: "Registro de Incidentes"
linkTitle: "4. Registro de Incidentes"
weight: 4
url: "/mvp/operacion/incidentes/registro-incidentes/"
---

## Formato minimo de registro
| Campo | Descripcion |
|---|---|
| incidentId | identificador unico |
| fechaInicio / fechaFin | ventana del incidente |
| severidad | Sev 1..Sev 4 |
| serviciosAfectados | servicios impactados |
| impactoNegocio | descripcion concreta |
| deteccion | alerta/reporte que lo disparo |
| causaRaiz | confirmada o preliminar |
| acciones | contencion y recuperacion |
| runbookUsado | referencia al runbook aplicado |
| evidencias | enlaces a dashboards/logs/trazas |
| estado | abierto, mitigado, cerrado |

## Politica de conservacion
El registro se conserva segun baseline de retencion de auditoria de
seguridad y operacion definido en arquitectura.
