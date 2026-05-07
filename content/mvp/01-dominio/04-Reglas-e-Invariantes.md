---
title: "Reglas e Invariantes"
linkTitle: "4. Reglas e Invariantes"
weight: 4
url: "/mvp/dominio/reglas-invariantes/"
---

## Marco normativo
_Responde: para que existe este documento, que protege dentro del dominio y de donde obtiene su autoridad semantica._

Registrar reglas de negocio e invariantes de dominio que no pueden violarse en
ArkaB2B para preservar integridad comercial, seguridad de tenant y trazabilidad
en `MVP`.

- RF/RNF aprobados en `/mvp/producto/catalogo-rf/` y `/mvp/producto/catalogo-rnf/`.
- Lenguaje canonico del dominio en `/mvp/dominio/lenguaje-ubicuo/`.
- Fronteras y relaciones estrategicas en `/mvp/dominio/mapa-contexto/`.
- Cobertura consolidada en `/mvp/dominio/trazabilidad/`.

## Gobierno de consistencia
_Responde: que principios globales rigen la integridad del modelo y como se clasifica el universo de reglas que deben cumplirse._
- `MUST`: toda mutacion incluye `traceId`, `correlationId`, `actorId`, `tenantId`.
- `MUST`: mutaciones validan sesion, rol y pertenencia de tenant antes de ejecutar cambios.
- `MUST`: eventos de core (`order`, `inventory`, `identity-access`) se publican via outbox.
- `MUST`: consumidores aplican dedupe por `eventId`.
- `MUST`: errores de negocio devuelven accion correctiva sugerida.
- `MUST`: `directory` publica cambios de configuracion por pais antes de habilitar operacion regional.
- `SHOULD`: reportes y notificaciones toleran eventual consistency.

| Tipo | Prefijo | Enfoque |
|---|---|---|
| Reglas de acceso y seguridad | `RN-ACC`, `I-ACC`, `D-ACC` | sesion valida, rol y aislamiento por tenant |
| Reglas de catalogo/inventario | `RN-INV`, `RN-RES`, `I-CAT`, `I-INV`, `D-CAT`, `D-INV` | vendible, disponibilidad, reservas y anti-sobreventa |
| Reglas de pedido/pago | `RN-ORD`, `RN-PAY`, `I-ORD`, `I-PAY`, `D-ORD`, `D-PAY` | confirmacion valida, transiciones y pagos idempotentes |
| Reglas de notificacion/reporteria | `RN-NOTI`, `RN-REP`, `I-NOTI`, `I-REP` | asincronia no bloqueante y separacion transaccional |
| Reglas de regionalizacion | `RN-LOC`, `I-LOC`, `D-DIR` | parametros por pais vigentes por tenant |
| Regla transversal de datos | `D-CROSS` | todo registro mutable con `tenantId` |

## Catalogo de reglas de negocio
_Responde: que politicas del negocio deben cumplirse obligatoriamente y con que criterio verificable se validan._
| ID | Regla | Tipo | Criterio verificable | FR/NFR |
|---|---|---|---|---|
| RN-ACC-01 | Usuario bloqueado/deshabilitado no inicia sesion ni ejecuta mutaciones | MUST | 0 mutaciones aceptadas con usuario no habilitado | FR-009, NFR-005 |
| RN-ACC-02 | Toda mutacion valida tenant y rol permitido | MUST | 0 incidentes criticos de acceso cruzado | FR-009, NFR-005 |
| RN-INV-01 | Stock fisico nunca queda negativo | MUST | 0 registros con `stock_fisico < 0` | FR-002, NFR-004 |
| RN-RES-01 | Reserva requiere disponibilidad suficiente y TTL activo | MUST | reserva rechazada si qty > disponibilidad | FR-004, NFR-004 |
| RN-RES-02 | Reserva en MVP es todo-o-nada | MUST | 0 reservas parciales en checkout | FR-004, NFR-004 |
| RN-ORD-01 | Pedido se materializa en `PENDING_APPROVAL` solo con reservas vigentes confirmables | MUST | 0 pedidos creados con reserva expirada | FR-004, NFR-004 |
| RN-ORD-02 | Reintento de confirmacion con misma correlacion no duplica pedido | MUST | max 1 pedido por `checkoutCorrelationId` | FR-004, NFR-006 |
| RN-ORD-03 | Paso de `PENDING_APPROVAL` a `CONFIRMED` requiere decision explicita de `order` y precondiciones validas | MUST | 0 transiciones a `CONFIRMED` sin origen permitido | FR-006, NFR-006 |
| RN-PAY-01 | Pago manual requiere monto > 0 y referencia valida | MUST | 100% pagos con monto positivo | FR-010, NFR-006 |
| RN-PAY-02 | Referencia de pago duplicada no duplica efecto | MUST | 0 dobles aplicaciones de pago | FR-010, NFR-006 |
| RN-NOTI-01 | Falla de notificacion no revierte core transaccional | MUST | pedido/pago se conservan ante fallo de envio | FR-006, NFR-007 |
| RN-REP-01 | Reporting no es fuente transaccional | MUST | cambios de pedido/stock no nacen en reporting | FR-003, FR-007 |
| RN-LOC-01 | Toda operacion requiere parametros operativos por pais vigentes para el tenant | MUST | 0 operaciones mutantes/computo semanal con fallback global no autorizado | FR-011, NFR-011 |

Regla de mapeo de error regional (`configuracion_pais_no_disponible`):
- resolucion directa del recurso de politica regional sin version vigente:
  `404` (ausencia de recurso tecnico);
- operacion de negocio bloqueada por precondicion regional no satisfecha:
  `409` (conflicto operacional).

## Catalogo de invariantes
_Responde: que verdades estructurales del dominio no pueden romperse y que restricciones de datos las aterrizan en registros persistidos._
Invariantes de dominio:
| ID | Invariante | Alcance | Error conceptual asociado |
|---|---|---|---|
| I-ACC-01 | Sesion activa solo para usuario habilitado y tenant valido | `identity-access` | `usuario_no_habilitado` |
| I-ACC-02 | Ninguna accion muta recursos fuera de tenant autorizado | Cross-BC | `acceso_cruzado_detectado` |
| I-CAT-01 | SKU identifica de forma unica una variante vendible por tenant | `catalog` | `sku_no_unico` |
| I-INV-01 | `stock_fisico >= 0` en todo momento | `inventory` | `stock_negativo_invalido` |
| I-INV-02 | `reservas_activas <= stock_fisico` | `inventory` | `reserva_invalida` |
| I-ORD-01 | Pedido confirmado implica reservas confirmadas para todas las lineas | `order` | `conflicto_checkout` |
| I-ORD-02 | Estado de pedido solo avanza por transiciones validas | `order` | `transicion_estado_invalida` |
| I-PAY-01 | Estado de pago deriva de pagos validos vs total del pedido | `order` | `estado_pago_inconsistente` |
| I-NOTI-01 | Notificacion no altera estado de pedido/pago | `notification` | `notificacion_fallida_no_bloqueante` |
| I-REP-01 | Vistas de reporting no mutan datos core | `reporting` | `operacion_no_permitida_en_reporting` |
| I-LOC-01 | Cada tenant tiene perfil de parametros operativos por pais activo y versionado | Cross-BC (`directory`, `order`, `reporting`) | `configuracion_pais_no_disponible` |

Invariantes de datos:
| ID | Invariante | Severidad |
|---|---|---|
| D-ACC-01 | email unico por tenant para cuentas activas | Alta |
| D-ACC-02 | sesion activa referencia `userId` y `tenantId` validos | Critica |
| D-DIR-01 | direccion usada en checkout pertenece a organizacion del pedido | Alta |
| D-DIR-02 | organizacion activa tiene `countryCode` y version de parametros operativos vigente | Critica |
| D-CAT-01 | SKU unico por variante vendible | Alta |
| D-INV-01 | stock fisico nunca negativo | Critica |
| D-INV-02 | reservas activas no exceden disponibilidad | Critica |
| D-ORD-01 | pedido confirmado contiene al menos una linea valida | Alta |
| D-ORD-02 | estado de pago deriva de pagos aplicados | Alta |
| D-PAY-01 | `paymentReference` no duplica aplicacion | Alta |
| D-CROSS-01 | todo registro mutable incluye `tenantId` | Critica |

## Reglas por bounded context
_Responde: que reglas e invariantes concentra cada bounded context como parte de su responsabilidad principal._
| BC | Reglas e invariantes principales |
|---|---|
| `identity-access` | RN-ACC-01, RN-ACC-02, I-ACC-01, D-ACC-01, D-ACC-02 |
| `directory` | RN-LOC-01, I-LOC-01, D-DIR-01, D-DIR-02 |
| `catalog` | I-CAT-01, D-CAT-01 |
| `inventory` | RN-INV-01, RN-RES-01, RN-RES-02, I-INV-01, I-INV-02, D-INV-01, D-INV-02 |
| `order` | RN-ORD-01, RN-ORD-02, RN-PAY-01, RN-PAY-02, I-ORD-01, I-ORD-02, I-PAY-01, D-ORD-01, D-ORD-02, D-PAY-01 |
| `notification` | RN-NOTI-01, I-NOTI-01 |
| `reporting` | RN-REP-01, I-REP-01 |
| Cross-BC | I-ACC-02, I-LOC-01, D-CROSS-01 |

## Contratos de operacion critica
_Responde: que debe ser cierto antes y despues de cada operacion relevante para que el dominio conserve consistencia._
| Operacion critica | Precondiciones semanticas | Postcondiciones semanticas |
|---|---|---|
| Crear pedido desde checkout | sesion valida, tenant match, direccion valida, reservas activas suficientes, SKU vendibles y parametros por pais vigentes | pedido creado en `PENDING_APPROVAL`, reservas confirmadas y snapshots de direccion/precio/politica regional congelados |
| Confirmar comercialmente pedido | pedido existente en `PENDING_APPROVAL`, actor autorizado y transicion permitida | pedido en `CONFIRMED` y evento `OrderConfirmed` emitido |
| Registrar pago manual | pedido existente, referencia no usada, monto > 0 y actor autorizado | pago registrado una sola vez por referencia y estado de pago recalculado |
| Crear reserva de stock | SKU valida, tenant valido, disponibilidad suficiente y TTL definido | reserva activa creada sin exceder `stock_fisico` |
| Cancelar pedido antes de confirmacion | pedido en estado cancelable y actor autorizado | pedido cancelado y reservas liberadas si aplica |
| Detectar carrito abandonado | carrito activo sin conversion dentro de la ventana de abandono | evento de abandono emitido y solicitud de notificacion no bloqueante creada |
| Ejecutar computo semanal por pais | organizacion con `countryCode` y parametros operativos vigentes | proyeccion/reporte semanal calculado con configuracion regional vigente |

## Transiciones invalidas
_Responde: que cambios de estado no estan permitidos y cual es la respuesta semantica cuando se intentan._
| Concepto | Transicion no permitida | Motivo | Respuesta semantica |
|---|---|---|---|
| `Order` | `PENDING_APPROVAL -> DELIVERED` | salta etapas reservadas fuera del baseline | `transicion_estado_invalida` |
| `Order` | `CONFIRMED -> PENDING_APPROVAL` | rompe cierre comercial del pedido | `transicion_estado_invalida` |
| `Order` | `CANCELLED -> CONFIRMED` | reabre un pedido ya terminado | `transicion_estado_invalida` |
| `Reservation` | `EXPIRED -> CONFIRMED` | una reserva vencida no puede consumirse | `reserva_expirada` |
| `Reservation` | `RELEASED -> ACTIVE` | una reserva liberada no se reactiva | `conflicto_reserva` |
| `NotificationRequest` | `SENT -> PENDING` | el envio ya tuvo cierre operacional | `operacion_no_permitida` |
| `Session` | `REVOKED -> ACTIVE` | la sesion debe recrearse, no revivirse | `token_expirado_o_revocado` |

## Control de violaciones
_Responde: como se manifiestan las rupturas esperadas del modelo, que severidad tienen y que mitigacion existe para contenerlas._
Matriz de violaciones esperadas:
| Regla/Invariante | Condicion de violacion | Respuesta semantica | Severidad |
|---|---|---|---|
| I-INV-01 | ajuste/reserva deja stock negativo | rechazar comando y auditar incidente | Critica |
| I-ORD-01 | reserva expira en checkout | rechazar confirmacion y ajustar carrito | Alta |
| I-ACC-02 | acceso a recurso de otro tenant | denegar operacion y elevar incidente | Critica |
| RN-PAY-02 | `paymentReference` duplicada | respuesta idempotente/rechazo sin efecto | Media |
| RN-NOTI-01 | falla de envio | marcar `NotificationFailed`, sin rollback | Media |
| RN-LOC-01 | perfil de pais ausente/no vigente | bloquear operacion y auditar incumplimiento de configuracion regional | Critica |

Riesgos por violacion y mitigaciones:
| Riesgo de ruptura | Consecuencia principal | Mitigacion definida |
|---|---|---|
| Sobreventa | reclamos y perdida de confianza comercial | RN-INV-01, RN-RES-01, I-INV-01 con rechazo temprano y auditoria |
| Acceso cruzado entre tenants | incidente de seguridad y no-go operativo | RN-ACC-02, I-ACC-02, D-CROSS-01 con bloqueo obligatorio |
| Pedido creado en estado invalido | inconsistencia comercial y reprocesos manuales | RN-ORD-01, I-ORD-01 con validacion previa a creacion |
| Duplicidad de pagos | inconsistencia financiera por doble aplicacion | RN-PAY-02, D-PAY-01 con idempotencia por referencia |
| Uso transaccional de reporting | estados invalidos y decisiones incorrectas | RN-REP-01, I-REP-01 con frontera estricta de BC |
| Operacion sin configuracion por pais | incumplimiento regional y fallas de calculo semanal | RN-LOC-01, I-LOC-01, D-DIR-02 con bloqueo por configuracion invalida |

## Enforcement semantico
_Responde: en que punto del modelo se hace cumplir cada regla o invariante y con que mecanismo principal se protege._
| Regla/Invariante | Punto principal de enforcement | Mecanismo |
|---|---|---|
| RN-ACC-01, RN-ACC-02, I-ACC-02 | validacion de comando y ACL de entrada | chequeo de sesion/rol/tenant antes de mutacion |
| RN-INV-01, RN-RES-01, RN-RES-02, I-INV-01, I-INV-02 | agregado de inventario | guard clauses e invariantes transaccionales locales |
| RN-ORD-01, RN-ORD-02, I-ORD-01, I-ORD-02 | agregado/politicas de `order` | validacion de transicion e idempotencia por correlacion |
| RN-PAY-01, RN-PAY-02, I-PAY-01 | comando de pago y agregado de pedido | dedupe por referencia y recalculo deterministico de estado |
| RN-NOTI-01, I-NOTI-01 | politica de notificacion | desacople asincrono sin rollback de core |
| RN-REP-01, I-REP-01 | frontera de `reporting` | prohibicion de mutaciones transaccionales |
| RN-LOC-01, I-LOC-01 | ACL `directory -> order/reporting` | resolucion de parametros por pais antes de operar |

## Trazabilidad normativa
_Responde: como se conectan las reglas e invariantes del dominio con bounded contexts y requisitos del producto._
| Reglas/Invariantes | BCs principales | RF/RNF relacionados |
|---|---|---|
| RN-ACC-* / I-ACC-* | `identity-access`, Cross-BC | FR-009, NFR-005 |
| RN-INV-* RN-RES-* / I-INV-* | `inventory`, `order` | FR-002, FR-004, FR-005, NFR-004 |
| RN-ORD-* RN-PAY-* / I-ORD-* I-PAY-* | `order`, `inventory` | FR-004, FR-005, FR-006, FR-010, NFR-006 |
| RN-NOTI-* / I-NOTI-* | `notification`, `order` | FR-006, FR-008, FR-010, NFR-007 |
| RN-REP-* / I-REP-* | `reporting` | FR-003, FR-007, NFR-002 |
| RN-LOC-* / I-LOC-* | `directory`, `order`, `reporting` | FR-011, NFR-011 |
| D-CROSS-01 | Cross-BC | NFR-005, NFR-006 |

## Historial normativo
_Responde: que decisiones relevantes modificaron este baseline normativo y que impacto tuvieron sobre el dominio._
| Fecha | Tipo | Cambio | Impacto |
|---|---|---|---|
| 2026-03-11 | Baseline | Se consolida catalogo global RN/I/D para `MVP`. | Base unica para validaciones semanticas en dominio. |
| 2026-03-11 | Mejora de estructura | Se organiza en orden abstracto->concreto y se agregan secciones de enforcement y trazabilidad resumida. | Cierre de coherencia entre reglas, BCs y RF/RNF. |
| 2026-03-13 | Reorganizacion editorial | Se separan pre/postcondiciones, se agregan transiciones invalidas y se mueve compatibilidad semantica a `Evolucion`. | El archivo queda enfocado en reglas, invariantes y enforcement del dominio. |
