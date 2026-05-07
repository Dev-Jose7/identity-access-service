---
title: "Riesgos y Deuda Tecnica"
linkTitle: "11. Riesgos y Deuda Tecnica"
weight: 11
url: "/mvp/arquitectura/arc42/riesgos-deuda-tecnica/"
---

## Proposito
Registrar riesgos tecnicos/de arquitectura y deuda aceptada con plan de mitigacion.

## Matriz de riesgos
| ID | Riesgo | Prob. | Impacto | Mitigacion |
|---|---|---|---|---|
| R-01 | errores de idempotencia duplican efectos en checkout/pago | media | alto | claves idempotentes + pruebas de contrato/integracion |
| R-02 | backlog de broker degrada notificaciones/reportes | media | medio-alto | particionado, autoscaling consumidores, DLQ |
| R-03 | brecha de permisos por rol/pais | alta | alto | baseline RBAC + cierre de matriz de permisos antes de endurecimiento |
| R-04 | uso indebido de reporting como fuente transaccional | media | alto | contratos read-only y controles de frontera |
| R-05 | configuracion reactiva incorrecta introduce bloqueos | media | alto | checklists no-bloqueantes + pruebas de carga |
| R-06 | dependencia de proveedor de notificaciones afecta SLA percibido | media | medio | retries/backoff/canal alterno y mensajes no bloqueantes |

## Deuda tecnica aceptada
| ID | Deuda | Motivo | Plan de pago |
|---|---|---|---|
| TD-01 | pago online no implementado en MVP | alcance inicial | iteracion posterior con ADR si cambia frontera |
| TD-02 | granularidad final de permisos por pais pendiente | depende de decision humana de negocio/regulacion | cerrar en fase de hardening de seguridad |
| TD-03 | tuning de carga real por pais pendiente | falta telemetria de trafico real | recalibrar budgets tras pilotos |
| TD-04 | reporting avanzado (proyecciones complejas) parcial | priorizacion de core transaccional | iteraciones posteriores de analitica |

## Mapa riesgo -> NFR
| Riesgo | NFR impactado |
|---|---|
| R-01 | NFR-004, NFR-006 |
| R-02 | NFR-002, NFR-007, NFR-008 |
| R-03 | NFR-005, NFR-006 |
| R-04 | NFR-004, NFR-009 |
| R-05 | NFR-001, NFR-003 |
| R-06 | NFR-003, NFR-007 |

## Criterio de aceptacion de riesgo
Un riesgo puede aceptarse temporalmente solo si existe:
- mitigacion verificable,
- fecha objetivo de reevaluacion,
- impacto documentado en FR/NFR.
