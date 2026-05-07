---
title: "Rollback de Release"
linkTitle: "11. Rollback de Release"
weight: 11
url: "/mvp/operacion/runbooks/rollback-release/"
---

## Proposito
Revertir un release cuando su impacto supera tolerancia operacional.

## Senal de entrada
- Degradacion tras despliegue confirmada por metricas.
- Fallas funcionales de flujo critico.

## Senales para activar rollback
| Senal | Decision |
|---|---|
| falla sostenida de healthcheck tras deploy | rollback inmediato |
| smoke funcional core fallido | rollback o fix forward en ventana corta |
| crecimiento anomalo de DLQ/lag tras cambio | pausar promocion y evaluar rollback |
| errores authn/authz anormales tras cambio de seguridad | rollback con incidente de seguridad |

## Impacto esperado
Riesgo de indisponibilidad o inconsistencia de negocio.

## Diagnostico inicial
- Comparar comportamiento pre/post deploy.
- Validar si causa esta en codigo, esquema o configuracion.

## Decision operativa (si X entonces Y)
| Si | Entonces |
|---|---|
| causa probable en codigo | revertir version de servicio |
| causa probable en configuracion | restaurar configuracion previa |
| causa probable en esquema | aplicar estrategia de reversion definida para DB |
| causa no concluyente en ventana critica | priorizar rollback para proteger disponibilidad |
| imagen desplegada no corresponde al release aprobado | rollback a tag/digest validado en ambiente estable |
| servicio no inicia por secreto/config inyectado | revertir configuracion y revalidar arranque del contenedor |

## Contencion
- Congelar nuevas promociones.
- Activar plan de rollback aprobado.

## Recuperacion
- Revertir version de servicio.
- Revertir migracion solo si estrategia lo permite.
- Restaurar configuracion previa estable.
- En despliegues con artefacto versionado, volver a version/tag/digest estable
  previo.

## Criterio de recuperacion cumplida
- servicios impactados con salud estable;
- flujo critico vuelve a tasa de exito esperada;
- sin alertas criticas nuevas atribuibles al release revertido.

## Verificacion posterior
- KPIs vuelven a baseline operativo.
- Flujos criticos superan smoke post-rollback.
- Artefacto final en runtime coincide con imagen/tag/digest esperado.

## Escalamiento
Sev 1 o Sev 2 segun impacto.

## Evidencia a registrar
- version rollback
- imagen/tag/digest rollback (cuando aplique)
- motivo y autorizacion
- resultado de verificacion
- timestamp de deteccion y de estabilizacion
- enlace a incidente/postmortem cuando aplique

## Artefactos relacionados
- 04-Estrategia-de-Releases-y-Deployments
- 12-Criterios-de-Promocion-entre-Ambientes
