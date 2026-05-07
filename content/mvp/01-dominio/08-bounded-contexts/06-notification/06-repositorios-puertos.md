---
title: "Repositorios y Puertos"
linkTitle: "7. Repositorios y Puertos"
weight: 7
url: "/mvp/dominio/contextos-delimitados/notificaciones/repositorios-puertos/"
---

## Marco de puertos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir puertos de entrada/salida y contratos de persistencia/integracion de `notification`.

## Puertos de entrada (in)
_Responde: que puertos de entrada exponen casos de uso o capacidades del contexto._
| Port | Tipo | Metodos clave |
|---|---|---|
| `NotificationUseCasePort` | in | `solicitarNotificacion`, `ejecutarEnvio`, `reintentarNotificacion`, `descartarNotificacion` |
| `NotificationQueryPort` | in | `obtenerSolicitud`, `listarSolicitudesPendientes` |

## Puertos de salida (out)
_Responde: que puertos de salida necesita el contexto para colaborar con otros sistemas._
| Port | Tipo | Metodos clave |
|---|---|---|
| `NotificationRepositoryPort` | out | `findById`, `findByNotificationKey`, `save`, `listPending` |
| `NotificationAttemptRepositoryPort` | out | `saveAttempt`, `listByNotificationId` |
| `RecipientResolverPort` | out | `resolveRecipient` |
| `ChannelProviderPort` | out | `send` |
| `NotificationAuditPort` | out | `record` |
| `OutboxPort` | out | `append`, `listPending`, `markPublished` |

## Contratos de consistencia
_Responde: que contratos preservan consistencia entre puertos, repositorios y reglas locales._
- `solicitarNotificacion` deduplica por `notificationKey` antes de crear.
- `ejecutarEnvio` registra intento y actualiza estado en la misma unidad de negocio.
- todo cambio de estado publica evento en outbox.
