---
title: "Lenguaje Ubicuo"
linkTitle: "2. Lenguaje Ubicuo"
weight: 2
url: "/mvp/dominio/lenguaje-ubicuo/"
---

## Gobierno del lenguaje
_Responde: para que existe este lenguaje, de donde obtiene su autoridad semantica y con que artefactos debe mantenerse alineado._

Unificar lenguaje semantico del dominio ArkaB2B para comandos, eventos,
errores y estados, evitando sinonimos no controlados entre Producto, Dominio
y Arquitectura.

- RF/RNF aprobados en `/mvp/producto/catalogo-rf/` y `/mvp/producto/catalogo-rnf/`.
- Fronteras estrategicas definidas en `/mvp/dominio/mapa-contexto/`.
- Terminos funcionales base definidos en `/mvp/producto/glosario/`.
- Evidencia funcional en adjuntos de producto (`/mvp/producto/adjuntos/`).

## Normas del lenguaje
_Responde: que reglas gobiernan el uso correcto del vocabulario y como debe aplicarse en el resto del pilar._
- Un concepto de negocio tiene un solo nombre canonico.
- Si aparece un termino nuevo, se actualiza este archivo antes de usarlo en contratos.
- Comandos expresan intencion; eventos expresan hecho confirmado.
- Terminos de operaciones sync/async deben mantener el mismo significado en todos los BC.
- Los eventos de contratos tecnicos usan naming en ingles alineado con Arquitectura.
- Todo archivo en `content/mvp/01-dominio/*` debe usar este vocabulario.
- Si hay conflicto con Producto, prevalece `/mvp/producto/glosario/` y se alinea este archivo.

## Diccionario canonico
_Responde: que significan los conceptos del dominio, que sinonimos son aceptables y que terminos no pueden tratarse como equivalentes._
Sinonimos aceptados:
| Termino canonico | Sinonimos aceptados | Restriccion de uso |
|---|---|---|
| `Usuario B2B` | usuario de organizacion | no reemplaza `actorId` ni `tenantId` |
| `Reserva` | reserva de stock | fuera de `inventory` debe conservar mismo significado |
| `Parametro operativo por pais` | configuracion regional vigente | solo cuando el contexto ya explico `countryCode` |
| `Carrito abandonado` | abandono de carrito | no implica perdida de pedido ni cancelacion |

Conceptos transversales del negocio:
| Concepto | Definicion operativa |
|---|---|
| Creacion de pedido | materializacion del pedido tras checkout valido en estado `PENDING_APPROVAL` |
| Confirmacion de pedido | transicion valida de `PENDING_APPROVAL` a `CONFIRMED` con reservas ya confirmadas |
| Acceso cruzado | operacion sobre recurso de otro tenant sin permiso |
| Pago duplicado | mismo `paymentReference` intenta aplicarse de nuevo al mismo pedido |
| Conflicto de checkout | inconsistencia de direccion/reserva al validar o confirmar |
| Faltante de abastecimiento | SKU con cobertura por debajo del umbral semanal |

Glosario canonico global:
| Termino | Definicion semantica | BC referente | Sinonimo no permitido |
|---|---|---|---|
| Tenant | frontera tecnica de aislamiento, autorizacion y trazabilidad de una operacion; IAM lo usa como contexto canonico de acceso y `directory` lo referencia para operar la organizacion correcta | `identity-access` + `directory` | organizacion |
| Organizacion | entidad comercial B2B cuya operacion se referencia desde un tenant y que posee direcciones, perfiles de usuario organizacionales, contactos institucionales y politica regional | `directory` | tenant |
| Usuario B2B | persona autenticable que opera dentro de un tenant y se vincula a una organizacion B2B | `identity-access` | usuario global |
| Perfil de usuario organizacional | representacion local del usuario IAM dentro de una organizacion para contexto operativo, ownership y estado local | `directory` | usuario autenticable |
| Contacto institucional | canal institucional de una organizacion usado para comunicacion operativa o comercial (`email`, `telefono`, `whatsapp`, `sitio web`, etc.) | `directory` | persona de contacto |
| Rol de negocio | permiso semantico para operar comandos | `identity-access` | admin total |
| Sesion activa | estado temporal valido de autenticacion | `identity-access` | sesion iniciada sin validar |
| Producto | agrupador comercial de variantes | `catalog` | item maestro |
| Variante (SKU) | unidad vendible concreta | `catalog` | item suelto |
| Vendible | habilitacion comercial de una variante | `catalog` | disponible |
| Stock fisico | unidades reales en inventario | `inventory` | stock comercial |
| Disponibilidad | `stock fisico - reservas activas` | `inventory` | disponibilidad comercial |
| Reserva | apartado temporal con TTL para checkout | `inventory` | bloqueo indefinido |
| Carrito | intencion de compra editable | `order` | pedido borrador |
| Pedido | entidad comercial de compra materializada tras checkout | `order` | orden finalizada |
| Estado de pedido | etapa valida del ciclo del pedido | `order` | estado final |
| Estado de pago | estado agregado derivado de pagos validos | `order` | estado financiero libre |
| Carrito abandonado | carrito sin conversion en ventana de abandono | `order` | carrito perdido |
| Sobreventa | pedido confirmado sin disponibilidad real suficiente | `inventory` + `order` | quiebre normal |
| Solicitud de notificacion | intencion de envio no bloqueante | `notification` | envio garantizado |
| Hecho analitico | vista derivada para decision, no transaccional | `reporting` | dato maestro |
| Parametro operativo por pais | configuracion versionada por `countryCode` que define moneda, corte semanal y retencion aplicable por organizacion | `directory` | fallback global implicito |

Terminos sensibles o no intercambiables:
| Termino A | Termino B | Diferencia obligatoria |
|---|---|---|
| `tenant` | `organizacion` | `tenant` delimita aislamiento/autorizacion; `organizacion` representa la entidad comercial operativa |
| `producto` | `variante (SKU)` | producto agrupa; variante es la unidad vendible concreta |
| `vendible` | `disponible` | `catalog` decide si se puede vender; `inventory` decide si hay stock reservable |
| `carrito` | `pedido` | carrito es intencion editable; pedido es compromiso comercial materializado |
| `OrderCreated` | `OrderConfirmed` | el primero crea pedido en `PENDING_APPROVAL`; el segundo cierra confirmacion comercial |
| `solicitud de notificacion` | `notificacion enviada` | la primera es intencion operacional; la segunda es resultado de entrega |
| `hecho analitico` | `dato maestro` | el primero es derivado y recalculable; el segundo es fuente de verdad de otro BC |
| `perfil de usuario organizacional` | `usuario B2B` | el perfil local vive en `directory`; la identidad autenticable y los permisos efectivos viven en `identity-access` |
| `contacto institucional` | `persona de contacto` | el primero es un canal institucional; el segundo implicaria una entidad personal distinta |

## Vocabulario por contexto delimitado
_Responde: que vocabulario clave usa cada bounded context dentro de su propia frontera._
| BC | Vocabulario clave |
|---|---|
| `identity-access` | Usuario B2B, Rol de negocio, Sesion activa |
| `directory` | Organizacion, Perfil de usuario organizacional, Contacto institucional, Parametro operativo por pais |
| `catalog` | Producto, Variante (SKU), Vendible |
| `inventory` | Stock fisico, Disponibilidad, Reserva, Sobreventa |
| `order` | Carrito, Pedido, Estado de pedido, Estado de pago, Carrito abandonado |
| `notification` | Solicitud de notificacion |
| `reporting` | Hecho analitico |

## Estados canonicos
_Responde: que estados son validos para los conceptos relevantes y cual es la semantica oficial del ciclo de pedido del MVP._
| Agregado | Estados permitidos | Estado inicial | Estado terminal |
|---|---|---|---|
| `Session` | `ACTIVE`, `REVOKED`, `EXPIRED` | `ACTIVE` | `REVOKED`, `EXPIRED` |
| `Organization` | `ONBOARDING`, `ACTIVE`, `SUSPENDED`, `INACTIVE` | `ONBOARDING` | `INACTIVE` |
| `SkuVariant` | `DRAFT`, `SELLABLE`, `DISCONTINUED` | `DRAFT` | `DISCONTINUED` |
| `Reservation` | `ACTIVE`, `CONFIRMED`, `EXPIRED`, `RELEASED` | `ACTIVE` | `CONFIRMED`, `EXPIRED`, `RELEASED` |
| `Cart` | `ACTIVE`, `CHECKOUT_IN_PROGRESS`, `CONVERTED`, `ABANDONED`, `CANCELLED` | `ACTIVE` | `CONVERTED`, `ABANDONED`, `CANCELLED` |
| `CheckoutAttempt` | `INVALID`, `VALID` | `INVALID` | `VALID`, `INVALID` |
| `Order` | `CREATED (interno)`, `PENDING_APPROVAL`, `CONFIRMED`, `CANCELLED`, `READY_TO_DISPATCH (RESERVED)`, `DISPATCHED (RESERVED)`, `DELIVERED (RESERVED)` | `CREATED (interno)` | `CANCELLED` (MVP), `DELIVERED` (RESERVED) |
| `PaymentStatus` | `PENDING`, `PARTIALLY_PAID`, `PAID`, `OVERPAID_REVIEW` | `PENDING` | `PAID`, `OVERPAID_REVIEW` |
| `NotificationRequest` | `PENDING`, `SENT`, `FAILED`, `DISCARDED` | `PENDING` | `SENT`, `FAILED`, `DISCARDED` |

Semantica oficial de ciclo de pedido (MVP):
- `CREATED` existe solo como estado interno de construccion dentro de `order`; el primer estado observable/persistido del baseline es `PENDING_APPROVAL`.
- `OrderCreated`: el pedido se materializa en `PENDING_APPROVAL`; no equivale a confirmacion comercial final.
- `OrderConfirmed`: evento de transicion a `CONFIRMED`; representa confirmacion comercial final de pedido.
- `READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED` quedan `RESERVED` para evolucion posterior, fuera del baseline operativo de `MVP`.

## Acciones o verbos del dominio
_Responde: que intenciones de negocio existen, que significan y que bounded context es responsable de ejecutarlas._
| Verbo de dominio | Significado | BC owner |
|---|---|---|
| autenticar usuario | iniciar sesion valida para un tenant/actor | `identity-access` |
| configurar parametros operativos por pais | fijar moneda/corte/retencion vigentes por organizacion | `directory` |
| registrar perfil de usuario organizacional | vincular un usuario IAM a la operacion local de una organizacion | `directory` |
| registrar contacto institucional | mantener un canal institucional operativo por organizacion y tipo | `directory` |
| resolver variante checkout | determinar si una SKU es vendible y con que precio vigente | `catalog` |
| crear reserva | apartar stock temporal para carrito/checkout | `inventory` |
| confirmar reserva | consumir una reserva activa para sostener un pedido | `inventory` |
| validar checkout | comprobar direccion, reservas y politica regional antes de crear pedido | `order` con `directory` + `inventory` |
| confirmar pedido | materializar pedido en `PENDING_APPROVAL` con snapshots inmutables | `order` |
| confirmar aprobacion de pedido | cerrar decision comercial y pasar a `CONFIRMED` | `order` |
| registrar pago manual | aplicar un pago valido y recalcular estado de pago | `order` |
| solicitar notificacion | crear una intencion de envio no bloqueante por un hecho de negocio | `notification` |
| generar reporte semanal | consolidar vistas derivadas por tenant/pais y emitir salida semanal | `reporting` |

## Diccionario de errores de negocio
_Responde: que errores semanticos reconoce el dominio, que significan y si admiten recuperacion operativa._
| Error | Significado | Recuperable |
|---|---|---|
| `usuario_no_habilitado` | usuario bloqueado/deshabilitado | No |
| `usuario_no_encontrado` | usuario objetivo no existe en el contexto de identidad y acceso | No |
| `acceso_cruzado_detectado` | mismatch de tenant/ownership | No |
| `credenciales_invalidas` | credenciales no validas para autenticar al usuario solicitado | Si |
| `sesion_no_encontrada` | sesion objetivo no existe o no esta disponible para revocacion/consulta | No |
| `rol_invalido` | rol solicitado no es valido para la operacion o el tenant | No |
| `token_expirado_o_revocado` | token no valido por expiracion, revocacion o formato invalido | Si |
| `auditoria_no_persistida` | no fue posible persistir evidencia de auditoria requerida para seguridad | No |
| `product_code_duplicado` | codigo de producto ya registrado para el tenant | No |
| `brand_o_categoria_invalida` | referencia de marca o categoria no valida para el catalogo | Si |
| `producto_no_activo` | producto no esta en estado permitido para la operacion | Si |
| `producto_retirado` | producto retirado no admite operaciones comerciales mutantes | No |
| `sku_no_unico` | SKU ya existe para el tenant y rompe unicidad de variante | No |
| `variante_no_vendible` | SKU no habilitada comercialmente | Si |
| `variante_descontinuada` | variante descontinuada no puede volver al flujo comercial activo | No |
| `stock_insuficiente` | qty solicitada supera disponibilidad | Si |
| `precio_invalido` | valor de precio no cumple reglas minimas del dominio | Si |
| `periodo_precio_solapado` | ventana de precio se cruza con otra vigente/programada para la misma variante | No |
| `required_attributes_missing` | faltan atributos requeridos para habilitar variante vendible | Si |
| `reserva_expirada` | reserva vencida al confirmar/usar | Si |
| `reserva_no_encontrada` | la reserva solicitada no existe en el contexto de inventario | No |
| `conflicto_reserva` | la reserva no puede transicionar por estado o concurrencia | Si |
| `conflicto_checkout` | validacion de checkout inconsistente | Si |
| `stock_negativo_invalido` | la mutacion propuesta deja `stock_fisico` en valor invalido | No |
| `contencion_stock` | bloqueo o contencion concurrente sobre SKU/almacen impide procesar la operacion | Si |
| `direccion_invalida` | direccion invalida o ajena | Si |
| `direccion_no_encontrada` | direccion solicitada no existe en la organizacion | Si |
| `direccion_no_pertenece_a_organizacion` | direccion no corresponde al tenant/organizacion de la operacion | No |
| `direccion_default_inconsistente` | no se puede mantener una sola direccion predeterminada por tipo | Si |
| `transicion_estado_invalida` | ciclo de pedido invalido | No |
| `tax_id_invalido` | identificacion tributaria no cumple reglas de pais/tipo | Si |
| `tax_id_duplicado` | identificacion tributaria ya registrada para una organizacion activa | No |
| `organizacion_no_encontrada` | organizacion objetivo no existe | No |
| `organizacion_inactiva` | organizacion no habilitada para la operacion solicitada | Si |
| `perfil_usuario_organizacion_duplicado` | ya existe un perfil local para la misma organizacion y usuario IAM | No |
| `perfil_usuario_organizacion_no_encontrado` | perfil local requerido no existe en la organizacion consultada | No |
| `perfil_usuario_organizacion_inactivo` | perfil local del usuario no esta habilitado para operar en la organizacion | Si |
| `contacto_duplicado` | contacto institucional ya existe para la organizacion bajo reglas de unicidad | Si |
| `contacto_primario_inconsistente` | no se cumple unicidad de contacto institucional primario por tipo | Si |
| `pago_duplicado` | referencia ya aplicada | Depende |
| `monto_pago_invalido` | monto <= 0 | Si |
| `notificacion_fallida_no_bloqueante` | envio fallido sin rollback de core | Si |
| `canal_no_disponible` | canal configurado no esta disponible para procesar el envio | Si |
| `destinatario_invalido` | destinatario no resoluble o no valido para el canal requerido | Si |
| `maximo_reintentos_excedido` | se agotaron intentos permitidos de reenvio para la solicitud | No |
| `provider_timeout` | proveedor de notificacion no respondio dentro de la ventana de envio | Si |
| `evento_duplicado` | evento fuente ya fue aplicado y no debe reprocesarse | No |
| `hecho_analitico_invalido` | payload analitico no cumple contrato semantico esperado | Si |
| `reporte_duplicado` | reporte ya generado para la misma ventana y tipo | No |
| `ventana_reporte_invalida` | ventana temporal solicitada no es valida para consolidacion | Si |
| `reporte_generacion_fallida` | generacion o exportacion del reporte no finalizo correctamente | Si |
| `validacion_externa_no_disponible` | servicio externo de validacion no disponible temporalmente | Si |
| `configuracion_pais_no_disponible` | operacion requiere parametros de pais inexistentes/no vigentes | No |
| `cart_not_found` | carrito objetivo no existe o no esta disponible para la operacion | No |
| `item_invalido` | item de carrito/pedido no cumple reglas minimas de validez | Si |
| `estado_pago_inconsistente` | estado agregado de pago no corresponde a pagos registrados y total | No |
| `operacion_no_permitida` | accion rechazada por estado, rol o politica de negocio vigente | No |

## Expresion del lenguaje
_Responde: como debe expresarse el lenguaje cuando sale a comandos, eventos y contratos para preservar consistencia, idempotencia y trazabilidad._
Convenciones de nombres y contratos semanticos:
| Tipo | Regla | Ejemplo |
|---|---|---|
| Comando conceptual | `verbo_objeto` en imperativo | `confirmar_pedido`, `registrar_pago_manual`, `crear_reserva` |
| Evento de contrato | `UpperCamelCase` en ingles | `OrderCreated`, `StockReservationExpired`, `UserBlocked` |
| Error conceptual | `snake_case` descriptivo de negocio | `stock_insuficiente`, `acceso_cruzado_detectado` |
| Topic de broker | `<bc>.<event-name>.v<major>` | `order.order-created.v1`, `inventory.stock-reservation-expired.v1` |

Convenciones de idempotencia semantica:
- `MUST`: comandos mutantes usan `Idempotency-Key` o referencia natural equivalente.
- Claves recomendadas:
  - carrito: `tenant:user:sku:intent`.
  - checkout: `tenant:user:checkoutCorrelationId`.
  - reserva: `tenant:warehouse:cart:sku`.
  - pago: `tenant:orderId:paymentReference`.
- Misma clave + mismo payload -> resultado equivalente.
- Misma clave + payload distinto -> `conflicto_idempotencia`.

Envelope canonico para eventos de dominio:
- Campos minimos: `eventType`, `eventVersion`, `eventId`, `occurredAt`, `tenantId`, `traceId`, `correlationId`.
- Entrega esperada: `at-least-once`; consumidores deben ser idempotentes.

## Trazabilidad semantica (termino -> BC -> RF/RNF)
_Responde: que terminos canonicos soportan cada bounded context y a que requisitos funcionales o no funcionales contribuyen._
| Termino canonico | BC principal | RF/RNF relacionados |
|---|---|---|
| Organizacion | `directory` | FR-009, FR-011, NFR-005, NFR-011 |
| Perfil de usuario organizacional | `directory` | FR-009, NFR-005 |
| Contacto institucional | `directory` + `notification` | FR-006, NFR-006 |
| Usuario B2B / Rol de negocio / Sesion activa | `identity-access` | FR-009, NFR-005 |
| Variante (SKU) / Vendible | `catalog` | FR-001, FR-004, FR-007 |
| Stock fisico / Disponibilidad / Reserva | `inventory` | FR-002, FR-003, FR-004, FR-005, NFR-004 |
| Carrito / Pedido | `order` | FR-004, FR-005, FR-006, FR-008, FR-010 |
| Estado de pedido / Estado de pago | `order` | FR-006, FR-010, NFR-006 |
| Carrito abandonado | `order` | FR-008, NFR-007 |
| Solicitud de notificacion | `notification` | FR-006, FR-008, FR-010, NFR-007 |
| Hecho analitico | `reporting` | FR-003, FR-007, FR-010, NFR-002 |
| Parametro operativo por pais | `directory` + `order` + `reporting` | FR-011, NFR-011 |

## Historial del lenguaje
_Responde: que decisiones semanticas cambiaron en el tiempo y que impacto tuvieron sobre el baseline del lenguaje._
| Fecha | Tipo | Cambio | Impacto |
|---|---|---|---|
| 2026-03-11 | Baseline | Se consolida vocabulario canonico para comandos, eventos, errores y estados. | Base comun semantica para todo el pilar de dominio. |
| 2026-03-11 | Mejora de estructura | Se organiza el archivo en orden abstracto->concreto y se agrega trazabilidad semantica a RF/RNF. | Cierre de coherencia entre lenguaje, contexto y cobertura de requisitos. |
| 2026-03-13 | Precision semantica | Se aclara `tenant != organizacion`, se agregan verbos de dominio y terminos no intercambiables. | Menor ambiguedad al pasar de semantica a comportamiento e integracion. |
