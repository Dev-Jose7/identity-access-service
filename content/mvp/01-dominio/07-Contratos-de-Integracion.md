---
title: "Contratos de Integracion"
linkTitle: "7. Contratos de Integracion"
weight: 7
url: "/mvp/dominio/contratos-integracion/"
---

## Marco de integracion
_Responde: para que existe este documento, que regula dentro de la colaboracion entre bounded contexts y de donde obtiene su autoridad semantica._

Definir contratos de integracion semantica entre bounded contexts para
operaciones sync/async, garantias, errores de negocio, idempotencia y
compatibilidad de `MVP`.

- RF/RNF aprobados en `/mvp/producto/catalogo-rf/` y `/mvp/producto/catalogo-rnf/`.
- Fronteras y relaciones en `/mvp/dominio/mapa-contexto/`.
- Lenguaje canonico en `/mvp/dominio/lenguaje-ubicuo/`.
- Procesos, ownership e idempotencia global en `/mvp/dominio/comportamiento-global/`.
- Eventos de dominio en `/mvp/dominio/eventos-dominio/`.
- Reglas e invariantes en `/mvp/dominio/reglas-invariantes/`.

## Gobierno de integracion
_Responde: que principios gobiernan las integraciones, cuando deben ser sincronas o asincronas y que limites de confianza deben respetarse entre bounded contexts._
- `MUST`: cada contrato expresa intencion o hecho de negocio con semantica estable.
- `MUST`: en HTTP protegido, `api-gateway-service` autentica el request y el BC destino valida `tenant`, permiso, ownership y legitimidad de negocio antes de ejecutar la mutacion.
- `MUST`: un BC no filtra su modelo interno; publica contrato estable y versionado.
- `MUST`: consumidores toleran `at-least-once` con idempotencia obligatoria.
- `SHOULD`: privilegio de acoplamiento minimo con ACLs en traducciones semanticas.
- `SHOULD`: fallas de canales de soporte (`notification`, `reporting`) no bloquean core.
- `MUST`: validacion de direccion/pais, vendible/precio y reservas ocurre en sincronico durante mutaciones criticas de compra.
- `MUST`: la autenticacion de requests HTTP protegidos se resuelve en borde y la autorizacion contextual se cierra dentro del BC destino.
- `MUST`: `notification` y `reporting` reaccionan por eventos y no participan en la transaccion de `order` o `inventory`.
- `MUST`: la consistencia fuerte se limita al boundary local del BC y a validaciones sync previas a la mutacion.
- `SHOULD`: la convergencia cross-BC ocurre por eventos idempotentes y replay/rebuild controlado.
- `SHOULD`: consultas sincronas a `identity-access` para introspeccion se reservan a fallback, alta sensibilidad o incertidumbre del estado de sesion.
- `MUST`: no existe fallback global silencioso para omitir configuracion regional por pais.

Limites de confianza entre bounded contexts:
| Integracion | Control obligatorio | Limite de confianza |
|---|---|---|
| `api-gateway-service -> BC HTTP protegido` | JWT valido + claims confiables + snapshot corto de revocacion | el gateway autentica; el BC destino no delega su autorizacion contextual |
| `servicios core <-> identity-access` | claims confiables + introspeccion fallback + eventos IAM | `identity-access` mantiene la verdad de sesion/rol; el BC consumidor no reinterpreta credenciales ni modelo interno |
| `order <-> directory` | pertenencia de direccion y configuracion por pais vigente | `directory` decide verdad de organizacion/direccion/pais |
| `order <-> catalog` | variante vendible y precio vigente | `catalog` decide semantica comercial |
| `order <-> inventory` | reserva/confirmacion con disponibilidad real | `inventory` decide disponibilidad; `order` no calcula stock |
| Async hacia `notification`/`reporting` | no mutar core, solo proyecciones o comunicaciones | BCs de soporte no ejecutan decisiones transaccionales core |

## Mapa de integraciones
_Responde: que integraciones existen, bajo que patron se relacionan los bounded contexts y cual es el flujo dominante de cada contrato._
Patrones de interaccion entre bounded contexts:
| Integracion | Patron | Tipo | Flujo dominante | Garantia |
|---|---|---|---|---|
| `api-gateway-service -> servicios HTTP protegidos` | Open Host de plataforma | Sync edge | request protegido | autenticacion JWT previa al BC |
| `servicios core <-> identity-access` | Customer/Supplier de soporte | Sync query fallback + Async eventos | contexto de seguridad incierto o cambio de estado IAM | identidad de borde confiable + verdad de sesion/rol bajo demanda |
| `order <-> directory` | Customer/Supplier + ACL | Sync API + Async fallback | validacion de direccion y parametros por pais | direccion/politica vigentes por tenant |
| `order <-> catalog` | Conformist + snapshot | Sync API | upsert de carrito y checkout | variante vendible + precio vigente |
| `order <-> inventory` | Customer/Supplier + ACL | Sync API + Async eventos | reserva/confirmacion/liberacion de stock + validacion sync de reservas de checkout | sin reserva valida no hay pedido |
| `order -> notification` | Open Host | Async eventos | pedido confirmado/pago/carrito abandonado | envio no bloqueante |
| `order -> reporting` | Conformist pasivo | Async eventos | pedido confirmado/pago/cambios de estado | consolidacion eventual |
| `notification -> reporting` | Conformist pasivo | Async eventos | solicitudes/intentos/resultado de entrega | consolidacion de efectividad |
| `inventory -> reporting` | Conformist pasivo | Async eventos | stock/reserva/bajo stock | consolidacion de abastecimiento |
| `catalog -> reporting` | Conformist pasivo | Async eventos | producto/variante/precio | analitica comercial |
| `directory -> reporting` | Conformist pasivo | Async eventos | cambios organizacionales/contactos institucionales/configuracion por pais | contexto regional en reportes |
| `identity-access -> order/directory/notification/reporting` | Open Host | Async eventos | sesion revocada, rol asignado, usuario bloqueado | ajuste de permisos, perfiles organizacionales locales, bloqueo de mutaciones y trazabilidad operativa |

Catalogo operativo de integraciones:
| Integracion | Tipo | Trigger | Contrato semantico |
|---|---|---|---|
| `api-gateway-service -> servicios HTTP protegidos` | Sync edge | request protegido | autenticar JWT y propagar claims confiables al BC destino |
| `servicios core <-> identity-access` | Sync query fallback + Async eventos | request protegido con estado incierto o cambio IAM | usar claims confiables, eventos IAM e introspeccion solo cuando haga falta |
| `order <-> directory` | Sync API + Async fallback | validacion de direccion de checkout y resolucion de politica por pais | direccion valida y parametros de pais vigentes para el tenant |
| `order <-> catalog` | Sync API | upsert de carrito y checkout | resolver variante vendible + precio vigente |
| `order <-> inventory` | Sync API + Async eventos | reserva/confirmacion/liberacion de stock + validacion sync de reservas de checkout | no confirmar pedido sin reservas vigentes |
| `order -> notification` | Async eventos | pedido confirmado/pago/carrito abandonado | envio no bloqueante de comunicaciones |
| `order -> reporting` | Async eventos | pedido confirmado/pago/cambios de estado | consolidacion eventual de ventas |
| `notification -> reporting` | Async eventos | solicitudes/intentos/resultado de entrega | consolidacion de efectividad de notificacion y descarte |
| `inventory -> reporting` | Async eventos | stock/reserva/bajo stock | consolidacion eventual de abastecimiento |
| `catalog -> reporting` | Async eventos | producto/variante/precio | catalogo comercial para analitica |
| `directory -> reporting` | Async eventos | cambios organizacionales/contactos institucionales/configuracion por pais | contexto de cliente y politicas regionales en reportes |
| `identity-access -> order/directory/notification/reporting` | Async eventos | usuario bloqueado, sesion revocada, rol asignado | ajustar permisos, perfiles organizacionales locales, bloquear mutaciones y mantener trazabilidad operativa |

Definiciones operativas y ejemplos: [Conceptos](/mvp/dominio/conceptos/).

## Especificacion de contratos
_Responde: que operaciones y hechos expone cada integracion, con que estructura minima se intercambian y que invariantes del dominio protege cada contrato._
Operaciones sync:
| Operacion conceptual | Precondicion | Postcondicion | Error canonico |
|---|---|---|---|
| autenticar usuario | credenciales validas + usuario habilitado | sesion activa | `usuario_no_habilitado` |
| gestionar carrito | sesion valida + variante vendible + qty > 0 | carrito con reserva valida | `stock_insuficiente` |
| validar checkout | direccion valida + reservas vigentes | checkout validable con snapshot de politica regional | `conflicto_checkout`, `configuracion_pais_no_disponible` |
| confirmar pedido | checkout validado + correlacion vigente + politica regional vigente | pedido creado en `PENDING_APPROVAL` con reservas consumidas y snapshots inmutables | `reserva_expirada`, `configuracion_pais_no_disponible` |
| confirmar aprobacion de pedido | pedido en `PENDING_APPROVAL` + transicion permitida + actor autorizado | pedido en `CONFIRMED` + evento `OrderConfirmed` | `transicion_estado_invalida`, `acceso_cruzado_detectado` |
| registrar pago manual | pedido existente + referencia unica + monto > 0 | estado de pago recalculado | `pago_duplicado`, `monto_pago_invalido` |
| resolver parametros operativos por pais | organizacion existente + `countryCode` soportado + version vigente | retorna moneda, corte semanal y retencion aplicable sin fallback global implicito | `configuracion_pais_no_disponible` |

Eventos async:
| Evento | Significado estable | No significa |
|---|---|---|
| `OrderCreated` | pedido creado tras checkout valido en `PENDING_APPROVAL` con reservas consumidas | pedido confirmado final o pedido entregado |
| `OrderConfirmed` | pedido cambia de `PENDING_APPROVAL` a `CONFIRMED` por decision explicita de `order` | despacho/entrega final o pago conciliado |
| `StockReservationExpired` | reserva invalida por TTL vencido | cancelacion de pedido confirmado |
| `StockUpdated` | snapshot coherente de stock/reservas | autorizacion de venta directa |
| `OrderPaymentRegistered` | pago manual aplicado validamente | conciliacion contable final |
| `NotificationFailed` | envio fallido sin impacto transaccional | fallo de pedido/pago |
| `OrganizationProfileUpdated` | cambios de perfil organizacional no legal disponibles para consumidores | no implica autorizacion automatica ni reemplaza perfiles organizacionales locales |
| `CountryOperationalPolicyConfigured` | cambios versionados de configuracion regional por pais disponibles para consumidores | no reemplaza la resolucion sync obligatoria de politica regional en runtime |

Especificacion minima de mensajes:
- Comandos mutantes:
  - `commandId` o `Idempotency-Key`
  - `tenantId`, `actorId`, `traceId`, `correlationId`
  - `payload` de negocio
- Eventos:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`
  - `tenantId`, `traceId`, `correlationId`
  - `payload` de hecho

Envelope logico minimo:
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

Reglas semanticas por contrato:
| Contrato | Invariantes que protege | Efecto semantico esperado |
|---|---|---|
| `order <-> inventory` | I-INV-01, I-INV-02, I-ORD-01 | no confirmar pedido sin reserva valida |
| `order <-> directory` | I-LOC-01, D-DIR-01 | no mutar pedido con direccion/politica invalida |
| `servicios core <-> identity-access` | I-ACC-01, I-ACC-02 | no ejecutar mutaciones con contexto de seguridad invalido ni sesion no confiable |
| `order -> notification` | I-NOTI-01 | fallas de envio no revierten estado core |
| `order -> reporting` / `inventory -> reporting` | I-REP-01 | reporting no muta datos transaccionales |

Nota de aplicacion:
- En flujos HTTP protegidos, la autenticacion tecnica ocurre en `api-gateway-service`.
- Cada BC protegido materializa su contexto local de seguridad y valida `tenant`, permiso, ownership y legitimidad de negocio sin delegar esa decision al gateway.
- `identity-access` se consulta sincronicamente solo en `login`, `refresh`, `logout`, `introspect` o cuando un BC necesita fallback explicito para resolver el estado real de sesion/rol.

Cobertura regional obligatoria:
- `directory` es la fuente de verdad de parametros operativos por pais versionados por organizacion.
- `order` valida en runtime que la organizacion tenga configuracion regional vigente antes de mutaciones criticas.
- `reporting` usa la configuracion regional para corte semanal y retencion aplicable por tenant.
- `notification -> reporting` aporta telemetria por pais/canal para seguimiento operativo.

Regla oficial de mapeo semantico (`configuracion_pais_no_disponible`):
- `404` cuando se consulta directamente el recurso tecnico de configuracion
  regional y no existe version vigente (resolucion sync en `directory`).
- `409` cuando una operacion de negocio (`checkout`, `confirmar_pedido`,
  `consulta/generacion de reportes`) no puede ejecutarse porque la precondicion
  regional no se cumple.
- En ambos casos el error estable es `configuracion_pais_no_disponible`, no hay
  fallback global implicito y el bloqueo debe quedar auditado.

## Garantias operativas
_Responde: como se controlan errores, duplicados, orden, latencia y riesgos para que la integracion conserve consistencia y degradacion aceptable._
Politicas de error:
| Error | Tipo | Tratamiento | Compensacion |
|---|---|---|---|
| `acceso_cruzado_detectado` | terminal | denegar y auditar incidente | no aplica |
| `stock_insuficiente` | recuperable | ajustar qty y reintentar | reintento de reserva con nuevo qty |
| `reserva_expirada` | recuperable | rearmar carrito y revalidar | regenerar reserva antes de confirmar |
| `conflicto_checkout` | recuperable | revalidar direccion/reservas | repetir validacion de checkout |
| `pago_duplicado` | condicionada | idempotente o rechazo sin side effect | respuesta equivalente sin doble aplicacion |
| `notificacion_fallida_no_bloqueante` | recuperable async | retry con backoff, sin rollback | reintentos y/o descarte controlado |
| `configuracion_pais_no_disponible` | terminal | bloquear operacion hasta configurar politica regional | corregir configuracion y reintentar flujo |

Idempotencia, orden y correlacion:
- Entrega de eventos: `at-least-once`.
- Orden:
  - garantizado por clave de agregado (`orderId`, `reservationId`, `sku`, `userId`).
  - no garantizado globalmente entre agregados distintos.
- Duplicados:
  - consumidores aplican dedupe por `eventId` y clave de agregado.
- Correlacion:
  - comandos y eventos `MUST` transportar `traceId` y `correlationId`.

Claves recomendadas por operacion:
| Operacion | Clave recomendada |
|---|---|
| upsert carrito | `tenant:user:sku:intent` |
| confirmar checkout | `tenant:user:checkoutCorrelationId` |
| crear reserva | `tenant:warehouse:cart:sku` |
| confirmar reserva | `tenant:reservationId:orderId` |
| registrar pago manual | `tenant:orderId:paymentReference` |
| cambiar estado pedido | `tenant:orderId:targetStatus:sourceRef` |

Objetivos operativos por contrato:
| Integracion | Timeout objetivo | Retry | Objetivo de disponibilidad |
|---|---|---|---|
| `order -> inventory` (reserva/confirmacion) | 300-500 ms por operacion | max 3 intentos con backoff | 99.9% en horario operativo |
| `order -> directory` (validacion direccion/pais) | 300 ms | max 2 intentos | 99.9% en horario operativo |
| `order -> catalog` (variante/precio) | 300 ms | max 2 intentos | 99.9% en horario operativo |
| `notification` envio | 2 s por intento | hasta 3 intentos | 99.5% en 15 min de ventana |
| `reporting` consumo eventos | asincrono por lote | retry idempotente + DLQ | 99.5% con retraso maximo controlado |

Riesgos de integracion:
| Riesgo | Impacto | Mitigacion |
|---|---|---|
| interpretacion divergente de terminos entre BCs | fallas funcionales | ACLs explicitas y lenguaje canonico unico |
| integracion sync lenta en checkout | mala experiencia y conflictos | validacion previa + timeout/retry acotado |
| backlog de eventos | latencia en notification/reporting | outbox, DLQ, alertas de backlog |
| falta de idempotencia en consumidores | duplicidad de efectos | `processed_event` obligatorio |

## Intercambio de eventos
_Responde: que eventos publica y consume cada bounded context dentro de sus contratos con otros contextos y cual es el limite de reaccion admitido._
Eventos publicados por bounded context:
| BC productor | Eventos publicados para otros BCs | Uso semantico principal |
|---|---|---|
| `identity-access` | `UserLoggedIn`, `SessionRevoked`, `SessionsRevokedByUser`, `RoleAssigned`, `UserBlocked` | habilitar/bloquear mutaciones y ajustar permisos |
| `directory` | `OrganizationProfileUpdated`, `CountryOperationalPolicyConfigured`, `Address*`, `Contact*`, `CheckoutAddressValidated` | sostener checkout, comunicacion institucional, operacion regional y reporting |
| `catalog` | `Product*`, `Variant*`, `Price*` | alimentar compra y analitica comercial |
| `inventory` | `Stock*`, `StockReservation*`, `LowStockDetected` | sostener anti-sobreventa y abastecimiento |
| `order` | `Order*`, `Cart*` | comunicar ciclo comercial a soporte y analitica |
| `notification` | `Notification*` | consolidar efectividad y descarte de comunicaciones |
| `reporting` | `WeeklyReportGenerated` | distribuir cierre semanal derivado |

Eventos consumidos por bounded context:
| BC consumidor | Eventos/familias consumidas | Limite de reaccion |
|---|---|---|
| `order` | `StockReservationExpired`, `StockReservationConfirmed`, `VariantDiscontinued`, `PriceUpdated`, `CheckoutAddressValidated`, `UserBlocked` | ajusta carrito/pedido, pero no redefine verdad de productores |
| `notification` | `OrderConfirmed`, `OrderPaymentRegistered`, `CartAbandonedDetected`, `LowStockDetected`, `OrganizationProfileUpdated`, `ContactRegistered`, `ContactUpdated`, `PrimaryContactChanged` | decide envio y retry; nunca altera core |
| `reporting` | eventos de `order`, `inventory`, `catalog`, `directory`, `notification` | solo proyecta y recalcula vistas |
| `directory` | `RoleAssigned`, `UserBlocked` | ajusta perfiles de usuario organizacionales, no sesiones |
| `inventory` | `VariantCreated`, `VariantUpdated`, `VariantDiscontinued`, `ProductRetired` | reacciona de forma operacional para sincronizar SKU y bloquear operacion sin mover precios/identidad |

## Perimetro externo
_Responde: que integraciones externas existen o quedan explicitamente fuera del baseline semantico de `MVP`._
- Los proveedores de envio (correo, SMS, WhatsApp u otro canal) existen detras de `notification`; el dominio solo conoce `solicitud_notificacion`, intentos y resultado de entrega.
- No hay PSP, ERP ni identidad externa modelados como parte del baseline semantico de `MVP`.
- Si un externo futuro impone otro vocabulario, debe entrar por ACL y nunca contaminar el lenguaje canonico del BC consumidor.

## Compatibilidad de contratos
_Responde: como evolucionan los contratos sin romper consumidores y que reglas de versionado o deprecacion aplican._
- Versionado conceptual de contratos: `v1`, `v2`, ...
- `MUST`: cambio breaking crea nueva version y ventana de convivencia.
- `MUST`: consumidores de version anterior mantienen operacion durante la ventana acordada.
- `SHOULD`: productores soportan campos opcionales para forward compatibility.

Reglas de deprecacion:
1. anunciar contrato/evento deprecado.
2. publicar reemplazo y mapeo semantico.
3. monitorear adopcion por consumidor.
4. retirar con evidencia de migracion completa.

## Trazabilidad de integracion
_Responde: que grupos de contratos soportan requisitos concretos del producto y desde que bounded contexts se aporta esa evidencia de integracion._
| Contrato / Grupo de contratos | BCs principales | RF/RNF relacionados |
|---|---|---|
| acceso y autorizacion (`gateway -> servicios protegidos`, fallback IAM, eventos de sesion/autorizacion) | `identity-access`, `order`, `directory` | FR-009, NFR-005 |
| checkout y pedido (`order <-> directory/catalog/inventory`) | `order`, `directory`, `catalog`, `inventory` | FR-004, FR-005, FR-011, NFR-004, NFR-011 |
| pago manual (`registrar pago`, `OrderPaymentRegistered`) | `order`, `notification`, `reporting` | FR-010, NFR-006, NFR-007 |
| estado y comunicacion (`OrderConfirmed`, `OrderStatusChanged`, `Notification*`) | `order`, `notification`, `reporting` | FR-006, FR-008, NFR-007 |
| analitica y abastecimiento (`inventory/reporting`, `catalog/reporting`) | `inventory`, `catalog`, `reporting` | FR-003, FR-007, NFR-002 |

## Historial de integracion
_Responde: que decisiones relevantes cambiaron este baseline de integracion y que impacto tuvieron sobre la colaboracion entre bounded contexts._
| Fecha | Tipo | Cambio | Impacto |
|---|---|---|---|
| 2026-03-11 | Baseline | Se consolida matriz principal de integraciones y contratos sync/async de `MVP`. | Base comun para interacciones entre BCs. |
| 2026-03-11 | Mejora de estructura | Se organiza en orden abstracto->concreto y se agregan seguridad, SLO/SLA y trazabilidad dedicada. | Cierre de coherencia entre contratos, BCs y RF/RNF. |
| 2026-03-13 | Reorganizacion editorial | Se agregan reglas explicitas de sync/async, eventos publicados/consumidos y estado de externos en `MVP`. | El archivo queda alineado con la capa de integracion semantica del pilar. |
