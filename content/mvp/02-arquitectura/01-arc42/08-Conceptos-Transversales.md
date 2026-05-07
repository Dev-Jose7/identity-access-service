---
title: "Conceptos Transversales"
linkTitle: "8. Conceptos Transversales"
weight: 8
url: "/mvp/arquitectura/arc42/conceptos-transversales/"
---

## Proposito
Consolidar conceptos transversales: consistencia, contratos, seguridad, observabilidad y gobernanza de cambios.

## Consistencia distribuida (CAP/PACELC)
| Escenario | Decision |
|---|---|
| Invariantes dentro de un servicio | consistencia fuerte local |
| Integraciones cross-service | consistencia eventual por eventos |
| Confirmacion de pedido | priorizar consistencia sobre disponibilidad |
| Consulta de reportes | priorizar disponibilidad sobre consistencia inmediata |

## Estrategia de integracion
- Sync para validaciones criticas entre servicios core (`order` con `directory`, `catalog`, `inventory`), incluyendo resolucion de parametros operativos por pais en `directory`; `identity-access-service` participa de forma sincrona solo en `login`, `refresh`, `logout`, administracion IAM e introspeccion de fallback cuando la confianza local del gateway no es suficiente.
- Async para propagacion de hechos:
  - `order -> notification`, `order -> reporting`
  - `inventory -> notification`, `inventory -> reporting`
  - `catalog -> reporting`
  - `directory -> reporting`
  - `notification -> reporting`
  - `reporting -> notification` (distribucion de reporte semanal)
- Contratos versionados con reglas de compatibilidad.

## Idempotencia y dedupe
| Flujo | Clave recomendada |
|---|---|
| checkout | `tenant:user:checkoutCorrelationId` |
| resolucion politica regional | `tenant:organization:countryCode` |
| reserva | `tenant:warehouse:cart:sku` |
| pago manual | `tenant:orderId:paymentReference` |
| consumo eventos | `eventId + consumerRef` |

## Seguridad transversal
- Validacion tenant y rol en todo comando mutante.
- Metadata de auditoria en eventos/logs de mutacion.
- Segregacion de datos por servicio.
- Secretos fuera de repositorio y rotacion controlada.

## Modelo transversal de autenticacion y autorizacion
| Capa | Responsabilidad |
|---|---|
| `identity-access-service` | autentica usuarios, emite JWT, mantiene sesion/rol y publica eventos de seguridad para propagacion de cambios |
| `api-gateway-service` | autentica requests HTTP validando JWT, `kid`, `iss`, `aud`, expiracion y cache de revocacion antes de enrutar |
| servicios HTTP | materializan un `PrincipalContext` canonico mediante `PrincipalContextPort` y `PrincipalContextAdapter`, aplican `PermissionEvaluatorPort`, `TenantIsolationPolicy` y politicas del dominio; no delegan la autorizacion contextual al gateway |
| listeners / schedulers / callbacks | materializan un `TriggerContext` canonico mediante `TriggerContextResolver`, validan legitimidad del trigger, `tenant`, caller tecnico, dedupe y politica de negocio antes de mutar estado |

Reglas de aplicacion:
- `Spring Security WebFlux` se usa en servicios HTTP para frontera, principal reactivo y guards gruesos.
- `PrincipalContext` es la abstraccion canonica para flujos HTTP; `TriggerContext` es la abstraccion canonica para eventos, schedulers y callbacks.
- `PermissionEvaluatorPort` es el guard funcional base cuando el servicio consume autorizacion RBAC; la autorizacion final se cierra en `use cases`, politicas y agregados del servicio destino.
- Salvo necesidad explicita de defensa en profundidad, los servicios no recalculan la firma JWT en cada request; su responsabilidad principal es la autorizacion contextual.

## Postura oficial de llaves publicas JWT (`JWKS`)
- `identity-access-service` expone `JWKS` como contrato estandar para verificadores autorizados.
- `api-gateway-service` y gateways internos consumen `JWKS` para validar JWT con `kid` vigente.
- El consumo de `JWKS` no sustituye la autorizacion contextual local de cada servicio.

## Baseline RBAC minimo (`MVP`)
| Perfil de negocio | Rol tecnico base | Permisos minimos esperados |
|---|---|---|
| comprador B2B | `tenant_user` | consultar catalogo, gestionar carrito propio, crear/editar pedido en ventana permitida y consultar su propio estado |
| administrador B2B | `tenant_admin` | capacidades de `tenant_user` + administrar usuarios/roles de su organizacion y datos operativos de su tenant |
| operador Arka | `arka_operator` | confirmar/cancelar pedido segun politica, registrar pago manual, ejecutar operaciones operativas autorizadas |
| analista de inventario | `arka_operator` (perfil solo lectura) | lectura de inventario/reportes y analitica operativa sin permisos mutantes de stock |
| administrador/soporte Arka | `arka_admin` | administracion transversal IAM/directorio/catalogo e intervenciones operativas de alto privilegio |
| actor tecnico interno | `service_scope:*` | acceso m2m acotado por scope/tenant y solo para contratos tecnicos explicitos |

Reglas de gobierno:
- la granularidad fina por pais/segmento evoluciona despues de `MVP`, sin romper este baseline minimo;
- cualquier ampliacion de permisos requiere trazabilidad en contratos API/eventos y pruebas de autorizacion.


## Modelo transversal de excepciones y errores
| Capa | Familia/decision canonica | Regla aplicada |
|---|---|---|
| `domain` | `DomainException`, `DomainRuleViolationException`, `ConflictException` | representan invariantes y conflictos semanticos del BC; nunca se degradan a errores tecnicos genericos |
| `application` | `ApplicationException`, `AuthorizationDeniedException`, `TenantIsolationException`, `ResourceNotFoundException`, `IdempotencyConflictException` | resuelven acceso, contexto, ausencia de recurso e idempotencia antes o alrededor de la decision del dominio sin filtrar detalles internos al borde |
| `infrastructure` | `InfrastructureException`, `ExternalDependencyUnavailableException`, `RetryableDependencyException`, `NonRetryableDependencyException` | encapsulan fallos de DB, broker, cache y clientes externos; los adapters no exponen errores crudos del proveedor |
| `adapter-in HTTP` | envelope canonico de error | el cierre web serializa `errorCode`, `category`, `traceId`, `correlationId`, `timestamp` y el `status` coherente con la familia semantica |
| `adapter-in async` | dedupe noop + clasificacion retryable/no-retryable | consumidores y schedulers tratan duplicados como `noop idempotente`; si el fallo es retryable se reintenta, si no lo es se enruta a DLQ y auditoria operativa |

Reglas de aplicacion:
- `Rechazo temprano`: normalmente nace en `PrincipalContext`, `TriggerContext`, `PermissionEvaluatorPort`, `TenantIsolationPolicy`, ausencia de recurso ya detectada o gates de idempotencia y se expresa como `AuthorizationDeniedException`, `TenantIsolationException`, `ResourceNotFoundException` o `IdempotencyConflictException`.
- `Rechazo de decision`: siempre nace en politicas o agregados del dominio y se expresa como `DomainRuleViolationException` o `ConflictException`, no como fallo tecnico.
- `Fallo de materializacion`: aparece despues de la decision al persistir, auditar, cachear o publicar; se traduce a `ExternalDependencyUnavailableException`, `RetryableDependencyException` o `NonRetryableDependencyException` segun la dependencia.
- `Evento duplicado`: cuando existe `ProcessedEvent*`, el resultado esperado es `noop idempotente`, no error funcional.

## Servicios gestionados y equivalencias de plataforma
| Necesidad transversal | Baseline preferente | Equivalencia admitida |
|---|---|---|
| secretos y bootstrap seguro | `AWS Secrets Manager` / `Parameter Store` | secret store gestionado del PaaS o vault externo compatible |
| storage de artifacts y backups | object storage tipo `S3` | object storage compatible desacoplado del runtime |
| persistencia relacional | PostgreSQL administrado | PostgreSQL administrado equivalente |
| cache distribuida | Redis administrado | Redis administrado equivalente |
| broker de eventos | plataforma gestionada compatible con `Kafka` | broker externo compatible con los contratos del sistema |
| observabilidad | stack gestionado o separado del plano app | proveedor/stack compatible con logs, metricas y trazas |

## Estandar transversal de contenedorizacion
Se adopta la decision canonica definida en
[Decisiones Arquitectonicas](/mvp/arquitectura/arc42/decisiones-arquitectonicas/).

## Artefactos esperados para empaquetado reproducible
| Artefacto | Proposito | Nota de uso |
|---|---|---|
| `Dockerfile` por servicio | estandarizar build y runtime de cada microservicio | uno por servicio, versionado con el codigo |
| imagen versionada del servicio | artefacto inmutable de promocion entre ambientes | promover por tag/digest |
| definicion de stack de integracion (`docker compose`) | levantar servicios+dependencias en local/dev/qa de integracion | enfocado a reproducibilidad tecnica y smoke |
| definicion de variables por entorno | separar config del artefacto | no embebida en imagen |

Regla transversal:
- la arquitectura define capacidades logicas, no dependencia fuerte a un vendor unico;
- `AWS` es la plataforma objetivo oficial de `MVP`;
- `Railway` solo aplica como fallback tactico/excepcional cuando `AWS` no puede materializarse dentro del ciclo, preservando seguridad, topologia logica, contratos e invariantes;
- `LocalStack` se usa exclusivamente para emulacion local/dev.

## Baseline operativo oficial (`MVP`)
| Capacidad | Baseline cerrado |
|---|---|
| broker / event bus | plataforma gestionada compatible con `Kafka`, con outbox obligatorio en productores y dedupe en consumidores (`processed_event` cuando aplique) |
| logs | logs estructurados con `traceId` y `correlationId`, sin exponer payload sensible completo |
| metricas | metricas por endpoint/consumer (`latency`, `error_rate`, `throughput`, `saturation`) con umbrales de alerta minimos por RNF |
| trazas | trazabilidad distribuida de flujos criticos con propagacion de contexto extremo a extremo |
| secretos / config | secretos fuera de repositorio en gestor administrado (`AWS Secrets Manager`/`Parameter Store` o equivalente en fallback), con rotacion y control de acceso |
| DLQ y reproceso | DLQ obligatoria en consumidores async; reproceso controlado, auditable y sin reescribir decisiones de dominio ya materializadas |
| empaquetado y ejecucion reproducible | artefacto versionado por microservicio + stack reproducible de integracion, sin asumir que el mecanismo local define produccion |

## Regionalizacion operativa obligatoria
- No existe fallback global implicito para operaciones mutantes ni computo semanal.
- Si no hay politica regional vigente por `countryCode`, la operacion critica se bloquea.
- El rechazo se expone con error semantico estable `configuracion_pais_no_disponible`.
- Todo bloqueo regional deja evidencia auditable (`traceId`, `correlationId`, actor y motivo).

Regla de mapeo HTTP (cerrada para `MVP`):
- `404` en consulta directa del recurso de configuracion regional (endpoint de
  resolucion en `directory`) cuando no existe version vigente.
- `409` en operacion de negocio (`order`/`reporting`) cuando la precondicion
  regional requerida no puede satisfacerse.

## Observabilidad transversal
- Logs estructurados con `traceId` y `correlationId`.
- Metricas de latencia, error rate y saturacion por endpoint/consumer.
- Trazas distribuidas para flows de checkout/pago/notificacion.
- Alertas minimas para NFR-001, NFR-003, NFR-004, NFR-007.
- Alertas minimas para NFR-011: `configuracion_pais_no_disponible` en checkout/reporting.

## Versionado de contratos
- Cambio no breaking: agregar campos opcionales/eventos nuevos.
- Cambio breaking: cambiar semantica o remover campos requeridos.
- Breaking change: requiere version mayor + ventana de convivencia + pruebas de contrato.

## Politica de datos y retencion
| Tipo de dato | Politica base `MVP` |
|---|---|
| transaccional core | se retiene para continuidad comercial y auditoria operativa; no se elimina fisicamente durante la ventana activa de retencion aplicable |
| auditoria de seguridad | se retiene como evidencia trazable e inmutable durante la ventana de cumplimiento definida por politica vigente |
| eventos operativos | se conserva envelope tecnico y metadata para dedupe/reproceso; payload sensible se minimiza cuando no sea indispensable |
| artefactos/reportes | se retienen con versionado y expiracion controlada; se purgan al vencer la politica vigente |
| PII de usuarios/contactos | minimizacion por defecto; anonimizar o borrar segun politica vigente cuando cese la necesidad operativa/comercial |
| payloads sensibles | no se persisten completos en logs/eventos cuando no sea estrictamente necesario; usar masking, huellas o resumen tecnico |

Regla de cierre del baseline:
- esta politica congela el baseline tecnico de `MVP` (retener, anonimizar, borrar o minimizar por clase de dato);
- la regulacion legal exhaustiva por pais queda en evolucion posterior y no bloquea el freeze arquitectonico actual.
