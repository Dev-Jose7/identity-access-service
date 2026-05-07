---
title: "Trazabilidad Operativa"
linkTitle: "14. Trazabilidad Operativa"
weight: 14
url: "/mvp/operacion/trazabilidad-operativa/"
---

## Objetivo
Conectar requisitos no funcionales y decisiones tecnicas con practica
operativa verificable.

## Mapa de trazabilidad
| Origen | Definicion | Operacion en 04 | Evidencia esperada |
|---|---|---|---|
| NFR disponibilidad | continuidad de flujo core | runbooks de caida/5xx/rollback | registro de incidente + metricas |
| NFR trazabilidad | correlacion extremo a extremo | logs/trazas con traceId+correlationId | dashboard + trazas por incidente |
| NFR seguridad | controles minimos operativos | accesos, secretos, eventos seguridad | auditoria de acceso/rotacion |
| Regionalizacion | sin fallback global implicito | bloqueo auditable por falta de politica | error canonico + log de bloqueo |
| Arquitectura eventos | broker+DLQ+idempotencia | operacion de lag/reintentos/reproceso | reporte DLQ y reproceso |
| Empaquetado reproducible | imagen versionada por servicio + stack de integracion reproducible | promocion por version de imagen + smoke de stack multi-contenedor en local/dev/qa | evidencia de build/arranque/promocion |
| Calidad minima | gates y certificacion minima | promocion condicionada y smoke post-deploy | evidencia de gate y acta |

## Cadena operativa NFR -> alerta -> riesgo -> runbook
| NFR | Alerta operativa | Riesgo operacional | Runbook principal | Evidencia de cierre |
|---|---|---|---|---|
| NFR-003 disponibilidad | servicio sin respuesta / 5xx alto | indisponibilidad core | 01-Caida-de-Servicio / 11-Rollback-de-Release | incidente + retorno de salud |
| NFR-001 latencia | checkout degradado | perdida de conversion | 04-Checkout-Degradado | metricas de latencia y exito |
| NFR-004 consistencia stock | reserva inconsistente | sobreventa o bloqueo indebido | 05-Reserva-Inconsistente | reconciliacion de reservas |
| NFR-005 aislamiento tenant | errores authz anormales | incidente cross-tenant | 03-Fallos-de-Autenticacion | auditoria + trazas de aislamiento |
| NFR-011 regionalizacion | `configuracion_pais_no_disponible` alta | bloqueo por pais | 09-Configuracion-Pais-No-Disponible | politica vigente aplicada |
| NFR-006/NFR-007 trazabilidad | metadata incompleta | baja auditabilidad operativa | 02-Aumento-de-Errores-5xx | cobertura de metadata en logs/eventos |

## Uso rapido para copiloto u operador
| Paso | Pregunta | Artefacto recomendado |
|---|---|---|
| 1 | que alerta se disparo | [06-SLI-SLO-y-Alertas](/mvp/operacion/sli-slo-alertas/) |
| 2 | que riesgo representa | [13-Riesgos-Operacionales](/mvp/operacion/riesgos-operacionales/) |
| 3 | que procedimiento ejecutar | [Runbooks](/mvp/operacion/runbooks/) |
| 4 | que evidencia guardar | [08-Incidentes/04-Registro-de-Incidentes](/mvp/operacion/incidentes/registro-incidentes/) |

## Relacion inter-pilar
- Producto aporta NFR y restricciones de alcance.
- Dominio aporta invariantes y errores canonicos.
- Arquitectura aporta decisiones y topologia operativa.
- Calidad aporta gates y paquete de certificacion minima.

Regla operativa de lectura del error regional:
- `404` aplica a ausencia del recurso tecnico de politica regional en
  resolucion directa (`directory`).
- `409` aplica al bloqueo de operacion de negocio (`order`/`reporting`) por
  falta de precondicion regional.

Referencia tecnica canonica:
- [Decisiones Arquitectonicas (canon de empaquetado y ejecucion reproducible)](/mvp/arquitectura/arc42/decisiones-arquitectonicas/)
