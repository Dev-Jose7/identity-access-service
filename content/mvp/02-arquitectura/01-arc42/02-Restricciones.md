---
title: "Restricciones"
linkTitle: "2. Restricciones"
weight: 2
url: "/mvp/arquitectura/arc42/restricciones/"
---

## Proposito
Registrar las restricciones tecnicas, de negocio, seguridad y operacion que condicionan el diseno de ArkaB2B en `MVP`.

## Restricciones de plataforma y stack base
| Categoria | Restriccion | Caracter | Impacto arquitectonico | Base |
|---|---|---|---|---|
| Lenguaje y runtime | La implementacion del backend se fija en `Java 21`. | MUST | unifica runtime, modelo de tipos, librerias y compatibilidad entre servicios | [Estrategia de Solucion](/mvp/arquitectura/arc42/estrategia-solucion/), `services/*/architecture/02-Vista-de-Codigo.md` |
| Framework de aplicacion | El stack de servicios se implementa sobre `Spring` con `WebFlux` para APIs y flujos reactivos. | MUST | orienta el modelo de I/O no bloqueante, configuracion y exposicion de endpoints/eventos | `04-Estrategia-de-Solucion.md`, `services/*/architecture/01-Vista-C4-Nivel-3.md` |
| Seguridad de aplicacion | La capa de seguridad reactiva debe resolverse con `Spring Security WebFlux` o una alternativa totalmente compatible con el baseline actual. | MUST | condiciona filtros, autenticacion, autorizacion y enforcement no bloqueante por tenant/rol | `services/identity-access-service/architecture/02-Vista-de-Codigo.md`, `services/*/security/01-Arquitectura-de-Seguridad.md` |
| Persistencia transaccional | Cada servicio usa `PostgreSQL 15+` como store transaccional propio. | MUST | refuerza `database-per-service`, integridad local y separacion fuerte de ownership | `05-Vista-de-Bloques.md`, `services/*/data/02-Modelo-Fisico.md` |
| Acceso a datos | La persistencia reactiva se implementa via `R2DBC`. | MUST | evita capas bloqueantes en servicios reactivos y condiciona adaptadores/repositorios | `services/*/architecture/02-Vista-de-Codigo.md` |
| Broker de eventos | Las integraciones async cross-service se fijan sobre `Kafka`. | MUST | define topicos, particionamiento, dedupe, DLQ y publicacion por outbox | `06-Vista-de-Ejecucion.md`, `07-Contratos-de-Integracion.md`, `services/*/contracts/02-Eventos.md` |
| Cache y coordinacion | `Redis` se usa para cache selectiva; adicionalmente soporta rate limiting, sesion tecnica y locking distribuido donde aplique. | SHOULD | condiciona latencia de lectura, proteccion de picos y control de contencion en inventario | `07-Vista-de-Despliegue.md`, `services/*/performance/01-Presupuestos-de-Rendimiento.md`, `services/inventory-service/architecture/02-Vista-de-Codigo.md` |
| Edge y plataforma | La plataforma base incluye `API Gateway`, `Config Server` y `Eureka Server`. | MUST | fija entrada unica, configuracion centralizada y discovery interno del sistema | `05-Vista-de-Bloques.md`, `07-Vista-de-Despliegue.md` |
| Plataforma de despliegue | La realizacion productiva prioriza servicios administrados sobre `AWS`; si `AWS` no puede materializarse en `MVP`, `Railway` queda aprobado como PaaS alterno preservando topologia logica, seguridad y contratos. | MUST | condiciona secretos, storage, red privada, operacion de runtimes y equivalencias administradas por entorno | `07-Vista-de-Despliegue.md`, `09-Decisiones-Arquitectonicas.md` |
| Seguridad criptografica | La identidad de acceso usa `JWT` firmado asimetricamente (`RS256`) con soporte de `JWKS`; credenciales con `BCrypt`. | MUST | condiciona login, refresh, introspeccion, revocacion y rotacion de llaves | `services/identity-access-service/security/01-Arquitectura-de-Seguridad.md`, `services/identity-access-service/architecture/02-Vista-de-Codigo.md` |
| Secretos | Los secretos operativos viven fuera del codigo; el baseline prioriza `AWS Secrets Manager / Parameter Store` y acepta secret store gestionado equivalente cuando el despliegue use `Railway`. | SHOULD | condiciona despliegue, bootstrap seguro y rotacion controlada | `services/*/security/01-Arquitectura-de-Seguridad.md`, `07-Vista-de-Despliegue.md`, `08-Conceptos-Transversales.md` |
| Contratos HTTP | En `MVP` los contratos HTTP se documentan semanticamente en markdown; la serializacion `OpenAPI` formal (`yaml/json`) queda fuera del baseline actual. | MUST | evita falsa completitud documental y concentra el detalle en contratos consumibles del ciclo | `services/*/contracts/01-APIs.md` |

## Restricciones arquitectonicas estructurales
| Restriccion | Caracter | Impacto arquitectonico | Base en producto/dominio |
|---|---|---|---|
| Arquitectura por bounded context con ownership de datos por servicio. | MUST | impide mezclar responsabilidades y fija el corte de servicios por verdad semantica | `01-Mapa-de-Contexto.md`, `05-Vista-de-Bloques.md` |
| Sin joins ni foreign keys cross-service; la integracion entre servicios ocurre por API o eventos. | MUST | fuerza contratos explicitos, ACLs y aislamiento de persistencia | `07-Contratos-de-Integracion.md`, `05-Vista-de-Bloques.md` |
| Validaciones criticas de negocio van sync; propagacion y side effects van async. | MUST | condiciona runtime, acoplamiento temporal y estrategia de resiliencia | `05-Comportamiento-Global.md`, `07-Contratos-de-Integracion.md`, `06-Vista-de-Ejecucion.md` |
| Todo comando mutante core debe ser idempotente mediante `Idempotency-Key` o clave natural equivalente. | MUST | condiciona APIs, dedupe, retries y manejo de replay | `04-Reglas-e-Invariantes.md`, `05-Comportamiento-Global.md`, `06-Vista-de-Ejecucion.md` |
| La publicacion de eventos de dominio se hace mediante outbox transaccional. | MUST | evita perdida de hechos y elimina necesidad de XA | `04-Estrategia-de-Solucion.md`, `06-Vista-de-Ejecucion.md`, `services/*/architecture/02-Vista-de-Codigo.md` |
| Todo consumidor cross-service debe aplicar dedupe por `eventId`; los mensajes irreparables terminan en DLQ. | MUST | condiciona consumidores, reproceso y operacion del broker | `06-Eventos-de-Dominio.md`, `06-Vista-de-Ejecucion.md`, `services/*/contracts/02-Eventos.md` |
| La consistencia fuerte solo se exige dentro de cada servicio; entre servicios se acepta consistencia eventual salvo validaciones criticas previas a mutacion. | MUST | fija limites transaccionales y estrategia CAP/PACELC del sistema | `08-Conceptos-Transversales.md`, `04-Reglas-e-Invariantes.md` |
| `reporting-service` es derivado y no puede gobernar decisiones transaccionales del core. | MUST | evita que analytics invada el flujo operativo | `01-Mapa-de-Contexto.md`, `04-Reglas-e-Invariantes.md`, `05-Vista-de-Bloques.md` |

## Restricciones de dominio y negocio
| Restriccion | Caracter | Impacto arquitectonico | Base en producto/dominio |
|---|---|---|---|
| La creacion de pedido exige reservas vigentes y direccion valida. | MUST | fuerza coordinacion estricta `order -> directory -> inventory` en checkout | `FR-004`, `FR-005`, `04-Reglas-e-Invariantes.md`, `05-Comportamiento-Global.md` |
| El pedido nace en `PENDING_APPROVAL`; la confirmacion comercial es explicita y posterior (`CONFIRMED`). | MUST | separa checkout de aprobacion comercial y evita colapsar dos estados de negocio en uno | `05-Comportamiento-Global.md`, `06-Eventos-de-Dominio.md`, `04-Reglas-e-Invariantes.md` |
| La sobreventa semanal aceptable es `<= 1.0%`. | MUST | obliga a priorizar consistencia de reservas, locking y presupuestos de checkout | `NFR-004`, `04-Reglas-e-Invariantes.md`, `10-Escenarios-de-Calidad.md` |
| El pago manual debe coexistir con el flujo MVP. | MUST | obliga a soportar evidencia manual, referencia unica y trazabilidad comercial | `FR-010`, `05-Comportamiento-Global.md` |
| Las operaciones criticas deben resolver politica operativa por `countryCode` vigente antes de mutar. | MUST | condiciona dependencia sync con `directory` y elimina fallbacks silenciosos en regionalizacion | `FR-011`, `NFR-011`, `04-Reglas-e-Invariantes.md`, `07-Contratos-de-Integracion.md` |
| Los fallos de notificacion no bloquean ni revierten estados core. | SHOULD | desacopla side effects y evita rollback de pedido/pago por canales externos | `FR-006`, `NFR-007`, `05-Comportamiento-Global.md` |
| `reporting-service` solo consume hechos y genera proyecciones/reportes; no valida verdad operativa. | MUST | refuerza pipelines async y modelo read-side derivado | `FR-003`, `FR-007`, `01-Mapa-de-Contexto.md` |

## Restricciones de seguridad y compliance
| Restriccion | Caracter | Impacto arquitectonico | Base en producto/dominio |
|---|---|---|---|
| Toda mutacion exige enforcement de `tenantId` y `rol`. | MUST | obliga a authz en borde y servicio, no solo en gateway | `FR-009`, `NFR-005`, `04-Reglas-e-Invariantes.md` |
| Eventos, logs y auditoria deben incluir al menos `traceId`, `correlationId`, `actorId` y `tenantId`. | MUST | condiciona envelope, logging estructurado y trazabilidad transversal | `NFR-006`, `NFR-007`, `04-Reglas-e-Invariantes.md`, `06-Eventos-de-Dominio.md` |
| El trafico cliente, m2m y hacia proveedores debe usar `TLS 1.2+` o superior. | MUST | condiciona gateway, certificados y configuracion de integraciones externas | `services/*/security/01-Arquitectura-de-Seguridad.md` |
| Los datos sensibles financieros o de identidad no se exponen completos en logs ni respuestas operativas. | MUST | obliga a masking, control de acceso y politicas de auditoria | `FR-010`, `NFR-006`, `services/order-service/security/01-Arquitectura-de-Seguridad.md` |
| Debe existir preparacion para retencion, borrado y anonimizacion por tipo de dato. | MUST | condiciona storage, eventos de borrado y estrategia de cumplimiento | `NFR-010`, `04-Reglas-e-Invariantes.md` |
| Las llaves, credenciales y secretos operativos no viven en repositorio ni configuracion embebida. | MUST | condiciona bootstrap, despliegue y runbooks de rotacion | `07-Vista-de-Despliegue.md`, `services/*/security/01-Arquitectura-de-Seguridad.md` |

## Restricciones operativas y de despliegue
| Restriccion | Caracter | Impacto arquitectonico | Base |
|---|---|---|---|
| La promocion de releases sigue `local -> dev -> qa -> staging -> prod` con gates y aprobacion humana para cambios estructurales. | MUST | condiciona versionado, rollout y evidencia previa a produccion | `07-Vista-de-Despliegue.md` |
| Debe existir observabilidad minima con logs, metricas, trazas y alertas de latencia/error. | MUST | obliga a exponer telemetria desde cada servicio y desde la plataforma | `NFR-007`, `08-Conceptos-Transversales.md`, `07-Vista-de-Despliegue.md` |
| Los componentes stateful (`DB`, `Kafka`, `Redis`) viven en plano privado de datos; el borde publico se limita al gateway. | MUST | condiciona topologia, segmentacion de red y superficie de ataque | `07-Vista-de-Despliegue.md` |
| Cada entorno mantiene configuracion aislada y sin credenciales compartidas. | SHOULD | evita contaminacion entre ambientes y endurece promotion segura | `07-Vista-de-Despliegue.md` |
| Deben existir backups por base de datos, pruebas periodicas de restore y runbooks para degradacion de broker/cache/proveedor. | SHOULD | condiciona continuidad operativa y recuperacion ante fallos | `07-Vista-de-Despliegue.md` |
| Si `Kafka` falla, la transaccion core persiste y el outbox acumula para reproceso; si `Redis` falla, el sistema degrada a DB con limites de throughput. | MUST | fija el comportamiento de degradacion aceptado del MVP | `services/*/performance/01-Presupuestos-de-Rendimiento.md`, `06-Vista-de-Ejecucion.md` |

## Matriz de impacto
| Restriccion dominante | Tecnologia/decisiones afectadas | Impacto principal | FR/NFR |
|---|---|---|---|
| Reserva valida previa a confirmacion | `Java 21`, `Spring WebFlux`, `Kafka`, `Redis`, `PostgreSQL`, outbox | orquestacion estricta checkout-order-inventory y proteccion de consistencia comercial | `FR-004`, `FR-005`, `NFR-004` |
| Aislamiento multi-tenant | `JWT RS256`, `JWKS`, `BCrypt`, `Spring Security WebFlux` | autorizacion por tenant/rol en todos los servicios y consultas | `FR-009`, `NFR-005` |
| Auditoria obligatoria | envelope con metadata, logs estructurados, broker, storage por servicio | trazabilidad completa de mutaciones y side effects | `FR-010`, `NFR-006`, `NFR-007` |
| Versionado y estabilidad de contratos | markdown contractual actual + evolucion futura `OpenAPI`, versionado de eventos | compatibilidad evolutiva y control de breaking changes | `FR-006`, `NFR-009` |
| Regionalizacion operativa | sync API con `directory`, politicas por pais, runtime de `order` y `reporting` | bloqueo de operaciones criticas sin configuracion vigente por pais | `FR-011`, `NFR-011` |
| Budgets de rendimiento y degradacion controlada | `R2DBC`, `Redis`, `Kafka`, particionamiento y telemetria | capacidad, latencia y continuidad operacional sin perder verdad de negocio | `NFR-001`, `NFR-002`, `NFR-008` |
