---
title: "Unitarias - servicio-reporteria"
linkTitle: "1. Unitarias"
weight: 1
url: "/mvp/calidad/pruebas/servicio-reporteria/unitarias/"
---

## Objetivo
Validar invariantes de proyecciones, reglas de regionalizacion y politicas de aplicacion idempotente de hechos analiticos.

## Cobertura objetivo
- `RN-REP-01`, `I-REP-01`.
- `RN-LOC-01`, `I-LOC-01`.
- `I-ACC-02`, `D-CROSS-01` en validacion contextual.
- estados de `HechoAnalitico` y `EjecucionReporteSemanal`.

## Fixtures base sugeridos
- `fixture_reporting_fact_captured_order_confirmed.yaml`
- `fixture_reporting_fact_applied_existing_source_event.yaml`
- `fixture_reporting_weekly_run_pending.yaml`
- `fixture_reporting_weekly_run_completed.yaml`
- `fixture_reporting_country_policy_active_co.yaml`
- `fixture_reporting_country_policy_missing_ec.yaml`
- `fixture_reporting_artifact_metadata_valid.yaml`

## Matriz detallada de casos unitarios
| ID | Escenario | Given | When | Then | Trazabilidad |
|---|---|---|---|---|---|
| REP-UT-001 | aplicar hecho nuevo | `sourceEventId` no existente | aplicar hecho analitico | estado `APPLIED` y metrica actualizada | FR-003, RN-REP-01 |
| REP-UT-002 | dedupe por `sourceEventId` | hecho ya aplicado | reaplicar mismo hecho | `noop idempotente` sin doble agregacion | RN-REP-01, NFR-009 |
| REP-UT-003 | rechazo de hecho invalido | payload sin campos minimos | normalizar hecho | estado `REJECTED` + motivo canonico | RN-REP-01 |
| REP-UT-004 | vista es solo lectura de core | intento mutar entidad core desde Reporting | ejecutar accion de vista | error de dominio y sin mutacion core | I-REP-01 |
| REP-UT-005 | generar reporte semanal valido | datos agregados completos | cerrar ventana semanal | `EjecucionReporteSemanal=COMPLETED` | FR-007 |
| REP-UT-006 | prevenir duplicado semanal | existe `tenant+week+type` previo | intentar nueva generacion | error `reporte_duplicado` | FR-007 |
| REP-UT-007 | bloqueo por politica regional ausente | `countryCode` sin configuracion vigente | consultar/generar reporte | error `configuracion_pais_no_disponible` | FR-011, RN-LOC-01 |
| REP-UT-008 | no fallback global implicito | politica local ausente y global disponible | evaluar resolucion regional | bloqueo obligatorio | I-LOC-01, NFR-011 |
| REP-UT-009 | rebuild no corre en paralelo | ejecucion `RUNNING` existente | iniciar nuevo rebuild | error `rebuild_in_progress` | FR-003, NFR-008 |
| REP-UT-010 | idempotencia de operacion interna | misma key + mismo payload | reintentar `weekly/generate` | retorna mismo resultado | NFR-009 |
| REP-UT-011 | conflicto idempotente | misma key + payload distinto | reintentar operacion interna | error `conflicto_idempotencia` | NFR-009 |
| REP-UT-012 | transicion valida `PENDING->RUNNING` | ejecucion inicial `PENDING` | iniciar job | estado `RUNNING` | FR-007 |
| REP-UT-013 | transicion valida `RUNNING->COMPLETED` | ejecucion en curso + export ok | finalizar job | estado `COMPLETED` + `locationRef` | FR-007 |
| REP-UT-014 | transicion valida `RUNNING->FAILED` | ejecucion en curso + export falla | finalizar job | estado `FAILED` + error `reporte_generacion_fallida` | NFR-007 |
| REP-UT-015 | aislamiento tenant en consultas | actor tenant A consulta tenant B | validar `TenantIsolationPolicy` | error `acceso_cruzado_detectado` | NFR-005, I-ACC-02 |
| REP-UT-016 | comando mutante sin tenant invalido | request m2m sin `tenantId` | validar contexto | rechazo por `D-CROSS-01` | NFR-005, D-CROSS-01 |
| REP-UT-017 | artifact inexistente | `reportId` sin `locationRef` registrado | resolver descarga | error `artifact_not_found` | FR-007 |
| REP-UT-018 | weekId invalido | `weekId` no normalizado | validar request | error de validacion semantica | FR-007 |

## Criterio de exito unitario
- Escenarios `REP-UT-001..018` en estado `Disenado` o superior, segun corrida y evidencia.
- Reglas de regionalizacion y dedupe cubiertas en diseno y pendientes de certificacion por corrida.
- En corrida de certificacion, no deben ocurrir mutaciones cross-tenant ni sobre BC core transaccional.
