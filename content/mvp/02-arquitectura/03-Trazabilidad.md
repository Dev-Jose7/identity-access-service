---
title: "Trazabilidad de Arquitectura"
linkTitle: "3. Trazabilidad"
weight: 3
url: "/mvp/arquitectura/trazabilidad/"
---

## Marco de trazabilidad
_Responde: como se verifica que arquitectura materializa RF/RNF y preserva el baseline de dominio sin contradicciones._

La trazabilidad del pilar de arquitectura conecta requisitos de
producto, decisiones de dominio y artefactos tecnicos globales/locales
para `MVP`.

## Criterio de cierre
_Responde: cuando un requisito se considera cubierto por arquitectura y que queda explicitamente para pilares posteriores._
- `Cubierto en arquitectura`: existe servicio owner, vista global/local y decision tecnica coherente con dominio.
- `Salida para pilar posterior`: el requisito ya esta diseniado, pero la evidencia de prueba u operacion vive en calidad/u operacion.

## Cobertura funcional
_Responde: que requisitos funcionales aterriza arquitectura, con que servicios y en que artefactos queda la evidencia._
| RF | Servicios principales | Evidencia de arquitectura | Estado |
|---|---|---|---|
| FR-001 | `catalog-service` | [Vista de Bloques](/mvp/arquitectura/arc42/vista-bloques/), [Servicio de Catalogo](/mvp/arquitectura/servicios/servicio-catalogo/), [APIs de Catalogo](/mvp/arquitectura/servicios/servicio-catalogo/contratos/apis/) | Cubierto en arquitectura |
| FR-002 | `inventory-service` | [Vista de Bloques](/mvp/arquitectura/arc42/vista-bloques/), [Servicio de Inventario](/mvp/arquitectura/servicios/servicio-inventario/), [Modelo Fisico de Inventario](/mvp/arquitectura/servicios/servicio-inventario/datos/modelo-fisico/) | Cubierto en arquitectura |
| FR-003 | `inventory-service`, `reporting-service` | [Vista de Ejecucion](/mvp/arquitectura/arc42/vista-ejecucion/), [Servicio de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/), [Eventos de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/contratos/eventos/) | Cubierto en arquitectura |
| FR-004 | `order-service`, `inventory-service`, `catalog-service`, `directory-service` | [Contexto y Alcance](/mvp/arquitectura/arc42/contexto-alcance/), [Vista de Ejecucion](/mvp/arquitectura/arc42/vista-ejecucion/), [Servicio de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/) | Cubierto en arquitectura |
| FR-005 | `order-service`, `inventory-service` | [Casos de Uso en Ejecucion de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/arquitectura-interna/casos-uso-ejecucion/), [Eventos de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/contratos/eventos/) | Cubierto en arquitectura |
| FR-006 | `order-service`, `notification-service` | [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [Servicio de Notificaciones](/mvp/arquitectura/servicios/servicio-notificaciones/) | Cubierto en arquitectura |
| FR-007 | `reporting-service`, `order-service` | [Escenarios de Calidad](/mvp/arquitectura/arc42/escenarios-calidad/), [Servicio de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/) | Cubierto en arquitectura |
| FR-008 | `order-service`, `notification-service` | [Vista de Ejecucion](/mvp/arquitectura/arc42/vista-ejecucion/), [Servicio de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/), [Servicio de Notificaciones](/mvp/arquitectura/servicios/servicio-notificaciones/) | Cubierto en arquitectura |
| FR-009 | `identity-access-service`, `directory-service` | [Restricciones](/mvp/arquitectura/arc42/restricciones/), [Servicio de Identidad y Acceso](/mvp/arquitectura/servicios/servicio-identidad-acceso/), [Arquitectura de Seguridad IAM](/mvp/arquitectura/servicios/servicio-identidad-acceso/seguridad/arquitectura-seguridad/) | Cubierto en arquitectura |
| FR-010 | `order-service`, `notification-service`, `reporting-service` | [Vista de Ejecucion](/mvp/arquitectura/arc42/vista-ejecucion/), [Servicio de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/), [Servicio de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/) | Cubierto en arquitectura |
| FR-011 | `directory-service`, `order-service`, `reporting-service` | [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [Servicio de Directorio](/mvp/arquitectura/servicios/servicio-directorio/), [Servicio de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/) | Cubierto en arquitectura |

## Cobertura no funcional
_Responde: que requisitos no funcionales quedan explicitamente resueltos en arquitectura y donde queda la evidencia._
| RNF | Decisiones/artefactos de arquitectura | Estado |
|---|---|---|
| NFR-001 | [Introduccion y Objetivos](/mvp/arquitectura/arc42/introduccion-objetivos/), [Escenarios de Calidad](/mvp/arquitectura/arc42/escenarios-calidad/), [Rendimiento de IAM](/mvp/arquitectura/servicios/servicio-identidad-acceso/rendimiento/), [Rendimiento de Directorio](/mvp/arquitectura/servicios/servicio-directorio/rendimiento/), [Rendimiento de Catalogo](/mvp/arquitectura/servicios/servicio-catalogo/rendimiento/), [Rendimiento de Inventario](/mvp/arquitectura/servicios/servicio-inventario/rendimiento/), [Rendimiento de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/rendimiento/), [Rendimiento de Notificaciones](/mvp/arquitectura/servicios/servicio-notificaciones/rendimiento/), [Rendimiento de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/rendimiento/) | Cubierto en arquitectura |
| NFR-002 | [Escenarios de Calidad](/mvp/arquitectura/arc42/escenarios-calidad/), [Servicio de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/) | Cubierto en arquitectura |
| NFR-003 | [Vista de Despliegue](/mvp/arquitectura/arc42/vista-despliegue/), [Riesgos y Deuda Tecnica](/mvp/arquitectura/arc42/riesgos-deuda-tecnica/) | Cubierto en arquitectura |
| NFR-004 | [Restricciones](/mvp/arquitectura/arc42/restricciones/), [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [Servicio de Inventario](/mvp/arquitectura/servicios/servicio-inventario/), [Servicio de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/) | Cubierto en arquitectura |
| NFR-005 | [Restricciones](/mvp/arquitectura/arc42/restricciones/), [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [Seguridad IAM](/mvp/arquitectura/servicios/servicio-identidad-acceso/seguridad/arquitectura-seguridad/), [Seguridad de Directorio](/mvp/arquitectura/servicios/servicio-directorio/seguridad/arquitectura-seguridad/), [Seguridad de Catalogo](/mvp/arquitectura/servicios/servicio-catalogo/seguridad/arquitectura-seguridad/), [Seguridad de Inventario](/mvp/arquitectura/servicios/servicio-inventario/seguridad/arquitectura-seguridad/), [Seguridad de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/seguridad/arquitectura-seguridad/), [Seguridad de Notificaciones](/mvp/arquitectura/servicios/servicio-notificaciones/seguridad/arquitectura-seguridad/), [Seguridad de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/seguridad/arquitectura-seguridad/) | Cubierto en arquitectura |
| NFR-006 | [Introduccion y Objetivos](/mvp/arquitectura/arc42/introduccion-objetivos/), [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [Seguridad IAM](/mvp/arquitectura/servicios/servicio-identidad-acceso/seguridad/arquitectura-seguridad/), [Seguridad de Directorio](/mvp/arquitectura/servicios/servicio-directorio/seguridad/arquitectura-seguridad/), [Seguridad de Catalogo](/mvp/arquitectura/servicios/servicio-catalogo/seguridad/arquitectura-seguridad/), [Seguridad de Inventario](/mvp/arquitectura/servicios/servicio-inventario/seguridad/arquitectura-seguridad/), [Seguridad de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/seguridad/arquitectura-seguridad/), [Seguridad de Notificaciones](/mvp/arquitectura/servicios/servicio-notificaciones/seguridad/arquitectura-seguridad/), [Seguridad de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/seguridad/arquitectura-seguridad/) | Cubierto en arquitectura |
| NFR-007 | [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [Vista de Ejecucion](/mvp/arquitectura/arc42/vista-ejecucion/), [Servicio de Notificaciones](/mvp/arquitectura/servicios/servicio-notificaciones/), [Servicio de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/) | Cubierto en arquitectura |
| NFR-008 | [Introduccion y Objetivos](/mvp/arquitectura/arc42/introduccion-objetivos/), [Rendimiento de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/rendimiento/), [Rendimiento de Notificaciones](/mvp/arquitectura/servicios/servicio-notificaciones/rendimiento/), [Rendimiento de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/rendimiento/) | Cubierto en arquitectura |
| NFR-009 | [Decisiones Arquitectonicas](/mvp/arquitectura/arc42/decisiones-arquitectonicas/), [Vista de Despliegue](/mvp/arquitectura/arc42/vista-despliegue/), [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [ADR](/mvp/arquitectura/arc42/adr/), [Escenarios de Calidad](/mvp/arquitectura/arc42/escenarios-calidad/), [Contratos de Servicios](/mvp/arquitectura/servicios/) | Cubierto en arquitectura |
| NFR-010 | [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [Datos de IAM](/mvp/arquitectura/servicios/servicio-identidad-acceso/datos/), [Datos de Directorio](/mvp/arquitectura/servicios/servicio-directorio/datos/), [Datos de Catalogo](/mvp/arquitectura/servicios/servicio-catalogo/datos/), [Datos de Inventario](/mvp/arquitectura/servicios/servicio-inventario/datos/), [Datos de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/datos/), [Datos de Notificaciones](/mvp/arquitectura/servicios/servicio-notificaciones/datos/), [Datos de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/datos/) | Cubierto en arquitectura |
| NFR-011 | [Restricciones](/mvp/arquitectura/arc42/restricciones/), [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/), [Servicio de Directorio](/mvp/arquitectura/servicios/servicio-directorio/), [Servicio de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/), [Servicio de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/) | Cubierto en arquitectura |

## Preservacion de verdades del dominio
_Responde: que decisiones de dominio quedan preservadas explicitamente por arquitectura._
| Verdad del dominio | Evidencia en arquitectura | Preservacion |
|---|---|---|
| `OrderCreated` no equivale a `OrderConfirmed` | `order-service`, runtime y contratos de eventos | separar creacion de pedido de confirmacion comercial |
| reporting no muta core | `reporting-service`, restricciones y conceptos transversales | APIs read-only + consumo async |
| notification no bloquea core | runtime, contratos y servicio de notificaciones | retries y DLQ sin rollback de pedido/pago |
| politica regional por pais obligatoria | `directory-service`, `order-service`, `reporting-service` | `configuracion_pais_no_disponible` con mapeo intencional: `404` en resolucion tecnica (`directory`) y `409` en bloqueo de operacion de negocio (`order`/`reporting`) |
| baseline RBAC minimo del MVP | `arc42/conceptos-transversales`, IAM y contratos API de servicios | enforcement por tenant/rol/scope en borde y en `use cases`; granularidad fina por pais queda en evolucion |
| MFA en cuentas administrativas Arka | `arc42/decisiones-arquitectonicas`, seguridad IAM/Order | diferido a hardening posterior; no bloquea el freeze del baseline arquitectonico `MVP` |
| `JWKS` como postura oficial de verificacion | `arc42/decisiones-arquitectonicas`, seguridad de IAM y gateway | distribucion estandar de llaves publicas para validacion JWT en componentes autorizados |
| tenant y actor en toda mutacion | IAM, seguridad transversal y contratos | metadata obligatoria y doble enforcement |
| fulfillment extendido reservado | dominio Order, contratos y runtime de `order-service` | `READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED` fuera del baseline operativo actual |

## Salida para calidad
_Responde: que artefactos debe consumir el pilar de calidad sin reinterpretar decisiones de arquitectura._
- vistas runtime para escenarios de integracion y resiliencia;
- contratos API/eventos para contract testing;
- budgets de rendimiento por servicio;
- controles de seguridad y amenazas por servicio;
- escenarios de calidad vinculados a RNF.

## Salida para operacion
_Responde: que artefactos debe consumir el pilar de operacion para cerrar observabilidad y despliegue._
- topologia, segmentacion y reglas de promocion;
- estandar de artefacto versionado por servicio y stack reproducible de
  integracion para local/dev/qa;
- SLI/SLO y presupuestos por servicio;
- puntos de contencion, backlog y alertas esperadas;
- runbooks minimos declarados en seguridad/runtime;
- riesgos tecnicos aceptados y mitigaciones.

## Estado de cobertura
_Responde: que tan cerrado queda el pilar de arquitectura en el ciclo actual._
- RF: `11/11` aterrizados en servicios y vistas de arquitectura.
- RNF: `11/11` con decision, servicio o artefacto tecnico identificado.
- La validacion ejecutable queda como salida para `03-calidad` y `04-operacion`.

## Historial de trazabilidad
_Responde: que cambios relevantes alteraron la matriz y su impacto._
| Fecha | Tipo | Cambio | Impacto |
|---|---|---|---|
| 2026-03-13 | Baseline | Se materializa `02-arquitectura` a partir del baseline vigente de producto y dominio del ciclo. | Arquitectura queda trazada a RF/RNF y salida a pilares posteriores. |
| 2026-03-28 | Consolidacion | Se completa la trazabilidad transversal de NFR por servicio (rendimiento, seguridad y datos) y se explicita preservacion del baseline RBAC minimo. | Se reduce ambiguedad de cobertura y se mejora consistencia entre trazabilidad global y servicios. |
| 2026-03-28 | Saneamiento de decisiones residuales | Se cierra plataforma base (`AWS` objetivo, `Railway` fallback, `LocalStack` local/dev), baseline operativo transversal, postura oficial `JWKS`, retencion minima por clase de dato y regionalizacion sin fallback global implicito; MFA se reclasifica a hardening posterior no bloqueante. | El registro de decisiones deja de bloquear el freeze del baseline y mejora la consistencia con calidad y operacion. |
