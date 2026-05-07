---
title: "Vista de Ejecucion"
linkTitle: "6. Vista de Ejecucion"
weight: 6
url: "/mvp/arquitectura/arc42/vista-ejecucion/"
---

## Proposito
Describir comportamiento runtime de flujos criticos y politicas de resiliencia.

## Escenarios runtime prioritarios
- Login y validacion de sesion.
- Creacion de carrito y reserva de stock.
- Checkout y creacion de pedido en `PENDING_APPROVAL`.
- Confirmacion comercial explicita de pedido.
- Registro de pago manual.
- Notificacion no bloqueante por eventos de negocio.
- Ingestion de hechos y generacion de reportes semanales.

## Modelo runtime de identidad y autorizacion
| Capa | Decision aplicada |
|---|---|
| `identity-access-service` | autentica usuarios, emite JWT y mantiene el estado confiable de sesion/rol que usa el resto del sistema |
| `api-gateway-service` | autentica el request HTTP validando JWT con `RS256`/`JWKS`, `iss`, `aud`, expiracion y cache de revocacion antes de enrutar |
| servicios HTTP | materializan `PrincipalContext` mediante `PrincipalContextPort` y `PrincipalContextAdapter`, validan permiso base con `PermissionEvaluatorPort` y cierran `tenant`, ownership y reglas del dominio en sus propios `use cases`; no delegan la autorizacion contextual al gateway |
| listeners / schedulers / callbacks | no asumen JWT de usuario; materializan `TriggerContext` mediante `TriggerContextResolver` y validan `tenant`, caller tecnico, dedupe y legitimidad del trigger antes de aplicar la decision del dominio |
| introspeccion fallback IAM | solo se usa cuando la cache de revocacion o el riesgo operativo hacen insuficiente la confianza local del gateway |
| eventos de IAM | `SessionRevoked`, `SessionsRevokedByUser`, `SessionRefreshed`, `UserBlocked` y `RoleAssigned` reducen el drift entre caches y estado real |


## Modelo runtime de errores y excepciones
| Momento runtime | Responsable principal | Resultado esperado |
|---|---|---|
| `Rechazo temprano` | `Adapter-in`, `PrincipalContext` / `TriggerContext`, guard funcional base y `TenantIsolationPolicy` | corta el flujo antes de la decision de dominio y entrega error semantico de validacion, autorizacion, `not_found` o dependencia previa |
| `Rechazo de decision` | politicas y agregados del servicio destino | preserva invariantes del dominio y se expresa como `business_rule` o `conflict` sin filtrar detalles internos |
| `Fallo de materializacion` | `use case` + adapters de persistencia/cache/outbox/broker | clasifica el fallo como retryable o no retryable segun la dependencia y cierra HTTP o flujo operativo con el envelope canonico |
| `Fallo de propagacion` | relay outbox, publisher o listener que ya materializo side effects | la recuperacion se gestiona por retry, DLQ y observabilidad, no reescribiendo la decision del dominio |
| `Evento duplicado` | `ProcessedEvent*` + politica de dedupe | se trata como `noop idempotente`; no genera error funcional ni rollback compensatorio |

## Runtime A: checkout resiliente
```mermaid
sequenceDiagram
  participant FE as Cliente B2B
  participant GW as API Gateway
  participant ORD as Order Service
  participant IAM as IdentityAccess
  participant DIR as Directory
  participant CAT as Catalog
  participant INV as Inventory
  participant K as Kafka

  FE->>GW: POST /checkout (Bearer + Idempotency-Key)
  GW->>GW: verificar JWT (`RS256`/`JWKS`) + exp/iss/aud
  GW->>GW: consultar cache de revocacion/rol
  alt cache incierta o riesgo elevado
    GW->>IAM: introspect fallback
    IAM-->>GW: sesion activa + claims efectivos
  end
  GW->>ORD: comando checkout + claims confiables
  ORD->>ORD: validar rol/tenant/ownership
  ORD->>DIR: validar direccion
  DIR-->>ORD: addressSnapshot valido
  ORD->>CAT: resolver precio/sellable
  CAT-->>ORD: priceSnapshot
  ORD->>INV: confirmar reservas
  INV-->>ORD: reservas confirmadas
  ORD->>ORD: crear pedido en PENDING_APPROVAL + outbox
  ORD-->>GW: pedido creado en PENDING_APPROVAL
  ORD->>K: publicar OrderCreated
```

## Runtime B: confirmacion comercial
```mermaid
sequenceDiagram
  participant OP as Operador
  participant GW as API Gateway
  participant ORD as Order Service
  participant IAM as IdentityAccess
  participant K as Kafka

  OP->>GW: POST /orders/{id}/status-transitions (Bearer)
  GW->>GW: verificar JWT (`RS256`/`JWKS`) + revocacion
  alt cache incierta o permiso sensible
    GW->>IAM: introspect fallback
    IAM-->>GW: sesion activa + claims efectivos
  end
  GW->>ORD: confirmar_aprobacion_pedido + claims confiables
  ORD->>ORD: validar actor, tenant, transicion y correlacion
  ORD->>ORD: mover PENDING_APPROVAL -> CONFIRMED + outbox
  ORD-->>GW: pedido confirmado comercialmente
  ORD->>K: publicar OrderConfirmed
```

## Runtime C: pago manual
```mermaid
sequenceDiagram
  participant OP as Operador
  participant GW as API Gateway
  participant ORD as Order Service
  participant IAM as IdentityAccess
  participant K as Kafka

  OP->>GW: POST /orders/{id}/manual-payment (Bearer)
  GW->>GW: verificar JWT (`RS256`/`JWKS`) + revocacion
  alt cache incierta o permiso financiero sensible
    GW->>IAM: introspect fallback
    IAM-->>GW: sesion activa + claims efectivos
  end
  GW->>ORD: registrar_pago_manual + claims confiables
  ORD->>ORD: validar actor, rol, tenant, referencia unica e importe > 0
  ORD->>ORD: actualizar estado de pago + outbox
  ORD-->>GW: pago registrado
  ORD->>K: publicar OrderPaymentRegistered / OrderPaymentStatusUpdated
```

## Runtime D: side effects async
```mermaid
sequenceDiagram
  participant K as Kafka
  participant NOTI as Notification
  participant REP as Reporting

  K->>NOTI: OrderConfirmed
  NOTI->>NOTI: crear solicitud + intento envio
  NOTI->>K: NotificationSent/NotificationFailed

  K->>REP: OrderConfirmed, StockUpdated, NotificationSent
  REP->>REP: dedupe + actualizar proyecciones
  REP->>K: WeeklyReportGenerated
```

## Politicas de resiliencia
| Politica | Valor base | Aplicacion |
|---|---|---|
| Validacion authn en borde | JWT validado en `api-gateway-service` + `JWKS` + cache de revocacion | todas las APIs protegidas |
| Introspeccion fallback | solo por cache incierta, revocacion reciente o operacion sensible | gateway e IAM |
| Timeout sync core-core | 500-1200 ms | Directory, Catalog, Inventory e introspeccion fallback IAM |
| Retry transitorio | max 2 intentos con backoff | errores de red/timeout |
| Circuit breaker | abrir por error rate > 50% ventana corta | dependencias sincronas |
| Idempotencia comando | obligatoria | checkout, pago manual, reservas |
| DLQ eventos | obligatoria | consumidores de notification/reporting |

## Compensaciones
- Checkout fallido antes de confirmacion: no se crea pedido.
- Reserva expirada: rechazo de confirmacion y ajuste de carrito.
- Falla de notificacion: no rollback de pedido/pago.
- Evento invalido: enrutar a DLQ con alerta.

## Riesgos runtime
- Desalineacion temporal entre rol/sesion real y cache local de revocacion.
- Carrera entre expiracion de reserva y confirmacion.
- Retries mal configurados que duplican side effects.
- Backlog de broker durante picos.
