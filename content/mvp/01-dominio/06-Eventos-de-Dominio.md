---
title: "Eventos de Dominio"
linkTitle: "6. Eventos de Dominio"
weight: 6
url: "/mvp/dominio/eventos-dominio/"
---

## Marco de eventos
_Responde: para que existe este catalogo, que cubre dentro del dominio y de donde obtiene su autoridad semantica._

Catalogar eventos de dominio de ArkaB2B con semantica estable para
integraciones entre bounded contexts en `MVP`.

- RF/RNF aprobados en `/mvp/producto/catalogo-rf/` y `/mvp/producto/catalogo-rnf/`.
- Fronteras y relaciones entre BCs en `/mvp/dominio/mapa-contexto/`.
- Vocabulario canonico del dominio en `/mvp/dominio/lenguaje-ubicuo/`.
- Procesos, ownership y transiciones globales en `/mvp/dominio/comportamiento-global/`.
- Reglas e invariantes de negocio en `/mvp/dominio/reglas-invariantes/`.

## Gobierno del catalogo
_Responde: que principios rigen el uso de eventos y como se clasifica el catalogo para mantener significado estable entre productores y consumidores._
- Nombre canonico de evento: `UpperCamelCase` en ingles.
- Topic canonico: `<bc>.<event-name>.v<major>`.
- Un evento describe un hecho ocurrido; nunca una intencion.
- Todos los eventos criticos incluyen `eventId`, `eventType`, `eventVersion`,
  `occurredAt`, `traceId`, `tenantId`, `correlationId`.
- Entrega esperada: `at-least-once`; consumidores deben ser idempotentes.

Taxonomia de eventos:
| Tipo | Descripcion | Ejemplos |
|---|---|---|
| Core transaccional | hechos de negocio que cambian estado core | `OrderCreated`, `OrderConfirmed`, `StockReserved`, `OrderPaymentRegistered` |
| Integracion operativa | hechos que sincronizan BCs sin mutar el core origen | `CheckoutAddressValidated`, `PrimaryContactChanged`, `UserBlocked` |
| Operacion/soporte | hechos para notificacion o seguimiento operativo | `NotificationRequested`, `NotificationFailed`, `LowStockDetected` |
| Analitico | hechos derivados para consolidacion y reportes | `AnalyticFactUpdated`, `WeeklyReportGenerated` |
| Regionalizacion | hechos asociados a configuracion por pais | `CountryOperationalPolicyConfigured`, `WeeklyReportGenerated` |

## Catalogo de eventos
_Responde: que eventos existen, quien los produce, quien los consume y como se distribuyen por bounded context._
Catalogo global de eventos:
| Evento | Productor | Consumidores principales | FR/NFR asociados | Estado |
|---|---|---|---|---|
| `UserLoggedIn` | `identity-access` | `reporting`, `security-monitoring` | FR-009, NFR-005 | Activo |
| `AuthFailed` | `identity-access` | `security-monitoring` | FR-009, NFR-005 | Activo |
| `SessionRevoked` | `identity-access` | `api-gateway-service`, `reporting` | FR-009, NFR-005 | Activo |
| `SessionsRevokedByUser` | `identity-access` | `api-gateway-service`, `security-monitoring` | FR-009, NFR-005 | Activo |
| `RoleAssigned` | `identity-access` | `directory`, `order`, `reporting` | FR-009, NFR-005 | Activo |
| `UserBlocked` | `identity-access` | `order`, `directory`, `notification`, `reporting`, `security-monitoring` | FR-009, NFR-005 | Activo |
| `OrganizationRegistered` | `directory` | `order`, `reporting` | FR-009, NFR-006 | Activo |
| `OrganizationProfileUpdated` | `directory` | `reporting`, `notification` | FR-009, NFR-006 | Activo |
| `CountryOperationalPolicyConfigured` | `directory` | `reporting` | FR-011, NFR-011 | Activo |
| `OrganizationLegalDataUpdated` | `directory` | `reporting` | FR-009, NFR-006 | Activo |
| `OrganizationActivated` | `directory` | `identity-access`, `order` | FR-009, NFR-005 | Activo |
| `OrganizationSuspended` | `directory` | `identity-access`, `order`, `notification` | FR-009, NFR-005 | Activo |
| `AddressRegistered` | `directory` | `order`, `reporting` | FR-004, FR-005 | Activo |
| `AddressUpdated` | `directory` | `order`, `reporting` | FR-004, FR-005 | Activo |
| `AddressDeactivated` | `directory` | `order`, `notification` | FR-004, FR-005 | Activo |
| `AddressDefaultChanged` | `directory` | `order` | FR-004, FR-005 | Activo |
| `ContactRegistered` | `directory` | `notification`, `reporting` | FR-006 | Activo |
| `ContactUpdated` | `directory` | `notification`, `reporting` | FR-006 | Activo |
| `ContactDeactivated` | `directory` | `notification` | FR-006 | Activo |
| `PrimaryContactChanged` | `directory` | `notification`, `reporting` | FR-006 | Activo |
| `CheckoutAddressValidated` | `directory` | `order`, `reporting` | FR-004 | Activo |
| `ProductCreated` | `catalog` | sin consumidores externos activos en `MVP` | FR-001, FR-007 | Activo |
| `ProductUpdated` | `catalog` | `reporting` | FR-001, FR-007 | Activo |
| `ProductActivated` | `catalog` | sin consumidores externos activos en `MVP` | FR-001, FR-004 | Activo |
| `ProductRetired` | `catalog` | `inventory`, `notification` | FR-001, FR-004 | Activo |
| `VariantCreated` | `catalog` | `inventory` | FR-001, FR-004 | Activo |
| `VariantUpdated` | `catalog` | `inventory` | FR-001, FR-007 | Activo |
| `VariantSellabilityChanged` | `catalog` | sin consumidores externos activos en `MVP` | FR-001, FR-004 | Activo |
| `VariantDiscontinued` | `catalog` | `order`, `inventory`, `notification` | FR-001, FR-004 | Activo |
| `PriceUpdated` | `catalog` | `order`, `reporting` | FR-001, FR-004, FR-007 | Activo |
| `PriceScheduled` | `catalog` | `reporting` | FR-001, FR-007 | Activo |
| `StockInitialized` | `inventory` | `reporting` | FR-002, NFR-004 | Activo |
| `StockAdjusted` | `inventory` | `reporting` | FR-002, NFR-004 | Activo |
| `StockIncreased` | `inventory` | `reporting` | FR-002, NFR-004 | Activo |
| `StockDecreased` | `inventory` | `reporting` | FR-002, NFR-004 | Activo |
| `StockUpdated` | `inventory` | `order`, `reporting`, `catalog` | FR-002, FR-003, NFR-004 | Activo |
| `StockReserved` | `inventory` | `order`, `reporting` | FR-004, FR-005, NFR-004 | Activo |
| `StockReservationExtended` | `inventory` | `order` | FR-005, NFR-004 | Activo |
| `StockReservationConfirmed` | `inventory` | `order`, `reporting` | FR-004, NFR-004 | Activo |
| `StockReservationReleased` | `inventory` | `order`, `reporting` | FR-005, NFR-004 | Activo |
| `StockReservationExpired` | `inventory` | `order`, `notification`, `reporting` | FR-004, FR-008, NFR-004 | Activo |
| `SkuReconciled` | `inventory` | `catalog`, `reporting` | FR-002, FR-003 | Activo |
| `LowStockDetected` | `inventory` | `notification`, `reporting` | FR-003 | Activo |
| `CartItemAdded` | `order` | `reporting` | FR-004, NFR-006 | Activo |
| `CartItemUpdated` | `order` | `reporting` | FR-004, FR-005 | Activo |
| `CartItemRemoved` | `order` | `reporting` | FR-005 | Activo |
| `CartCleared` | `order` | `reporting` | FR-005 | Activo |
| `OrderCheckoutValidationFailed` | `order` | `reporting` | FR-004 | Activo |
| `OrderCreated` | `order` | `reporting` | FR-004 | Activo |
| `OrderConfirmed` | `order` | `notification`, `reporting` | FR-006, FR-007 | Activo |
| `OrderStatusChanged` | `order` | `notification`, `reporting` | FR-006 | Activo |
| `OrderCancelled` | `order` | `notification`, `reporting` | FR-005, NFR-006 | Activo |
| `OrderPaymentRegistered` | `order` | `notification`, `reporting` | FR-010, NFR-006 | Activo |
| `OrderPaymentStatusUpdated` | `order` | `notification`, `reporting` | FR-010, NFR-006 | Activo |
| `CartAbandonedDetected` | `order` | `notification`, `reporting` | FR-008 | Activo |
| `NotificationRequested` | `notification` | `reporting` | FR-006, FR-008 | Activo |
| `NotificationSent` | `notification` | `reporting` | FR-006, FR-008 | Activo |
| `NotificationFailed` | `notification` | `reporting`, `order` | FR-006, NFR-007 | Activo |
| `NotificationDiscarded` | `notification` | `reporting` | FR-006, NFR-007 | Activo |
| `AnalyticFactUpdated` | `reporting` | consumo interno de reporting | FR-003, FR-007 | Activo |
| `WeeklyReportGenerated` | `reporting` | `notification` | FR-003, FR-007, FR-011, NFR-011 | Activo |

Vista resumida por bounded context:
| BC productor | Eventos representativos | Objetivo semantico principal |
|---|---|---|
| `identity-access` | `UserLoggedIn`, `SessionRevoked`, `RoleAssigned`, `UserBlocked` | control de acceso, sesion y autorizacion por tenant |
| `directory` | `OrganizationProfileUpdated`, `ContactUpdated`, `CountryOperationalPolicyConfigured`, `CheckoutAddressValidated` | identidad organizacional, contacto institucional, direccion operativa valida y politica regional |
| `catalog` | `ProductCreated`, `VariantSellabilityChanged`, `PriceUpdated` | estado comercial de producto/SKU/precio |
| `inventory` | `StockUpdated`, `StockReserved`, `StockReservationExpired`, `LowStockDetected` | disponibilidad real y ciclo de reserva |
| `order` | `OrderCreated`, `OrderConfirmed`, `OrderStatusChanged`, `OrderPaymentRegistered`, `CartAbandonedDetected` | ciclo de compra, confirmacion comercial, estado y pago |
| `notification` | `NotificationRequested`, `NotificationSent`, `NotificationFailed` | envio no bloqueante y resultado de entrega |
| `reporting` | `AnalyticFactUpdated`, `WeeklyReportGenerated` | consolidacion analitica y reporte semanal |

## Contrato de publicacion
_Responde: que estructura minima, garantias de idempotencia y metadatos de correlacion debe cumplir todo evento para publicarse y consumirse de forma segura._
Envelope minimo:
```json
{
  "eventId": "evt_...",
  "eventType": "OrderCreated",
  "eventVersion": "1.0.0",
  "occurredAt": "2026-03-03T20:10:00Z",
  "producer": "order-service",
  "tenantId": "org-co-001",
  "traceId": "trc_...",
  "correlationId": "chk_...",
  "payload": {}
}
```

- `MUST`: consumidores deduplican por `eventId` y clave de agregado (`orderId`, `reservationId`, `sku`, etc.).
- `MUST`: orden garantizado solo por particion/clave de agregado, no global.
- `SHOULD`: side effects externos (`notification`, `reporting`) deben ser reentrantes.
- `MUST`: `order`, `inventory`, `identity-access` y `catalog` mantienen `processed_event` para eventos consumidos cuando materializan side effects o recomputan estado interno por consumo asincrono.
- `MUST`: publicar `correlationId` y `traceId` en todo evento core para reconstruccion de flujo end-to-end.

## Uso operativo de eventos
_Responde: para que se usa cada evento en el flujo, bajo que precondiciones se publica y que riesgos deben mitigarse en su consumo._
Matriz de consumo por evento:
| Evento | Consumidores clave | Decision/accion de negocio habilitada |
|---|---|---|
| `StockReservationExpired` | `order`, `notification`, `reporting` | invalidar checkout, notificar y ajustar metricas de abandono |
| `OrderCreated` | `reporting` | trazar inicio del ciclo comercial y consolidar el pedido en `PENDING_APPROVAL` para analitica |
| `OrderConfirmed` | `notification`, `reporting` | confirmar comercialmente pedido y habilitar comunicacion/venta consolidada |
| `OrderPaymentRegistered` | `notification`, `reporting` | recalcular estado de pago y comunicar confirmacion |
| `UserBlocked` | `order`, `directory`, `notification`, `reporting` | detener mutaciones, inactivar perfiles de usuario organizacionales locales y alertar operacion |
| `LowStockDetected` | `notification`, `reporting` | activar abastecimiento y seguimiento operativo |
| `WeeklyReportGenerated` | `notification` | distribuir reporte semanal por tenant/pais |

Precondiciones y postcondiciones de publicacion:
| Evento/familia | Precondiciones | Postcondiciones |
|---|---|---|
| `OrderCreated` | checkout valido, direccion valida, reservas confirmables | pedido persistido en `PENDING_APPROVAL` y evento emitido una sola vez por correlacion |
| `OrderConfirmed` | pedido en `PENDING_APPROVAL`, transicion permitida y actor autorizado | pedido pasa a `CONFIRMED` y se publica hecho comercial final |
| `OrderPaymentRegistered` | pedido existente, referencia de pago no aplicada, monto valido | pago aplicado e inicio de recalculo de estado |
| `StockReserved` / `StockReservationConfirmed` | disponibilidad suficiente y TTL activo | reserva registrada/confirmada sin violar `stock_fisico` |
| `StockReservationExpired` | reserva activa excede TTL | reserva expirada y consumidores informados para compensacion |
| `NotificationRequested` | hecho de negocio publicable y canal permitido | solicitud no bloqueante registrada para entrega |
| `WeeklyReportGenerated` | corte semanal ejecutado con parametros de pais vigentes | reporte consolidado disponible para distribucion |

Riesgos semanticos y mitigacion:
| Riesgo | Impacto | Mitigacion |
|---|---|---|
| consumidores sin idempotencia | side effects duplicados | `processed_event` + claves de dedupe por agregado |
| perdida temporal de publicacion | drift en `notification`/`reporting` | outbox + replay por rango |
| reinterpretacion de eventos | comportamiento inconsistente | semantica explicita en dossiers `08-bounded-contexts/*/04-eventos.md` + ACLs |
| backlog de eventos | latencia operativa | monitoreo de backlog y DLQ por topic |

## Compatibilidad del catalogo
_Responde: como pueden evolucionar los eventos sin romper consumidores activos ni degradar la semantica compartida._
- Cambio compatible: agregar campo opcional o nuevo evento no obligatorio para consumidores actuales.
- Cambio breaking: remover/cambiar significado de campo o evento critico (`OrderCreated`, `OrderConfirmed`, `StockReservationExpired`, `StockUpdated`, `UserBlocked`).
- Para cambio breaking:
  - crear `v2`.
  - mantener convivencia minima de 1-2 releases funcionales.
  - documentar mapa `v1 -> v2` por BC productor.
- Politica de deprecacion:
  1. anunciar evento deprecado y fecha de retiro.
  2. publicar reemplazo semantico.
  3. validar consumidores migrados.
  4. retirar solo con evidencia de cero consumidores activos.

## Trazabilidad de eventos
_Responde: que eventos cubren cada requisito del producto y desde que bounded contexts se aporta esa evidencia semantica._
| Eventos | BC productor(es) | RF/RNF relacionados |
|---|---|---|
| Acceso y sesion (`UserLoggedIn`, `AuthFailed`, `SessionRevoked`, `SessionsRevokedByUser`, `RoleAssigned`, `UserBlocked`) | `identity-access` | FR-009, NFR-005 |
| Directorio (`Address*`, `Contact*`, `Organization*`, `CountryOperationalPolicyConfigured`, `CheckoutAddressValidated`) | `directory` | FR-004, FR-005, FR-006, FR-009, FR-011, NFR-011 |
| Catalogo y precio (`Product*`, `Variant*`, `Price*`) | `catalog` | FR-001, FR-004, FR-007 |
| Stock y reservas (`Stock*`, `LowStockDetected`) | `inventory` | FR-002, FR-003, FR-004, FR-005, FR-008, NFR-004 |
| Pedido y pago (`Order*`, `Cart*`) | `order` | FR-004, FR-005, FR-006, FR-008, FR-010, NFR-006 |
| Notificacion (`Notification*`) | `notification` | FR-006, FR-008, FR-010, NFR-007 |
| Analitica y reporte (`AnalyticFactUpdated`, `WeeklyReportGenerated`) | `reporting` | FR-003, FR-007, FR-011, NFR-002, NFR-011 |

## Historial del catalogo
_Responde: que decisiones relevantes cambiaron este catalogo y que impacto tuvieron sobre la integracion semantica del dominio._
| Fecha | Tipo | Cambio | Impacto |
|---|---|---|---|
| 2026-03-11 | Baseline | Se consolida catalogo global de eventos por BC con cobertura RF/RNF. | Base unica para integracion semantica en `MVP`. |
| 2026-03-11 | Mejora de estructura | Se organiza el archivo en orden abstracto->concreto y se agregan matriz de consumo, pre/post y trazabilidad resumida. | Cierre de coherencia entre eventos, BCs y requisitos. |
| 2026-03-13 | Reorganizacion editorial | El archivo se reposiciona despues de `Comportamiento Global` y se explicita la fuente de procesos/ownership. | Los eventos quedan como detalle de hechos, no como sustituto del flujo global. |
