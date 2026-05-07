---
title: "SLI, SLO y Alertas"
linkTitle: "6. SLI/SLO y Alertas"
weight: 6
url: "/mvp/operacion/sli-slo-alertas/"
---

## Proposito
Traducir RNF del baseline a indicadores operativos medibles,
objetivos de servicio y alertas accionables.

## Modelo operativo de medicion
- Fuente primaria: metricas de servicio, logs estructurados y trazas.
- Fuente de respaldo: auditoria de operaciones criticas y eventos del broker.
- Regla de calculo: usar ventanas completas; incidentes en curso no se excluyen.
- Exclusiones permitidas: mantenimientos aprobados y trazados en ventana
  comunicada.

## SLI/SLO iniciales del MVP (operativos)
| Capacidad | Servicios fuente | Senal / calculo | SLO inicial | Ventana | Alerta | Runbook |
|---|---|---|---|---|---|---|
| Disponibilidad backend core (NFR-003) | gateway + IAM + directory + catalog + inventory + order | `1 - (errores no controlados + caidas) / total requests core` | >= 99.5% | mensual (06:00-22:00) | critica | [Caida de Servicio](/mvp/operacion/runbooks/caida-servicio/) |
| Latencia flujo core (NFR-001) | order/inventory/catalog/directory | p95 por endpoints criticos de login, checkout, reserva, creacion pedido | lectura <= 800 ms, mutacion <= 1500 ms | diario + por release | media/alta | [Checkout Degradado](/mvp/operacion/runbooks/checkout-degradado/) |
| Autenticacion estable | IAM + gateway | tasa 401/403 anomala sobre baseline por ruta protegida | sin crecimiento sostenido fuera de umbral operacional | 15m/1h | alta/critica | [Fallos de Autenticacion](/mvp/operacion/runbooks/fallos-autenticacion/) |
| Consistencia de reservas (NFR-004) | inventory + order | casos de reserva inconsistente / total reservas de ventana | <= 1.0% semanal | semanal | alta | [Reserva Inconsistente](/mvp/operacion/runbooks/reserva-inconsistente/) |
| Salud de procesamiento de eventos | broker + consumidores core | lag por consumer group + ratio de reintentos | lag y retry dentro de baseline de servicio | 5m/30m | alta | [Consumidor Atascado](/mvp/operacion/runbooks/consumidor-atascado/) |
| Control de DLQ | broker + servicios consumidores | crecimiento neto de DLQ por topico en ventana | sin crecimiento sostenido no explicado | 15m/1h | alta | [Crecimiento de DLQ](/mvp/operacion/runbooks/crecimiento-dlq/) |
| Regionalizacion sin fallback (NFR-011) | directory + order + reporting | conteo de `configuracion_pais_no_disponible` por countryCode | 100% operaciones criticas con politica vigente | por release + diario | alta | [Configuracion Pais No Disponible](/mvp/operacion/runbooks/configuracion-pais-no-disponible/) |
| Aislamiento tenant (NFR-005) | todos los servicios mutantes | incidentes cross-tenant confirmados | 0 criticos/mes | mensual | critica | [Fallos de Autenticacion](/mvp/operacion/runbooks/fallos-autenticacion/) |
| Entrega de notificaciones no bloqueantes | notification-service | `failed`/`discarded` sobre `requested` por canal | dentro de umbral operacional del canal | diario | media | [Reporting Atrasado](/mvp/operacion/runbooks/reporting-atrasado/) |
| Trazabilidad de mutaciones (NFR-006/NFR-007) | todos los servicios mutantes | porcentaje de respuestas/eventos con `traceId` y metadata obligatoria | >= 99% metadata completa; `traceId` en 100% mutaciones | por release | media | [Aumento de Errores 5xx](/mvp/operacion/runbooks/aumento-errores-5xx/) |

## Politica de alertas
| Severidad | Disparador tipico | Tiempo de reaccion objetivo | Canal |
|---|---|---|---|
| Critica | caida de servicio core, ruptura checkout, fallo authn generalizado | <= 10 min | on-call + escalamiento inmediato |
| Alta | error-rate elevado sostenido, crecimiento rapido de DLQ, lag alto de consumidor core | <= 20 min | on-call + equipo servicio |
| Media | degradacion de latencia, atraso de reporting, retry anomalo | <= 60 min | equipo servicio |
| Baja | ruido no critico, warnings de capacidad | <= 1 dia habil | backlog operacional |

## Mapa alerta -> runbook
| Alerta | Runbook |
|---|---|
| Servicio sin respuesta | [Caida de Servicio](/mvp/operacion/runbooks/caida-servicio/) |
| `5xx` sobre umbral | [Aumento de Errores 5xx](/mvp/operacion/runbooks/aumento-errores-5xx/) |
| fallos authn/authz | [Fallos de Autenticacion](/mvp/operacion/runbooks/fallos-autenticacion/) |
| checkout degradado | [Checkout Degradado](/mvp/operacion/runbooks/checkout-degradado/) |
| inconsistencia de reservas | [Reserva Inconsistente](/mvp/operacion/runbooks/reserva-inconsistente/) |
| DLQ en crecimiento | [Crecimiento de DLQ](/mvp/operacion/runbooks/crecimiento-dlq/) |
| lag de consumidor | [Consumidor Atascado](/mvp/operacion/runbooks/consumidor-atascado/) |
| reporting atrasado | [Reporting Atrasado](/mvp/operacion/runbooks/reporting-atrasado/) |
| `configuracion_pais_no_disponible` alta | [Configuracion Pais No Disponible](/mvp/operacion/runbooks/configuracion-pais-no-disponible/) |
| compromiso de secreto | [Rotacion Urgente de Secretos](/mvp/operacion/runbooks/rotacion-urgente-secretos/) |

## Mapa operativo alerta -> riesgo -> evidencia
| Alerta | Riesgo asociado | Evidencia operativa requerida |
|---|---|---|
| servicio sin respuesta | indisponibilidad de flujo core | incidente + capturas de salud + trazas de error |
| checkout degradado | caida de conversion y pedidos incompletos | trazas e2e + metricas de exito/error de checkout |
| DLQ creciente | atraso de procesamiento y deriva de consistencia | conteo de DLQ por topico + lote reprocesado |
| error regionalizacion | bloqueo de operacion por pais | log canonico + version de politica aplicada |
| errores authn/authz | bloqueo de acceso o riesgo de aislamiento | logs seguridad + estado JWKS + trazas afectadas |

## Consideraciones de calculo y exclusiones
- Incidentes de `prod` siempre cuentan para disponibilidad y error-rate.
- Ambientes `dev/qa` se usan para tendencia y pre-alerta, no para SLO final.
- Eventos descartados por politica valida se separan de fallas tecnicas.
- Ajustes de umbral no modifican la semantica del SLO comprometido.

## Reglas de calibracion
- Umbrales se ajustan por telemetria real sin romper SLO baseline.
- Cambios de umbral requieren registro de cambio operacional.
- Ninguna calibracion puede ocultar incidentes `P0/P1`.

## Checklist rapido de respuesta a alertas
| Paso | Objetivo |
|---|---|
| 1. confirmar alerta | descartar falso positivo y delimitar alcance |
| 2. clasificar severidad | activar canal y tiempos de respuesta correctos |
| 3. ejecutar runbook | contener y recuperar con procedimiento estandar |
| 4. validar recuperacion | confirmar retorno a umbral en ventana definida |
| 5. registrar evidencia | asegurar trazabilidad para auditoria y aprendizaje |
