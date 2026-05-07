---
title: "Evolucion de Arquitectura"
linkTitle: "4. Evolucion"
weight: 4
url: "/mvp/arquitectura/evolucion/"
---

## Marco de evolucion
_Responde: como cambia esta arquitectura sin romper contratos, fronteras ni verdades del dominio._

La evolucion de arquitectura se gobierna por compatibilidad de
contratos, respeto al ownership de servicio, ADR y control explicito
de deuda tecnica.

## Cambios no breaking
_Responde: que cambios pueden realizarse sin alterar el contrato semantico aprobado._
- agregar campos opcionales a APIs/eventos;
- introducir nuevos consumers idempotentes;
- optimizar persistencia, indices o cache sin cambiar semantica;
- agregar observabilidad, hardening o budgets mas estrictos.

## Cambios breaking
_Responde: que cambios obligan version nueva, ADR o reevaluacion del baseline._
- fusionar o dividir servicios alterando ownership de bounded contexts;
- cambiar el significado de `OrderCreated`, `OrderConfirmed`, `vendible`, `disponible` o errores canonicos;
- mover un flujo critico de sync a async cuando rompe una invariante;
- exponer mutaciones core desde `reporting-service` o `notification-service`.

## Gobierno de decision
_Responde: que cambios requieren ADR y cual es el criterio de registro._
- cambios de estilo arquitectonico;
- cambios de versionado/compatibilidad de contratos externos;
- cambios en estrategia de consistencia, outbox, seguridad transversal o despliegue;
- incorporacion de nueva plataforma obligatoria para todos los servicios.

## Deuda tecnica aceptada
_Responde: que simplificaciones del MVP quedan aceptadas y como deben controlarse._
| ID | Deuda | Motivo | Control minimo |
|---|---|---|---|
| A-TD-01 | pago online no materializado | alcance MVP | integrar en ciclo posterior con ADR propio |
| A-TD-02 | permisos finos por pais pendientes | falta cierre regulatorio/operativo | mantener baseline RBAC minimo (`tenant_user`, `tenant_admin`, `arka_operator`, perfil analista inventario de solo lectura, `arka_admin` y `service_scope:*`) y registrar decision futura |
| A-TD-03 | tuning real de carga por pais pendiente | falta trafico productivo | recalibrar budgets despues del piloto |
| A-TD-04 | estados de fulfillment reservados | foco en baseline comercial | no exponerlos como capacidad operativa cerrada |
| A-TD-05 | MFA para cuentas administrativas Arka diferido | hardening de seguridad posterior al freeze del baseline | planificar cierre en `04-operacion` sin bloquear `MVP` |

## Clasificacion de decisiones para evolucion
_Responde: que queda vigente, parametrizado o diferido sin bloquear el baseline del ciclo._
Fuente unica vigente: [Decisiones Arquitectonicas](/mvp/arquitectura/arc42/decisiones-arquitectonicas/).

Reglas de gobierno:
- este documento no replica matrices detalladas para evitar drift con `arc42/09`;
- toda decision que cambie de categoria (`vigente`, `parametrizada`, `evolucion posterior`, `no bloqueante`) se actualiza primero en `arc42/09` y luego se refleja en trazabilidad/servicios;
- las decisiones en evolucion posterior no bloquean el freeze arquitectonico de `MVP`.

## Historial de evolucion
_Responde: que hitos relevantes marcaron la evolucion del pilar durante el ciclo._
| Fecha | Hito | Efecto |
|---|---|---|
| 2026-03-13 | Materializacion inicial de arquitectura | se consolida baseline global, packs por servicio y salida para calidad/operacion |
| 2026-03-28 | Consolidacion de gobierno de abiertas | evolucion deja de duplicar listados y adopta `arc42/09` como fuente unica para seguimiento de decisiones abiertas |
| 2026-03-28 | Saneamiento de decisiones residuales | se consolida la clasificacion vigente/parametrizada/evolutiva; plataforma, baseline operativo, `JWKS`, regionalizacion y retencion minima quedan cerrados; MFA pasa a hardening posterior no bloqueante |
