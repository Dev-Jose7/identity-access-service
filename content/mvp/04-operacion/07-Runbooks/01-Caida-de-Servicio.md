---
title: "Caida de Servicio"
linkTitle: "1. Caida de Servicio"
weight: 1
url: "/mvp/operacion/runbooks/caida-servicio/"
---

## Proposito
Restaurar disponibilidad cuando un servicio deja de responder por completo.

## Senal de entrada
- Healthcheck fallando en dos ventanas consecutivas.
- Error rate cercano al 100% para endpoints criticos.

## Senales iniciales y prioridad
| Senal | Prioridad sugerida |
|---|---|
| servicio core sin respuesta | Sev 1 |
| servicio no core caido con workaround | Sev 2 |
| health inestable sin impacto funcional | Sev 3 |

## Impacto esperado
Interrupcion de flujo critico segun el servicio afectado.

## Diagnostico inicial
- Confirmar alcance: servicio aislado o degradacion de dependencia.
- Revisar ultimo deployment, cambios de configuracion y secretos.
- Verificar estado de base de datos, broker y red.
- Confirmar version/tag de imagen desplegada y estado de arranque del contenedor.
- En local/dev/qa, validar si el fallo se reproduce en el stack
  multi-contenedor de integracion.

## Decision rapida (si X entonces Y)
| Si | Entonces |
|---|---|
| hubo despliegue o cambio reciente | evaluar rollback inmediato |
| dependencia critica degradada | contener trafico y priorizar restauracion de dependencia |
| impacto en checkout/pedido | escalar a Sev 1 y activar coordinacion cross-servicio |
| version/tag de imagen no coincide con release esperado | detener promocion y revertir a imagen aprobada |
| contenedor no inicia por configuracion/secreto | corregir inyeccion por entorno y reiniciar controladamente |
| en local/dev/qa falta dependencia del stack compose | levantar dependencia y repetir smoke de flujo |

## Contencion
- Redirigir trafico a instancia sana si existe.
- Activar degradacion controlada cuando aplique.
- Bloquear promociones mientras dure incidente.

## Criterio de contencion cumplida
- error-rate deja de escalar;
- alcance del impacto esta delimitado;
- no hay nuevas promociones sobre servicios afectados.

## Recuperacion
- Reiniciar componente afectado.
- Revertir release o configuracion reciente si aplica.
- Restaurar conectividad a dependencia critica.
- Si aplica, reversion por imagen estable previa.

## Verificacion posterior
- Healthchecks estables por dos ventanas.
- Error rate y latencia dentro de SLO base.
- Flujo critico confirmado en smoke operativo.
- Version/tag/digest de imagen coincide con release estabilizado.

## Criterio de cierre
- servicio estable en ventana operativa;
- runbook ejecutado y incidente actualizado;
- evidencia minima registrada y comunicacion de cierre emitida.

## Escalamiento
- Sev 1 si afecta checkout/pedido en prod.
- Escalar a owner de servicio + plataforma en menos de 10 min.

## Evidencia a registrar
- timeline del incidente
- metricas antes/despues
- causa raiz preliminar
- acciones ejecutadas

## Artefactos relacionados
- Incidentes/01-Gestion-de-Incidentes
- Incidentes/02-Severidades-y-Escalamiento
