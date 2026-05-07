---
title: "Modelo de Ambientes"
linkTitle: "2. Modelo de Ambientes"
weight: 2
url: "/mvp/operacion/modelo-ambientes/"
---

## Proposito
Definir ambientes oficiales, su uso operativo y los criterios minimos para
promocion y estabilidad.

## Ambientes oficiales
| Ambiente | Proposito operativo | Usuarios principales | Estabilidad esperada | Dependencias reales vs emuladas | Datos permitidos | Observabilidad minima | Restricciones clave |
|---|---|---|---|---|---|---|---|
| `local` | desarrollo y validacion tecnica individual | devs/copilotos | baja-media | emuladas/locales (`LocalStack`, contenedores locales, stubs) | sinteticos/seed | logs locales + trazas basicas por flujo | sin datos reales, sin llaves reales, sin trafico externo productivo |
| `dev` | integracion temprana de cambios por servicio | devs + QA tecnico | media | mezcla de dependencias compartidas y emuladas controladas | sinteticos anonimizados | logs estructurados + metricas base + trazas de mutaciones | no validar SLO finales ni decisiones de capacidad |
| `qa` | validacion funcional y certificacion minima de release candidate | QA + owners tecnicos | alta | mayormente reales de stack objetivo, con datos de prueba | anonimizados y datasets controlados | dashboards por servicio + alertas de smoke + trazas e2e | no usar para operacion comercial ni decisiones de negocio final |
| `prod` | operacion real del MVP | operaciones + soporte + negocio | muy alta | reales sobre plataforma objetivo `AWS` | productivos bajo politica de privacidad | observabilidad completa, alertas activas, auditoria operativa | cambios solo por pipeline, gates y ventanas controladas |

## Mecanismo de ejecucion por ambiente
| Ambiente | Mecanismo estandar |
|---|---|
| `local` | stack de integracion reproducible (mecanismo multi-contenedor) con servicios + dependencias locales |
| `dev` | ejecucion reproducible por artefacto versionado de servicio y stack de integracion para smoke |
| `qa` | artefacto versionado por servicio; stack de integracion permitido para pruebas controladas |
| `prod` | despliegue de artefacto versionado sobre plataforma objetivo (`AWS`) |

Referencia canonicidad:
- ver formulacion oficial de empaquetado/ejecucion reproducible en
  [Decisiones Arquitectonicas](/mvp/arquitectura/arc42/decisiones-arquitectonicas/).

## Cambios permitidos por ambiente
| Ambiente | Permitido | No permitido |
|---|---|---|
| `local` | cambios experimentales, datos seed, pruebas de compatibilidad | pruebas con datos reales, bypass de seguridad base, publicar eventos reales externos |
| `dev` | cambios incrementales por PR, ajustes tacticos de parametros no criticos | cambios sin trazabilidad, cambios breaking de contrato sin versionado |
| `qa` | validacion de release candidate y migraciones aprobadas | cambios ad-hoc directos sin pipeline, rotacion manual no trazada de secretos |
| `prod` | releases aprobados, cambios de emergencia con incidente abierto y evidencia | cambios manuales fuera de pipeline, activacion de capacidades fuera de baseline |

## Reglas de consistencia por ambiente
- Todo ambiente mutante exige `tenantId`, `actorId`, `traceId`, `correlationId`.
- `qa` y `prod` deben mantener contratos API/eventos coherentes por version.
- `prod` no admite fallback global para regionalizacion.
- Si falta politica regional vigente por `countryCode`, se bloquea la
  operacion critica en cualquier ambiente operativo.
- `dev`, `qa` y `prod` deben aplicar el mismo vocabulario de errores canonicos.
- `prod` requiere llaves/secretos exclusivos por entorno, sin reutilizacion con
  otros ambientes.
- Todo artefacto de servicio debe recibir configuracion por entorno y no contener
  secretos embebidos.

## Criterios operativos minimos por ambiente
| Ambiente | Criterio minimo de entrada | Criterio minimo de salida |
|---|---|---|
| `local` | servicios core ejecutables con seed coherente | cambios listos para integracion en `dev` con smoke local |
| `dev` | smoke de salud + pruebas unitarias/integracion del cambio | evidencia tecnica para avanzar a `qa` |
| `qa` | gates de calidad aplicables + escenarios criticos + evidencia de release candidate | decision explicita de promocion o rechazo |
| `prod` | aprobacion de promocion, runbooks vigentes, alertas activas y plan de rollback | estabilidad post-deploy sin alertas criticas en ventana inicial |

## Relacion con promocion entre ambientes
- Ningun cambio se promueve de `dev` a `qa` sin evidencia de compatibilidad de
  contratos y smoke de integracion.
- Ningun cambio se promueve de `qa` a `prod` sin cumplimiento de
  [Criterios de Promocion](/mvp/operacion/criterios-promocion-ambientes/).
- Cambios en secretos, eventos o configuracion regional requieren checklist
  adicional y verificacion post-cambio reforzada.
- Promocion entre ambientes mantiene mismo artefacto versionado (tag/digest),
  variando solo configuracion por entorno.

## Checklist rapido por ambiente
| Ambiente | Checklist minimo antes de cerrar actividad |
|---|---|
| `local` | smoke local ejecutado, sin secretos reales, trazabilidad de cambio registrada |
| `dev` | salud de servicio, pruebas del cambio, sin alertas tecnicas abiertas por el cambio |
| `qa` | evidencia de escenarios criticos, contratos compatibles, decision de promover o rechazar |
| `prod` | estabilidad en ventana inicial, alertas criticas en verde, evidencia de despliegue archivada |

## Flujo alerta -> incidente -> evidencia por ambiente
| Ambiente | Disparador de incidente | Evidencia minima esperada |
|---|---|---|
| `dev` | fallo de smoke repetitivo o ruptura de contrato | log tecnico + resultado de prueba + decision de correccion |
| `qa` | fallo en escenario critico o incompatibilidad de integracion | traza del caso, resultado de gate y accion aplicada |
| `prod` | alerta alta/critica de SLI/SLO | registro de incidente, metricas, runbook aplicado y cierre |
