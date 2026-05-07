---
title: "Mapeo de Pruebas"
linkTitle: "2. Mapeo de Pruebas"
weight: 2
url: "/mvp/calidad/mapeo-pruebas/"
---

## Proposito
Trazar de forma verificable los requisitos e invariantes del ciclo contra escenarios de prueba concretos, separando diseno, implementacion, ejecucion y validacion con evidencia.

## Convenciones de estado (taxonomia unica)
- `Definido`: requisito identificado en el pilar con alcance de prueba.
- `Disenado`: escenario documentado con Given/When/Then y resultado esperado.
- `Listo para implementar`: escenario disenado con artefacto objetivo de prueba y owner.
- `Implementado`: escenario automatizado en suite.
- `Ejecutado`: corrida registrada en entorno objetivo con resultado trazable.
- `Validado con evidencia`: resultado ejecutado, evidencia referenciada y aprobada en gate.

Estado documental actual del ciclo `MVP`:
- Cobertura funcional/no funcional completa en estado `Listo para implementar`.
- La certificacion operativa (`Ejecutado` / `Validado con evidencia`) depende de corridas y evidencias de release.

## Regla de certificacion minima
- Esta matriz conserva cobertura completa `FR/NFR` del baseline.
- La certificacion minima obligatoria usa un subconjunto critico y representativo definido en:
  - [Plan de Certificacion Minima](/mvp/calidad/evidencias/baseline-certification-plan/)
  - [Matriz de Resultados Esperados](/mvp/calidad/evidencias/matriz-resultados-esperados/)

## Matriz maestra de trazabilidad de pruebas
| Requisito/Invariante | Escenario de prueba | Nivel | Servicio(s) impactado(s) | Artefacto de prueba | Evidencia esperada | Estado |
|---|---|---|---|---|---|---|
| FR-001 | alta/edicion de categoria, marca, producto y variante con reglas de catalogo | Integracion | `catalog-service` | [Catalogo Integracion](/mvp/calidad/pruebas/servicio-catalogo/integracion/) (`CAT-IT-001..025`) | request/response + persistencia + eventos `Product*`/`Variant*`/`Price*` | Listo para implementar |
| FR-002 + RN-INV-01 + I-INV-01 | ajuste de stock nunca permite `stock_fisico < 0` | Unitarias | `inventory-service` | [Inventario Unitarias](/mvp/calidad/pruebas/servicio-inventario/unitarias/) (`INV-UT-001..015`) | rechazos de dominio sin mutacion indebida | Listo para implementar |
| FR-003 + RN-REP-01 + I-REP-01 | calculo de abastecimiento semanal con datos de inventario y ventas | E2E | `reporting-service`, `inventory-service` | [Reporteria E2E](/mvp/calidad/pruebas/servicio-reporteria/e2e/) (`REP-E2E-001..018`) | `WeeklyReportGenerated` + artefacto de reporte + trazabilidad de fuentes | Listo para implementar |
| FR-004 + RN-ORD-01 + I-ORD-01 | checkout valido crea pedido en `PENDING_APPROVAL` | E2E | `order-service`, `inventory-service` | [Pedidos E2E](/mvp/calidad/pruebas/servicio-pedidos/e2e/) (`ORD-E2E-001..014`) | respuesta API + `OrderCreated` + persistencia en `purchase_order`/outbox | Listo para implementar |
| FR-005 + I-ORD-02 | edicion permitida solo en `PENDING_APPROVAL` y rechazada en otros estados | Integracion | `order-service` | [Pedidos Integracion](/mvp/calidad/pruebas/servicio-pedidos/integracion/) (`ORD-IT-001..030`) | respuesta API + `transicion_estado_invalida` cuando aplique | Listo para implementar |
| FR-006 + RN-NOTI-01 + I-NOTI-01 | `OrderConfirmed` dispara solicitud de notificacion no bloqueante | E2E | `order-service`, `notification-service` | [Notificaciones E2E](/mvp/calidad/pruebas/servicio-notificaciones/e2e/) (`NOTI-E2E-001..014`) | `OrderConfirmed` + `NotificationRequested` + core consistente | Listo para implementar |
| FR-007 + RN-REP-01 + I-REP-01 | generacion de reporte semanal sin mutacion de core transaccional | E2E | `reporting-service` | [Reporteria E2E](/mvp/calidad/pruebas/servicio-reporteria/e2e/) (`REP-E2E-001..018`) | `WeeklyReportGenerated` + evidencia read-only sobre core | Listo para implementar |
| FR-008 + RN-NOTI-01 | `CartAbandonedDetected` genera flujo de recordatorio | Integracion | `order-service`, `notification-service` | [Notificaciones Integracion](/mvp/calidad/pruebas/servicio-notificaciones/integracion/) (`NOTI-IT-001..028`) | evento de abandono + solicitud de envio + dedupe/retry | Listo para implementar |
| FR-009 + RN-ACC-01 + I-ACC-01 | login/refresh/logout con sesion valida por tenant y rol | Integracion | `identity-access-service` | [IAM Integracion](/mvp/calidad/pruebas/servicio-identidad-acceso/integracion/) (`IAM-IT-001..018`) | request/response + `UserLoggedIn` + `auth_audit` | Listo para implementar |
| FR-010 + RN-PAY-01 + I-PAY-01 | registro de pago manual recalcula estado de pago y evita duplicado | Integracion | `order-service` | [Pedidos Integracion](/mvp/calidad/pruebas/servicio-pedidos/integracion/) (`ORD-IT-001..030`) | `OrderPaymentRegistered` + estado actualizado + control de idempotencia | Listo para implementar |
| FR-011 + RN-LOC-01 + I-LOC-01 | operacion critica sin politica por `countryCode` se bloquea | E2E | `directory-service`, `order-service`, `reporting-service` | [Directorio E2E](/mvp/calidad/pruebas/servicio-directorio/e2e/) (`DIR-E2E-001..012`) | error `configuracion_pais_no_disponible` + auditoria de bloqueo | Listo para implementar |
| FR-011 + I-LOC-01 | consulta tecnica de politica regional inexistente devuelve ausencia de recurso | Integracion | `directory-service` | [Directorio Integracion](/mvp/calidad/pruebas/servicio-directorio/integracion/) (`DIR-IT-017`) | `404 configuracion_pais_no_disponible` en resolucion directa del recurso | Listo para implementar |
| FR-011 + I-LOC-01 | operacion de negocio bloqueada por ausencia regional aunque la consulta tecnica subyacente sea ausencia de recurso | Integracion | `order-service`, `reporting-service` | [Pedidos Integracion](/mvp/calidad/pruebas/servicio-pedidos/integracion/) (`ORD-IT-027`) + [Reporteria Integracion](/mvp/calidad/pruebas/servicio-reporteria/integracion/) (`REP-IT-007`, `REP-IT-014`) | `409 configuracion_pais_no_disponible` en capa de negocio + auditoria | Listo para implementar |
| NFR-001 | latencia p95 de APIs core dentro de presupuesto | E2E no funcional | `order-service`, `inventory-service`, `catalog-service`, `directory-service` | [Puertas de Calidad - Gate 4](/mvp/calidad/puertas-calidad/) + [Notas de Cobertura](/mvp/calidad/notas-cobertura/) | reporte de performance con p95 por flujo | Listo para implementar |
| NFR-002 | generacion de reporte semanal <= 15 min | E2E no funcional | `reporting-service` | [Reporteria E2E](/mvp/calidad/pruebas/servicio-reporteria/e2e/) (`REP-E2E-001..018`) | duracion de corrida semanal por `tenant+week+type` | Listo para implementar |
| NFR-003 | disponibilidad mensual >= 99.5% en ventana 06:00-22:00 | Operacional | Todos los servicios core | [Puertas de Calidad - Gate 4](/mvp/calidad/puertas-calidad/) | reporte SLI/SLO de disponibilidad por ventana operativa | Listo para implementar |
| NFR-004 | sobreventa semanal <= 1.0% | Integracion + E2E | `inventory-service`, `order-service` | [Inventario Integracion](/mvp/calidad/pruebas/servicio-inventario/integracion/) + [Pedidos E2E](/mvp/calidad/pruebas/servicio-pedidos/e2e/) | metrica de sobreventa semanal + incidentes asociados | Listo para implementar |
| NFR-005 + D-CROSS-01 | toda mutacion persiste `tenantId` y respeta aislamiento | Integracion | Todos los servicios core | [Pruebas por servicio](/mvp/calidad/pruebas/) (suites de integracion) | evidencia DB + respuestas sin fuga cross-tenant | Listo para implementar |
| NFR-006 | mutaciones criticas con `traceId`, `correlationId`, `actorId`, `tenantId` | Integracion | Todos los servicios | [Convenciones de Prueba](/mvp/calidad/convenciones-prueba/) + suites integracion | logs/eventos/auditoria con metadatos completos | Listo para implementar |
| NFR-007 | observabilidad operacional minima en mutaciones y consumo async (incluye retry/DLQ instrumentado) | Integracion | `notification-service`, `order-service` | [Notificaciones Integracion](/mvp/calidad/pruebas/servicio-notificaciones/integracion/) (`NOTI-IT-001..028`) | `traceId` en respuestas mutantes + alertas/metricas + evidencia de retry/DLQ | Listo para implementar |
| NFR-008 | carga 3x baseline con degradacion <= 30% de p95 NFR-001 | E2E no funcional | `order-service`, `inventory-service`, `catalog-service` | [Puertas de Calidad - Gate 4](/mvp/calidad/puertas-calidad/) | comparativo baseline vs prueba de estres | Listo para implementar |
| NFR-009 | compatibilidad de contratos v1 (API/eventos) sin breaking no versionado | Integracion | Todos los servicios | [Puertas de Calidad - Gate 1/Gate 2](/mvp/calidad/puertas-calidad/) + suites de contratos por servicio | resultados de contract tests producer/consumer | Listo para implementar |
| NFR-009 | build de artefacto de empaquetado por servicio sin secretos embebidos y con config externa | Integracion tecnica | Todos los servicios | [Puertas de Calidad - Gate 1/Gate 2](/mvp/calidad/puertas-calidad/) | logs de build/arranque + validacion de config/secretos externos | Listo para implementar |
| NFR-003 + NFR-007 | arranque reproducible de stack de integracion (mecanismo multi-contenedor) | Integracion tecnica + smoke | servicios core + dependencias compartidas | [Puertas de Calidad - Gate 2](/mvp/calidad/puertas-calidad/) | evidencia de stack levantado + health/smoke + telemetria minima | Listo para implementar |
| NFR-010 | politica de retencion y borrado/anonimizacion por tipo de dato | Integracion + Operacional | Todos los servicios (foco IAM/Directory/Reporting) | [Notas de Cobertura](/mvp/calidad/notas-cobertura/) + suites de integracion | evidencia de retencion/masking/borrado con trazabilidad | Listo para implementar |
| NFR-011 + RN-LOC-01 | operacion regional sin fallback global implicito | E2E + Operacional | `directory-service`, `order-service`, `reporting-service` | [Directorio E2E](/mvp/calidad/pruebas/servicio-directorio/e2e/) + [Puertas de Calidad - Gate 4](/mvp/calidad/puertas-calidad/) | salidas consistentes por `countryCode` + bloqueos auditados | Listo para implementar |

## Cobertura minima obligatoria (control documental)
| Grupo | Cobertura requerida | Estado documental |
|---|---|---|
| FR core (`checkout`, `pedido`, `stock`, `auth`, `notificaciones`, `reportes`, `catalogo`) | al menos 1 escenario E2E o integracion por flujo critico + artefacto referenciado | Listo para implementar |
| NFR clave (`latencia`, `disponibilidad`, `sobreventa`, `aislamiento tenant`, `trazabilidad`, `retencion`, `regionalizacion`) | al menos 1 escenario verificable por NFR en matriz | Listo para implementar |

## Historial
- 2026-03-28: se normaliza la taxonomia de estados y se completa la matriz maestra con cobertura explicita `FR-001..FR-011` y `NFR-001..NFR-011`.
