---
title: "Registro de Ejecuciones"
linkTitle: "6.3 Registro"
weight: 3
url: "/mvp/calidad/evidencias/registro-ejecuciones/"
---

## Proposito
Mantener un ledger auditable de corridas reales del baseline. Este documento se actualiza solo con ejecuciones efectivas.

## Convenciones
- No registrar corridas no ejecutadas.
- Cada ejecucion debe enlazar evidencia verificable.
- Una ejecucion puede cubrir varias suites/casos, pero debe declarar scope exacto.

## Plantilla de registro
| ID ejecucion | Fecha (UTC) | Entorno | Scope | Suites/casos ejecutados | Resultado | Defectos encontrados | Defectos aceptados | Evidencia asociada | Conclusiones |
|---|---|---|---|---|---|---|---|---|---|
| `EXEC-YYYYMMDD-001` | `YYYY-MM-DDThh:mm:ssZ` | `qa/staging/prod` | release candidato | IDs de suites/casos | `ok/fail/partial` | lista IDs defecto | lista IDs aceptados | links a artefactos | decision de salida |

## Registro vigente
Sin corridas registradas en este artefacto para el baseline `MVP` al momento de esta actualizacion documental.

## Checklist antes de agregar una fila real
- evidencia de runner o pipeline disponible;
- evidencia tecnica adjunta (logs, reportes, trazas, consultas);
- referencia de version/commit evaluado;
- relacion con gate aplicable y matriz de resultados esperados.
