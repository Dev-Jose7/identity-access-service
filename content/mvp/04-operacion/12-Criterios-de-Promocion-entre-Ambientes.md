---
title: "Criterios de Promocion entre Ambientes"
linkTitle: "12. Promocion entre Ambientes"
weight: 12
url: "/mvp/operacion/criterios-promocion-ambientes/"
---

## Objetivo
Definir condiciones minimas para promover cambios entre ambientes.

## Gates de promocion por tramo
| Tramo | Evidencia obligatoria | Gate minimo |
|---|---|---|
| `local -> dev` | smoke local + trazabilidad de cambio | Gate 0/1 segun alcance |
| `dev -> qa` | pruebas unitarias/integracion, compatibilidad de contrato | Gate 1/2 |
| `qa -> prod` | certificacion minima aplicable, smoke `qa`, plan de rollback, aprobacion operativa | Gate 3/4 |

## Requisitos de promocion
- Gates de calidad del cambio en estado conforme.
- Evidencia minima de certificacion del baseline segun `03-calidad`.
- Contratos API/eventos compatibles y versionados cuando aplique.
- Migraciones y configuraciones criticas aprobadas.
- Plan de rollback validado (codigo + configuracion + datos si aplica).
- Runbook de riesgo principal identificado para el cambio.
- Artefacto versionado identificado por version/tag/digest para el alcance.

## No promover cuando
- hay incidente Sev 1 o Sev 2 abierto.
- no hay evidencia de smoke post-deploy en ambiente origen.
- la configuracion regional vigente no esta asegurada.
- existe crecimiento anomalo de DLQ o lag no explicado.
- el cambio introduce evento/API breaking sin convivencia versionada.
- no hay evidencia de validacion de secretos/configuracion sensible.
- no existe trazabilidad de version de artefacto o rollback.

## Restricciones para cambios de eventos, secretos y regionalizacion
- Eventos: cualquier cambio semantico requiere version explicita y evidencia de
  consumidores compatibles.
- Secretos: la promocion solo avanza con verificacion post-rotacion en el
  ambiente origen.
- Configuracion regional: no se promueve a `prod` si falta politica vigente por
  `countryCode` para la operacion critica del cambio.

## Restriccion para stack multi-contenedor de integracion
- `docker compose` es requisito para reproducibilidad de local/dev/qa de
  integracion y smoke.
- La promocion a `prod` no se fundamenta solo en levantar compose; requiere
  evidencia de release sobre plataforma objetivo y controles de gate.

## Criterio de rollback
Rollback obligatorio ante degradacion sostenida fuera de umbral SLO.

## Reversion de promocion
- Si falla tramo `dev -> qa`, el cambio regresa a backlog tecnico.
- Si falla tramo `qa -> prod`, se ejecuta rollback y se abre incidente.
- Ninguna re-promocion se autoriza sin evidencia de correccion del fallo
  original y nueva validacion completa del tramo.

## Dependencia con certificacion minima de calidad
- La promocion a `prod` consume el paquete de certificacion minima definido en
  `03-calidad/06-Evidencias`.
- El estado documental permitido para promover es `Ejecutado` o superior en
  casos obligatorios del alcance del release.
