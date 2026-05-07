---
title: "Gestion de Configuracion y Secretos"
linkTitle: "3. Configuracion y Secretos"
weight: 3
url: "/mvp/operacion/gestion-configuracion-secretos/"
---

## Proposito
Definir el manejo operativo de configuracion y secretos por ambiente,
asegurando trazabilidad, seguridad y control de cambios.

## Clasificacion de configuracion
| Clase | Ejemplos | Owner primario | Riesgo operativo | Regla de cambio |
|---|---|---|---|---|
| Funcional | ventanas de abandono, umbrales de stock, calendario semanal | producto + owner de servicio | medio | versionado y validado en `qa` |
| Operativa | timeouts, retries, concurrencia, tamano de lote | owner tecnico + operaciones | medio-alto | cambio con smoke y monitoreo reforzado |
| Regional critica | moneda, corte semanal, politicas por `countryCode` | `directory-service` | alto | cambio auditable, sin fallback global implicito |
| Seguridad sensible | allowlists, scopes base, politicas de acceso | seguridad + owner tecnico | alto | aprobacion dual y evidencia obligatoria |
| Integracion externa | endpoint broker, observabilidad, webhooks | plataforma + owner tecnico | alto | cambio coordinado con ventana y rollback |

## Secretos por entorno y origen
| Entorno | Origen permitido | Uso | Restricciones |
|---|---|---|---|
| `local` | secretos efimeros locales | desarrollo/pruebas tecnicas | nunca credenciales reales |
| `dev` | gestor de secretos del entorno | integracion temprana | acceso restringido a equipos tecnicos |
| `qa` | gestor de secretos del entorno | validacion release candidate | rotacion controlada por ciclo |
| `prod` | canal oficial gestionado en `AWS` | operacion real | acceso nominal, auditoria obligatoria |

## Secretos criticos a gobernar
- Secretos de base de datos por servicio.
- Credenciales del broker/event bus.
- Credenciales de observabilidad (escritura de metricas/trazas si aplica).
- Material de firma/validacion relacionado con JWT/JWKS en IAM.
- Tokens de integraciones externas de notificacion/reporteria.

## Tabla operativa de secretos/configuracion sensible
| Tipo | Origen oficial | Entornos | Rotacion minima | Auditoria minima |
|---|---|---|---|---|
| DB por servicio | gestor de secretos del entorno | dev/qa/prod | periodica + urgente por incidente | lectura/escritura y validacion post-rotacion |
| Broker/event bus | gestor de secretos del entorno | dev/qa/prod | periodica + ante fallo auth | cambio + impacto en productores/consumidores |
| Observabilidad | gestor de secretos del entorno | dev/qa/prod | periodica | uso de credencial + errores de ingesta |
| JWT/JWKS (IAM) | canal oficial IAM | qa/prod | segun politica de seguridad vigente | publicacion/rotacion de llave y consumidores afectados |
| Configuracion regional | `directory-service` versionado | dev/qa/prod | por cambio de politica | evidencia de vigencia por `countryCode` |

## Reglas obligatorias
- Prohibido hardcodear secretos, llaves privadas o tokens en codigo/docs.
- Rotacion planificada por tipo de secreto (aplicacion, DB, broker,
  proveedores externos).
- Acceso por minimo privilegio y trazabilidad de lectura/escritura de secreto.
- Todo cambio de configuracion critica requiere registro de motivo, responsable,
  fecha efectiva y estrategia de rollback.
- Toda mutacion de secreto/configuracion sensible requiere validacion
  post-cambio en el ambiente objetivo.
- El artefacto de servicio no debe contener secretos ni valores de entorno sensibles.
- La configuracion se inyecta por entorno en tiempo de despliegue/arranque.
- El proceso de aplicacion debe emitir logs por `stdout/stderr`.

## Acceso humano vs acceso maquina
- Acceso humano: solo cuentas nominales autorizadas por entorno.
- Acceso maquina: cuentas de servicio con permisos minimos por recurso.
- Ninguna cuenta de servicio debe reutilizar secretos entre `qa` y `prod`.
- Se audita quien/que realizo lectura, escritura o rotacion.

## Configuracion regional y error estable
- `directory-service` mantiene la configuracion regional versionada.
- `order-service` y `reporting-service` deben resolver politica vigente por
  `countryCode` antes de operar.
- Si no existe politica vigente: error `configuracion_pais_no_disponible`,
  bloqueo de operacion critica y auditoria.

## Flujo minimo de cambio (configuracion/secretos)
1. Solicitud con alcance, riesgo y ventana.
2. Aprobacion segun clase de configuracion/secreto.
3. Aplicacion por canal oficial del entorno.
4. Verificacion post-cambio (health, smoke, alertas, trazas).
5. Registro de evidencia y cierre.

## Configuracion de contenedores (baseline)
| Tema | Regla |
|---|---|
| imagen | inmutable por version; sin configuracion sensible embebida |
| variables de entorno | inyectadas por entorno (`dev/qa/prod`) |
| secretos | leidos desde gestor oficial del entorno |
| persistencia | fuera del contenedor (DB/broker/cache/storage) |
| logging | salida a `stdout/stderr` para observabilidad central |

## Artefactos esperados (configuracion/runtime)
| Artefacto | Proposito operativo |
|---|---|
| `Dockerfile` por servicio | definir build y runtime reproducible del servicio |
| imagen versionada del servicio | promover mismo artefacto entre ambientes |
| definicion de stack de integracion (`docker compose`) | levantar stack reproducible para local/dev/qa de integracion |
| archivo/plantilla de variables por entorno | inyectar configuracion sin embebido en imagen |

## Decision operativa (si X entonces Y)
| Si ocurre | Entonces |
|---|---|
| cambio en configuracion regional critica | validar `countryCode` afectados y ejecutar verificacion funcional dirigida |
| rotacion de secreto de broker | validar publish/consume y estado de DLQ/lag en ventana inmediata |
| cambio en secreto IAM/JWKS | verificar autenticacion en consumidores y errores JWT/JWKS |
| fallo post-cambio en `prod` | aplicar rollback/reversion y abrir incidente |

## Auditoria de cambios obligatoria
Cada cambio debe registrar:
- ambiente, servicio y parametro;
- valor anterior/nuevo (enmascarado si sensible);
- responsable y ticket;
- ventana de aplicacion;
- resultado de verificacion post-cambio;
- referencia a rollback si fue necesario.

## Validacion posterior minima
- `dev`: health + smoke tecnico del servicio.
- `qa`: smoke funcional del flujo impactado + verificacion de contratos.
- `prod`: smoke operativo + ventana de observabilidad reforzada.
- Para secretos criticos: prueba de conectividad/autenticacion al recurso
  dependiente despues de la rotacion.

## Checklist de cierre de cambio
| Item | Criterio |
|---|---|
| salud tecnica | probes y healthchecks en verde |
| flujo funcional | smoke del flujo impactado exitoso |
| observabilidad | sin alerta alta/critica nueva atribuible al cambio |
| auditoria | registro de cambio completo con responsable y evidencia |

## Referencias
- [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/)
- [Vista de Despliegue](/mvp/arquitectura/arc42/vista-despliegue/)
- [Runbook Rotacion Urgente de Secretos](/mvp/operacion/runbooks/rotacion-urgente-secretos/)
