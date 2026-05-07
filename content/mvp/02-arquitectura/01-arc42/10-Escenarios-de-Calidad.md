---
title: "Escenarios de Calidad"
linkTitle: "10. Escenarios de Calidad"
weight: 10
url: "/mvp/arquitectura/arc42/escenarios-calidad/"
---

## Proposito
Conectar NFR con escenarios verificables de arquitectura, pruebas y operacion.

## Escenarios por NFR
| NFR | Estimulo | Respuesta esperada | Medida |
|---|---|---|---|
| NFR-001 | Pico de lecturas/mutaciones en APIs core | sistema mantiene p95 dentro de presupuesto | p95 <= 800ms lectura / <= 1500ms mutacion |
| NFR-002 | Ejecucion de reporte semanal por tenant | reporte disponible dentro de ventana operativa | <= 15 min |
| NFR-003 | Falla parcial de dependencia no critica | servicio degrada sin caida total | disponibilidad mensual >= 99.5% |
| NFR-004 | Checkout concurrente sobre SKU caliente | no confirmar pedido sin disponibilidad real | sobreventa <= 1.0% semanal |
| NFR-005 | Intento de acceso cross-tenant | operacion rechazada y auditada | 0 incidentes criticos |
| NFR-006 | Mutacion de stock/pedido/pago | evento/log guarda metadata de actor y correlacion | >= 99% mutaciones trazables |
| NFR-007 | Incidente de latencia o error rate | alerta y trazabilidad disponible para diagnostico | 100% mutaciones con `traceId` |
| NFR-008 | Carga 3x baseline | degradacion controlada sin perdida de consistencia | <= 30% degradacion p95 objetivo |
| NFR-009 | PR de cambio en contratos/core | quality gates y tests pasan antes de merge | 100% PR con tests/lint; cobertura >= 70% core |
| NFR-010 | Solicitud de retencion/borrado | flujo aplicado segun politica de datos | <= 30 dias cuando aplique |
| NFR-011 | Checkout o reporte semanal en tenant/pais sin politica vigente | operacion bloqueada con error semantico estable y auditada | 0 mutaciones/reporte semanal exitosos sin politica por `countryCode`; 100% errores `configuracion_pais_no_disponible` trazables |

## Escenarios de estres prioritarios
- ES-01: checkout concurrente + expiracion de reservas + retries.
- ES-02: backlog de broker + reproceso idempotente en notification/reporting.
- ES-03: degradacion de proveedor de notificaciones sin afectar core.
- ES-04: recomputo de reportes semanales durante pico de eventos.

## Relacion con artefactos
| Tipo de validacion | Artefacto |
|---|---|
| Budgets por servicio | [Servicios](/mvp/arquitectura/servicios/) -> categoria `Rendimiento` |
| Seguridad y auditoria | [Servicios](/mvp/arquitectura/servicios/) -> categoria `Seguridad` |
| Runtime critico | [Servicios](/mvp/arquitectura/servicios/) -> categoria `Arquitectura Interna` |
| Salida a operacion | [Trazabilidad de Arquitectura](/mvp/arquitectura/trazabilidad/) (`Salida para operacion`) |
| Trazabilidad del ciclo | [Trazabilidad de Producto](/mvp/producto/trazabilidad/), [Trazabilidad de Arquitectura](/mvp/arquitectura/trazabilidad/) |
