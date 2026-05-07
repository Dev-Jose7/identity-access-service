---
title: "Estrategia de Releases y Deployments"
linkTitle: "4. Releases y Deployments"
weight: 4
url: "/mvp/operacion/estrategia-releases-deployments/"
---

## Proposito
Definir como liberar cambios en `MVP` con riesgo controlado,
compatibilidad contractual y capacidad de rollback.

## Estrategia de release
- Cadencia incremental por release candidate validado en `qa`.
- Promocion secuencial: `dev -> qa -> prod`.
- Despliegue por servicio con verificacion de dependencias criticas.
- Cambios breaking prohibidos sin versionado y ventana de convivencia.
- Preferencia por releases por servicio; release coordinado solo cuando existe
  dependencia contractual o de datos entre multiples servicios.
- Promocion de artefacto versionado (mismo artefacto entre ambientes; cambia
  solo configuracion por entorno).

## Tipos de release y prerequisitos
| Tipo | Alcance | Prerequisitos minimos |
|---|---|---|
| Servicio aislado | un servicio, sin cambio breaking | trazabilidad FR/NFR, Gate aplicable, smoke en `dev/qa`, rollback definido |
| Coordinado | varios servicios con dependencia de contrato/evento | matriz de compatibilidad, ventana de convivencia, orden de despliegue, rollback conjunto |
| Emergencia | remediacion de incidente activo | incidente abierto, autorizacion explicita, smoke minimo obligatorio, postmortem posterior |
| Configuracion critica | parametros/secrets/regional | evidencia de validacion en ambiente previo, ventana controlada, plan de reversion |

## Pre-requisitos de despliegue
- Cambio trazado a FR/NFR, riesgo o deuda aprobada.
- Calidad minima aplicable aprobada (Gate 0..4 segun alcance en `03-calidad`).
- Verificacion de contratos API/eventos impactados y estrategia de versionado.
- Plan de rollback documentado para codigo, configuracion y datos.
- Runbook relacionado identificado para el tipo de riesgo principal.
- Artefacto del alcance construido, identificado por version/digest y
  disponible para promocion.

## Matriz de verificacion por etapa de release
| Etapa | Verificacion minima | Si falla |
|---|---|---|
| Pre-deploy | gates de calidad, contratos/eventos, plan de rollback, imagen versionada | no desplegar y devolver a correccion |
| Deploy | despliegue completado desde imagen objetivo y health tecnico inicial | detener avance y evaluar rollback |
| Post-deploy temprano | smoke funcional + observabilidad + eventos | activar mitigacion/rollback segun severidad |
| Cierre de release | evidencia archivada + decision de estabilidad | abrir incidente o cerrar release |

## Compatibilidad de contratos y eventos
- API: cambios compatibles hacia atras en release regular.
- Eventos: cambios breaking solo con nueva version de evento y convivencia.
- Consumidores criticos deben validar deserializacion y semantica antes de `prod`.
- Ningun despliegue se aprueba si introduce ambiguedad semantica de evento.

## Manejo de cambios de esquema
- Migraciones forward-safe como estrategia por defecto.
- Migraciones destructivas solo fuera del baseline regular y con plan de
  restauracion validado.
- Separar despliegue de codigo y activacion de uso de nuevo esquema cuando el
  riesgo lo justifique.

## Post-deploy checks
| Tipo de check | Objetivo | Fuente de verificacion |
|---|---|---|
| Salud tecnica | confirmar disponibilidad (`/health`, readiness) | dashboard de salud + probes |
| Smoke funcional | validar flujo impactado | casos smoke por servicio |
| Observabilidad | validar logs/metricas/trazas | paneles operativos y trazas |
| Eventos | detectar lag/DLQ anomalo | panel broker + consumidores |
| Seguridad | validar errores authn/authz anormales | panel de seguridad operacional |

## Decision rapida post-deploy
| Senal | Decision recomendada |
|---|---|
| falla de healthcheck sostenida | rollback inmediato |
| smoke funcional falla en flujo core | rollback o fix forward en ventana corta controlada |
| DLQ/lag anomalo tras cambio de evento | pausar promocion y corregir compatibilidad |
| aumento authn/authz tras cambio de seguridad | activar runbook de autenticacion y evaluar rollback |

## Estrategia de imagenes y rollback
| Tema | Regla operacional |
|---|---|
| versionado | version/tag por release + digest inmutable |
| promocion | misma imagen entre `dev -> qa -> prod`, con configuracion por entorno |
| rollback | reversion a imagen estable anterior (o configuracion previa) |
| trazabilidad | registrar version/tag/digest en evidencia de release |

## Artefactos minimos por release
| Artefacto | Uso |
|---|---|
| imagen versionada del servicio (`tag`/`digest`) | promocion y rollback reproducible |
| `Dockerfile` vigente del servicio | trazabilidad de build/runtime |
| definicion de variables por entorno | inyeccion de config en `dev/qa/prod` |
| definicion de stack `docker compose` (cuando aplique) | smoke e integracion reproducible en local/dev/qa |

## Reglas de cambios sensibles
| Tipo de cambio | Regla operacional |
|---|---|
| Contratos API/Eventos | solo backward-compatible en release regular; breaking exige version mayor |
| Esquema DB | migraciones forward-safe y plan de rollback de datos |
| Configuracion critica | aplicar con ventana controlada y verificacion inmediata |
| Seguridad/secretos | rotacion sin downtime o con degradacion controlada y reversible |

## Releases por servicio vs coordinados
- `catalog`, `directory`, `notification`, `reporting`: preferencia por release
  aislado cuando no cambian contratos compartidos.
- `order` con `inventory` o `directory`: evaluar release coordinado si cambia
  checkout, reservas o validacion regional.
- `identity-access`: coordinar con consumidores cuando cambian politicas de
  token/JWKS o controles de autenticacion.

## Rollback
- Disparadores: aumento sostenido de `5xx`, degradacion de flujo critico,
  inconsistencia de eventos o riesgo de seguridad.
- Acciones: detener promocion, revertir version/configuracion,
  estabilizar estado y registrar incidente.
- Evidencia minima: timestamp, version revertida, causa, impacto y validacion
  posterior.
- Criterio de decision: si la mitigacion no estabiliza en la ventana objetivo,
  se ejecuta rollback obligatorio.
- Para releases con artefacto versionado, rollback preferente por version/tag
  de imagen.

## Compatibilidad con baseline congelado
- No se habilitan estados de fulfillment extendido en releases `MVP`.
- `MFA` administrativo no es precondicion de salida de release `MVP`.
- Toda operacion regional mantiene regla de bloqueo sin fallback global.

## Referencias
- [Criterios de Promocion](/mvp/operacion/criterios-promocion-ambientes/)
- [Puertas de Calidad](/mvp/calidad/puertas-calidad/)
- [Runbook Rollback de Release](/mvp/operacion/runbooks/rollback-release/)
- [Decisiones Arquitectonicas (canon de empaquetado y ejecucion reproducible)](/mvp/arquitectura/arc42/decisiones-arquitectonicas/)
