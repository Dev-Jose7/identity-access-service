---
title: "Matriz de Resultados Esperados"
linkTitle: "6.2 Matriz"
weight: 2
url: "/mvp/calidad/evidencias/matriz-resultados-esperados/"
---

## Proposito
Definir resultados esperados del baseline y distinguir explicitamente que casos forman parte de la certificacion minima.

## Matriz
| ID caso/suite | FR/NFR cubiertos | Servicio(s) | Tipo de prueba | Estado actual | Evidencia requerida | Criterio de aceptacion | Certificacion minima |
|---|---|---|---|---|---|---|---|
| IAM-UT-CRIT | FR-009, NFR-005 | `identity-access-service` | Unitarias | Listo para implementar | resultado de runner + cobertura de reglas IAM | sesion/rol/tenant invalido rechazado sin side effects | Si |
| DIR-UT-CRIT | FR-011, NFR-011 | `directory-service` | Unitarias | Listo para implementar | resultado de runner + asserts de ownership/politica regional | ownership y politica por pais validados | Si |
| CAT-UT-CRIT | FR-001, FR-004 | `catalog-service` | Unitarias | Listo para implementar | resultado de runner + asserts de vendibilidad/precio | variante no vendible rechazada; precio vigente consistente | Si |
| INV-UT-CRIT | FR-002, NFR-004 | `inventory-service` | Unitarias | Listo para implementar | resultado de runner + invariantes de stock/reserva | nunca `stock_fisico < 0`; reserva valida por estado | Si |
| ORD-UT-CRIT | FR-004, FR-005, FR-010 | `order-service` | Unitarias | Listo para implementar | resultado de runner + transiciones/pago idempotente | transiciones invalidas fallan con error canonico | Si |
| NOTI-UT-CRIT | FR-006, FR-008, NFR-007 | `notification-service` | Unitarias | Listo para implementar | resultado de runner + politicas retry/discard | notificacion fallida no bloquea core | Si |
| REP-UT-CRIT | FR-007, NFR-002 | `reporting-service` | Unitarias | Listo para implementar | resultado de runner + dedupe/proyeccion | reporting no muta core y dedupe activo | Si |
| ORD-INV-IT | FR-004, FR-005, NFR-004 | `order-service`, `inventory-service` | Integracion | Listo para implementar | request/response + DB + outbox/evento | reserva/confirmacion coherente sin sobreventa | Si |
| ORD-CAT-IT | FR-001, FR-004 | `order-service`, `catalog-service` | Integracion | Listo para implementar | request/response + snapshot de precio/vendible | checkout solo con variante vendible y precio vigente | Si |
| ORD-DIR-IT | FR-004, FR-011, NFR-011 | `order-service`, `directory-service` | Integracion | Listo para implementar | request/response + evidencia de politica regional | bloqueo con `configuracion_pais_no_disponible` cuando aplique | Si |
| ORD-NOTI-IT | FR-006, NFR-007 | `order-service`, `notification-service` | Integracion | Listo para implementar | eventos `OrderConfirmed`/`Notification*` + logs | side effect no bloqueante y retry controlado | Si |
| CORE-REP-IT | FR-003, FR-007 | `reporting-service` + productores core | Integracion | Listo para implementar | consumo de eventos + dedupe + proyeccion | consolidacion derivada idempotente | Si |
| E2E-CORE-HAPPY | FR-004, FR-005, FR-010 | `order`, `inventory`, `catalog`, `directory` | E2E | Listo para implementar | traza completa + evidencia API/DB/eventos | compra feliz en flujo core sin desviaciones semanticas | Si |
| E2E-REGIONAL-BLOCK | FR-011, NFR-011 | `directory`, `order`, `reporting` | E2E | Listo para implementar | error canonico + auditoria de bloqueo | operacion critica se bloquea sin fallback global implicito | Si |
| E2E-STOCK-BLOCK | FR-002, FR-004, NFR-004 | `inventory`, `order` | E2E | Listo para implementar | respuesta de rechazo + no side effects indebidos | falta de disponibilidad/reserva bloquea checkout/pedido | Si |
| NFR-TENANT-ISO | NFR-005, D-CROSS-01 | servicios core | Integracion/E2E no funcional | Listo para implementar | evidencias cross-tenant negativas + auditoria | cero acceso cruzado en mutaciones core | Si |
| NFR-IDEMP-CORE | NFR-004, NFR-009 | `order`, `inventory`, `notification`, `reporting` | Integracion | Listo para implementar | replay/retry + dedupe (`processed_event`) | mismo comando/evento no duplica efectos | Si |
| NFR-CONTRACT-V1 | NFR-009 | todos | Integracion | Listo para implementar | reportes de contract tests producer/consumer | sin breaking no versionado en API/eventos `v1` | Si |
| TECH-DOCKER-BUILD | NFR-009 | todos | Integracion tecnica | Listo para implementar | evidencia de build de imagen por servicio + arranque tecnico | imagen construye y arranca con config externa, sin secretos embebidos | Si |
| TECH-COMPOSE-SMOKE | NFR-003, NFR-007, NFR-009 | stack core + dependencias de integracion | Integracion tecnica/E2E smoke | Listo para implementar | evidencia de `docker compose` up + health/smoke + telemetria minima | stack reproducible disponible para pruebas de integracion | Si |
| NFR-CANONICAL-ERRORS | FR-004, FR-005, FR-011 | `order`, `inventory`, `directory` | Integracion/E2E | Listo para implementar | respuestas con error canonico estable | errores de negocio estables y trazables | Si |
| NFR-PERF-BASELINE | NFR-001 | `order`, `inventory`, `catalog`, `directory` | E2E no funcional | Listo para implementar | reporte p95 por flujo critico | p95 dentro de presupuesto definido | Si |
| NFR-SEC-TRACE | NFR-006, NFR-007 | todos | Integracion | Listo para implementar | logs/trazas/auditoria con metadatos | `traceId`/`correlationId`/`tenantId`/`actorId` presentes | Si |
| NFR-DISPO-SLO | NFR-003 | plataforma completa | Operacional | Listo para implementar | reporte SLI/SLO por ventana operativa | disponibilidad mensual conforme objetivo | No |
| NFR-RETENCION-FINA | NFR-010 | IAM/Directory/Reporting (foco) | Integracion/Operacional | Listo para implementar | evidencia de retencion/anonimizado/borrado | politicas minimas aplicadas por tipo de dato | No |

## Nota de uso
- Esta matriz define resultados esperados; no sustituye corridas reales.
- El cambio de estado a `Ejecutado` o `Validado con evidencia` requiere registro en:
  - [Registro de Ejecuciones](/mvp/calidad/evidencias/registro-ejecuciones/)
  - [Acta de Certificacion Minima](/mvp/calidad/evidencias/acta-certificacion-minima/)
