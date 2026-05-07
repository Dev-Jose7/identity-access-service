---
title: "Riesgos Operacionales"
linkTitle: "13. Riesgos Operacionales"
weight: 13
url: "/mvp/operacion/riesgos-operacionales/"
---

## Matriz de riesgos
| Riesgo | Causa | Impacto | Senal temprana | Mitigacion | Runbook | Estado | Clasificacion |
|---|---|---|---|---|---|---|---|
| Caida de servicio core | release/configuracion defectuosa | indisponibilidad de flujo | healthchecks rojos | rollback + recuperacion guiada | 01-Caida-de-Servicio | activo | baseline |
| Error 5xx alto | dependencia degradada | fallas transaccionales | error-rate creciente | contencion + fix/rollback | 02-Aumento-de-Errores-5xx | activo | baseline |
| DLQ creciente | incompatibilidad de contrato | atraso y perdida de frescura | cola DLQ ascendente | reproceso controlado | 06-Crecimiento-de-DLQ | activo | baseline |
| Configuracion pais ausente | cambio incompleto de politica regional | bloqueo de operacion por pais | error configuracion_pais_no_disponible | publicar politica valida | 09-Configuracion-Pais-No-Disponible | activo | baseline |
| Exposicion de secreto | manejo inseguro | compromiso de seguridad | alerta de acceso anomalo | rotacion urgente | 10-Rotacion-Urgente-de-Secretos | activo | baseline |
| Demanda superior al baseline | crecimiento no planificado | degradacion progresiva | latencia y saturation elevadas | degradacion controlada + ajuste tactico | 03-Degradacion-Controlada | monitoreado | evolucion |

## Mapa de reaccion operacional por riesgo
| Riesgo | Alerta principal | Severidad inicial sugerida | Evidencia minima |
|---|---|---|---|
| Caida de servicio core | servicio sin respuesta | Sev 1 | timeline + metricas de salud + accion de recuperacion |
| Error 5xx alto | 5xx sobre umbral | Sev 2 (Sev 1 si impacta checkout) | panel error-rate + trazas + decision tecnica |
| DLQ creciente | crecimiento DLQ | Sev 2 | conteo por topico + causa clasificada + lote reprocesado |
| Configuracion pais ausente | error regionalizacion | Sev 2 | politica aplicada + log canonico + validacion funcional |
| Exposicion de secreto | compromiso de secreto | Sev 1 seguridad | secreto rotado + evidencia post-rotacion |

## Criterio de cierre de riesgo activo
- alerta principal controlada en ventana operativa acordada;
- runbook ejecutado y documentado;
- evidencia minima registrada en incidente o bitacora operativa.
