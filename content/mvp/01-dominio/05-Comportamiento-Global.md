---
title: "Comportamiento Global"
linkTitle: "5. Comportamiento Global"
weight: 5
url: "/mvp/dominio/comportamiento-global/"
---

## Marco del comportamiento
_Responde: para que existe esta vista dinamica del dominio, que cubre y de donde obtiene su autoridad semantica._

Describir como evoluciona el dominio ArkaB2B a traves del tiempo en
`MVP`: procesos criticos, escenarios clave, comandos validos,
hechos publicados, politicas reactivas, transiciones de estado y
ownership de decisiones entre bounded contexts.

- `Mapa de Contexto` para fronteras, relaciones y ownership semantico.
- `Lenguaje Ubicuo` y `Conceptos` para significado de comandos, eventos y estados.
- `Reglas e Invariantes` para precondiciones, postcondiciones y enforcement.
- `Eventos de Dominio` y `Contratos de Integracion` para hechos e interacciones sync/async.
- Dossiers de `Contextos Delimitados` para detalle tactico por BC.

## Dinamica operativa
_Responde: cuales son los procesos principales del MVP y como se materializan en escenarios representativos del flujo de negocio._
Procesos criticos del dominio:
| Proceso | Owner del proceso | Contextos involucrados | Resultado esperado |
|---|---|---|---|
| `P1 Gestion de acceso B2B` | `identity-access` | `identity-access`, `directory` | sesion valida con tenant/rol y perfil organizacional local coherente con `directory` |
| `P2 Compra recurrente` | `order` | `order`, `catalog`, `inventory`, `directory`, `identity-access` | pedido creado en `PENDING_APPROVAL` sin sobreventa |
| `P3 Actualizacion de stock` | `inventory` | `inventory`, `reporting` | disponibilidad coherente y auditable por SKU/almacen |
| `P4 Ciclo de estado de pedido` | `order` | `order`, `notification`, `reporting` | transiciones comerciales consistentes y comunicables |
| `P5 Abastecimiento semanal` | `reporting` | `inventory`, `reporting`, `notification` | backlog semanal accionable por bajo stock |
| `P6 Recuperacion de carrito` | `order` | `order`, `notification`, `reporting` | deteccion de abandono y recordatorio no bloqueante |
| `P7 Registro de pago manual` | `order` | `order`, `notification`, `reporting` | pago aplicado idempotentemente y estado recalculado |
| `P8 Reporte semanal de ventas` | `reporting` | `reporting`, `order`, `catalog`, `inventory`, `directory` | consolidado semanal por tenant/pais |
| `P9 Operacion regional por pais` | `directory` + `order` | `directory`, `order`, `reporting` | reglas de moneda, corte y retencion aplicadas por organizacion |

Escenarios clave del MVP:
| Escenario | Happy path | Desvios relevantes | Resultado semantico |
|---|---|---|---|
| Login B2B | `autenticar_usuario` crea sesion activa | usuario bloqueado, credenciales invalidas | no hay mutacion si identidad no es valida |
| Upsert de carrito | variante vendible + reserva activa | variante no vendible, stock insuficiente, acceso cruzado | carrito mantiene solo items operables |
| Validacion de checkout | direccion valida + reservas vigentes + politica regional vigente | conflicto de checkout, direccion invalida, reserva expirada, configuracion de pais ausente | checkout queda `VALID` o se rechaza sin pedido parcial |
| Creacion de pedido | `confirmar_pedido` crea `OrderCreated` en `PENDING_APPROVAL` | conflicto de idempotencia o invalidacion final | no existe pedido duplicado ni con snapshots inconsistentes |
| Confirmacion comercial | `confirmar_aprobacion_pedido` emite `OrderConfirmed` | transicion invalida o actor no autorizado | solo `order` cierra el compromiso comercial |
| Pago manual | `registrar_pago_manual` aplica referencia unica | referencia duplicada o monto invalido | estado de pago recalculado sin doble efecto |
| Recuperacion de carrito | timer detecta abandono y `notification` reacciona | destinatario invalido o provider timeout | fallo de notificacion no altera el carrito |
| Reporte semanal | `reporting` consume hechos y genera corte por pais | lag, evento duplicado, ventana invalida | se reconstruye proyeccion sin tocar BCs core |

Regla de lectura operacional para regionalizacion:
- cuando `directory` resuelve directamente politica por `countryCode` y no hay
  version vigente, la ausencia se expresa como `404` del recurso tecnico;
- cuando `order` o `reporting` ejecutan una operacion de negocio sin esa
  precondicion, el bloqueo se expresa como `409` con
  `configuracion_pais_no_disponible`.

## Interacciones del dominio
_Responde: que intenciones existen, que hechos se publican y que reacciones dispara el dominio cuando el flujo avanza o falla._
Comandos del dominio:
| Familia de comando | BC owner | Agregado/objeto principal | Quien lo emite | Efecto semantico esperado |
|---|---|---|---|---|
| acceso y sesion (`autenticar_usuario`, `refrescar_sesion`, `revocar_sesion`) | `identity-access` | `Session` / `User` | actor B2B o operacion de seguridad | abre/cierra capacidad de operar por tenant |
| gobierno organizacional (`registrar_organizacion`, `configurar_parametros_operativos_pais`, `registrar_direccion`, `registrar_perfil_usuario_organizacion`, `registrar_contacto`) | `directory` | `Organization` | operacion/administracion | habilita verdad organizacional, perfiles locales, comunicacion institucional y regionalizacion |
| catalogo comercial (`registrar_producto`, `crear_variante`, `actualizar_precio_vigente`) | `catalog` | `Product` / `SkuVariant` | operacion comercial | determina vendible y precio vigente |
| inventario (`ajustar_stock`, `crear_reserva`, `confirmar_reserva`, `liberar_reserva`) | `inventory` | `InventoryAggregate` / `ReservationAggregate` | operacion o `order` via contrato | protege disponibilidad y anti-sobreventa |
| compra (`agregar_o_actualizar_item_carrito`, `solicitar_validacion_checkout`, `confirmar_pedido`, `confirmar_aprobacion_pedido`) | `order` | `CartAggregate` / `OrderAggregate` | usuario B2B / operacion comercial | transforma intencion en pedido comercial valido |
| pago (`registrar_pago_manual`) | `order` | `PaymentAggregate` | operacion financiera/comercial | aplica cobro manual y recalcula estado |
| comunicacion operacional (`solicitar_notificacion`, `reintentar_notificacion`) | `notification` | `NotificationAggregate` | reaccion a eventos core | intenta entrega sin bloquear el negocio |
| proyeccion analitica (`registrar_hecho_analitico`, `refrescar_vista_*`, `generar_reporte_semanal`) | `reporting` | `ReportingViewAggregate` / `WeeklyReportAggregate` | consumo interno de eventos | materializa lectura y reporte semanal |

Eventos del dominio:
| Familia de eventos | Productor | Significado estable | Para que existen en el flujo |
|---|---|---|---|
| acceso (`UserLoggedIn`, `AuthFailed`, `SessionRevoked`, `SessionsRevokedByUser`, `RoleAssigned`, `UserBlocked`) | `identity-access` | hechos de identidad, sesion y autorizacion | habilitar/bloquear mutaciones, actualizar contexto de seguridad y auditar |
| directorio (`OrganizationProfileUpdated`, `CountryOperationalPolicyConfigured`, `AddressUpdated`, `ContactUpdated`, `PrimaryContactChanged`, `CheckoutAddressValidated`) | `directory` | hechos sobre organizacion, direccion, contacto institucional y politica regional | mantener consistencia de checkout, comunicacion y reporting |
| catalogo (`Product*`, `Variant*`, `Price*`) | `catalog` | cambios comerciales sobre producto/SKU/precio | resolver vendible/precio y alimentar analitica |
| inventario (`Stock*`, `LowStockDetected`) | `inventory` | cambios reales de stock y reservas | sostener anti-sobreventa y abastecimiento |
| compra y pago (`Cart*`, `Order*`) | `order` | evolucion comercial del carrito/pedido/pago | comunicar el ciclo de compra a soporte y analitica |
| notificacion (`Notification*`) | `notification` | resultado operacional de envios | medir entrega/descarte sin tocar core |
| analitica (`AnalyticFactUpdated`, `WeeklyReportGenerated`) | `reporting` | consolidacion derivada del negocio | cerrar lectura semanal y distribucion del reporte |

El catalogo detallado y el envelope canonico se mantienen en [Eventos de Dominio](/mvp/dominio/eventos-dominio/).

Politicas reactivas:
| Trigger | BC que reacciona | Accion resultante | Regla de negocio asociada |
|---|---|---|---|
| `StockReservationExpired` | `order` | invalidar item o checkout y ajustar carrito | no confirmar pedido con reserva vencida |
| `VariantDiscontinued` | `order` | remover item no operable del carrito | no mantener SKU no vendible en compra activa |
| `OrganizationSuspended` | `identity-access` | bloquear usuarios del tenant y revocar sesiones activas | el directorio gobierna la operabilidad organizacional |
| `OrderConfirmed` | `notification` | crear solicitud de confirmacion comercial | la comunicacion no bloquea el core |
| `OrderPaymentRegistered` | `notification` | crear solicitud de aviso de pago | informar avance sin recalcular negocio fuera de `order` |
| `LowStockDetected` | `notification` / `reporting` | alertar y consolidar abastecimiento | sostener seguimiento semanal |
| `NotificationFailed` | `reporting` | registrar efectividad/descarte | el fallo no revierte pedido/pago |
| `RoleAssigned` | `directory` | actualizar perfil de usuario organizacional vinculado | alinear contexto local del usuario sin duplicar permisos efectivos |
| `UserBlocked` | `directory` | inactivar perfil de usuario organizacional vinculado | ningun perfil de usuario organizacional local puede seguir operativo si IAM bloquea al usuario |
| `UserBlocked` | `order` | bloquear nuevas mutaciones y cancelar segun politica | ningun actor bloqueado sigue operando |

## Gobierno del flujo
_Responde: quien mueve las decisiones relevantes del dominio y bajo que transiciones de estado avanza cada concepto principal._
Transiciones de estado globales:
| Concepto | Transiciones permitidas en `MVP` | Notas |
|---|---|---|
| `Cart` | `ACTIVE -> CHECKOUT_IN_PROGRESS -> CONVERTED` ; `ACTIVE -> ABANDONED` ; `ACTIVE -> CANCELLED` | `CONVERTED` ocurre al crear pedido valido |
| `CheckoutAttempt` | `INVALID -> VALID` | el intento puede volver a invalidarse por una nueva evaluacion, pero no crea pedido parcial |
| `Order` | `CREATED (interno) -> PENDING_APPROVAL -> CONFIRMED` ; `PENDING_APPROVAL|CONFIRMED -> CANCELLED` | `READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED` quedan reservados |
| `Reservation` | `ACTIVE -> CONFIRMED|EXPIRED|RELEASED` | no existe vuelta a `ACTIVE` desde un estado terminal |
| `PaymentStatus` | `PENDING -> PARTIALLY_PAID -> PAID` ; `PENDING|PARTIALLY_PAID -> OVERPAID_REVIEW` | deriva de pagos validos, no de edicion manual libre |
| `NotificationRequest` | `PENDING -> SENT|FAILED|DISCARDED` | el retry reaplica sobre la solicitud; no reabre `SENT` |
| `WeeklyReportExecution` | `PENDING -> RUNNING -> COMPLETED|FAILED` | un reporte `COMPLETED` es inmutable |

Ownership de decisiones:
| Proceso critico | Quien inicia | Quien decide | Quien confirma/cierra | Quien solo reacciona |
|---|---|---|---|---|
| Validacion de checkout | `order` | `directory` valida direccion/pais, `inventory` valida reservas, `order` decide crear pedido | `order` deja el pedido en `PENDING_APPROVAL` | `reporting` observa |
| Confirmacion comercial de pedido | `order` / operacion comercial | `order` | `order` emite `OrderConfirmed` | `notification`, `reporting` |
| Ciclo de reserva | `order` solicita | `inventory` | `inventory` confirma/expira/libera | `order`, `reporting`, `notification` |
| Registro de pago manual | operacion comercial/financiera | `order` | `order` recalcula `PaymentStatus` | `notification`, `reporting` |
| Notificacion por hechos de negocio | eventos core | `notification` decide canal, retry o descarte | `notification` cierra `SENT/FAILED/DISCARDED` | `reporting` consolida |
| Consolidacion semanal | cron/operacion interna | `reporting` decide ventana y rebuild | `reporting` genera salida semanal | `notification` distribuye reporte |

## Garantias operativas
_Responde: que garantias de idempotencia y correlacion necesita el flujo para conservar consistencia bajo reintentos, asincronia y observabilidad._
- Toda mutacion transporta `tenantId`, `actorId`, `traceId`, `correlationId`.
- Comandos mutantes usan `Idempotency-Key` o una referencia natural equivalente.
- Eventos se entregan `at-least-once`; los consumidores deduplican por `eventId`.
- La correlacion de checkout, pedido, pago y notificacion se preserva a traves del mismo `correlationId`.

Claves operativas recomendadas:
| Operacion | Clave de correlacion/idempotencia |
|---|---|
| autenticacion | `tenant:user:requestId` |
| upsert carrito | `tenant:user:sku:intent` |
| validar checkout | `tenant:user:checkoutCorrelationId` |
| crear/confirmar reserva | `tenant:reservationId:orderId` |
| confirmar pedido | `tenant:user:checkoutCorrelationId` |
| registrar pago manual | `tenant:orderId:paymentReference` |
| solicitar notificacion | `eventId:channel:recipientRef` |
| generar reporte semanal | `tenant:weekId:reportType` |

## Trazabilidad del comportamiento
_Responde: que partes del comportamiento cubren requisitos concretos del producto y donde se concentra esa evidencia dinamica._
| Seccion | Cubre principalmente |
|---|---|
| Procesos criticos | FR-004, FR-005, FR-006, FR-007, FR-008, FR-010, FR-011 |
| Escenarios clave | FR-004, FR-006, FR-008, FR-010; NFR-004, NFR-006, NFR-007, NFR-011 |
| Ownership de decisiones | FR-004, FR-006, FR-010; NFR-004, NFR-007 |
| Idempotencia y correlacion | NFR-006, NFR-007 |

## Historial del comportamiento
_Responde: que decisiones relevantes modificaron la dinamica global del dominio y que impacto tuvieron sobre el baseline conductual._
| Fecha | Tipo | Cambio | Impacto |
|---|---|---|---|
| 2026-03-13 | Baseline | Se consolida la capa de comportamiento global con procesos, escenarios, ownership y transiciones de estado. | Queda explicita la dinamica del dominio entre reglas, eventos y tactica por BC. |
