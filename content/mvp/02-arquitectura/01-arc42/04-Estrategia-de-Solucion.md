---
title: "Estrategia de Solucion"
linkTitle: "4. Estrategia de Solucion"
weight: 4
url: "/mvp/arquitectura/arc42/estrategia-solucion/"
---

## Proposito
Definir principios de diseno y estrategia tecnica para construir ArkaB2B de forma consistente.

## Principios arquitectonicos
| ID | Principio | Regla de aplicacion |
|---|---|---|
| P-01 | Dominio primero | invariantes y reglas en `domain`/`application`, no en adapters |
| P-02 | Bounded contexts explicitos | cada servicio con ownership de datos y contratos propios |
| P-03 | Sync + async por criticidad | sync en validaciones criticas, async en propagacion/side effects |
| P-04 | Idempotencia por diseno | comandos mutantes con clave idempotente |
| P-05 | Event-driven con outbox | publicacion confiable de hechos sin XA |
| P-06 | Seguridad en capas | control en borde + control en dominio |
| P-07 | Observabilidad built-in | trazabilidad completa de requests/comandos/eventos |
| P-08 | Evolucion gobernada | cambios estructurales con ADR y versionado |

## Estilo tecnico
- Arquitectura general: microservicios por bounded context.
- Arquitectura interna: hexagonal/clean + CQRS ligero.
- Stack: Java 21 + Spring (WebFlux para APIs/eventos) + broker + cache.
- Persistencia: database-per-service + outbox/inbox + modelos read-side derivados.

## Estrategia por concern
| Concern | Estrategia | Evidencia |
|---|---|---|
| Consistencia de checkout | reserva y confirmacion coordinada con inventory | [Casos de Uso en Ejecucion de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/arquitectura-interna/casos-uso-ejecucion/) |
| Aislamiento tenant | tenant/rol en token + validacion en dominio | [Arquitectura de Seguridad IAM](/mvp/arquitectura/servicios/servicio-identidad-acceso/seguridad/arquitectura-seguridad/) |
| Evolucion de contratos | versionado + compatibilidad + contract testing | [Contratos por Servicio](/mvp/arquitectura/servicios/) |
| Rendimiento | presupuestos por servicio + tuning por endpoint/flujo | [Presupuestos de Rendimiento por Servicio](/mvp/arquitectura/servicios/) |
| Trazabilidad | eventos y logs con correlation metadata | [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [Seguridad por Servicio](/mvp/arquitectura/servicios/) |
| Regionalizacion operativa | parametros por `countryCode` versionados en Directory y resueltos en runtime por Order/Reporting | [APIs de Directorio](/mvp/arquitectura/servicios/servicio-directorio/contratos/apis/), [APIs de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/contratos/apis/), [Casos de Uso en Ejecucion de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/arquitectura-interna/casos-uso-ejecucion/) |

## Trade-offs explicitos
| Decision | Beneficio | Costo/impacto |
|---|---|---|
| Microservicios por bounded context | cohesion semantica y escalado independiente | mayor complejidad operacional |
| Sync + async combinados | consistencia fuerte donde importa y desacople en side effects | mayor esfuerzo de orquestacion y observabilidad |
| DB por servicio | ownership claro de datos | reconciliacion entre contextos por eventos |
| Reactivo (WebFlux) | eficiencia I/O en picos | curva de aprendizaje del equipo |

## Anti-patrones prohibidos
- Logica de negocio en controllers/listeners/adapters.
- Acceso a DB de otro servicio.
- Mezclar semanticas `vendible` (catalog) y `disponibilidad` (inventory).
- Eventos sin `eventId`, `traceId`, `correlationId`, `tenantId`.
- Uso de reporting para decisiones transaccionales.

## Mapa estrategia -> NFR
| Estrategia | NFR principal |
|---|---|
| cache selectiva + optimizacion de consultas | NFR-001 |
| pipelines de reporte por lote | NFR-002 |
| resiliencia y failover operativo | NFR-003 |
| control de reservas y confirmacion | NFR-004 |
| authz tenant/rol en toda mutacion | NFR-005 |
| auditoria completa de mutaciones | NFR-006 |
| estandar de observabilidad | NFR-007 |
| escalado horizontal + particionado | NFR-008 |
| quality gates + contract tests | NFR-009 |
| retencion/anonimizacion por tipo de dato | NFR-010 |
| resolucion obligatoria de politica por pais en operaciones criticas | NFR-011 |
