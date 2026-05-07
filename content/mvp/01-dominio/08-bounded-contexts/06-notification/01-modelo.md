---
title: "Modelo"
linkTitle: "2. Modelo"
weight: 2
url: "/mvp/dominio/contextos-delimitados/notificaciones/modelo/"
---

## Marco del modelo
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Modelo conceptual del BC `notification`.

## Entidades principales
_Responde: que entidades estructuran el modelo local._
- SolicitudNotificacion.
- IntentoNotificacion.
- PlantillaNotificacion.
- PoliticaCanal.

## Value objects principales
_Responde: que objetos de valor expresan reglas relevantes sin identidad propia._
- `NotificationKey`.
- `ChannelRef`.
- `DeliveryContext`.
- `RetryPolicyRef`.

## Estados importantes
_Responde: que estados son relevantes para entender el ciclo local._
| Entidad | Estados permitidos | Inicial | Terminales |
|---|---|---|---|
| SolicitudNotificacion | `PENDING`, `SENT`, `FAILED`, `DISCARDED` | `PENDING` | `SENT`, `FAILED`, `DISCARDED` |
| IntentoNotificacion | `CREATED`, `SENT`, `FAILED` | `CREATED` | `SENT`, `FAILED` |

## Reglas de negocio nucleo
_Responde: que reglas de negocio sostienen el modelo del contexto._
- Falla de notificacion no revierte pedido/pago/stock.
- Solicitud se deduplica por clave de negocio (`eventId` + canal + destinatario).
- Numero maximo de reintentos configurable por tipo de evento.

## Identidad de agregados
_Responde: como se identifica cada agregado relevante del contexto._
- `NotificationAggregate(notificationId, sourceEventType, eventVersion, tenantId, channel, status, attempts[])`.
