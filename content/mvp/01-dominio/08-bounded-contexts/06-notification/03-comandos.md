---
title: "Comandos"
linkTitle: "4. Comandos"
weight: 4
url: "/mvp/dominio/contextos-delimitados/notificaciones/comandos/"
---

## Marco de comandos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Catalogo de comandos de `notification`.

## Lista de comandos
_Responde: que comandos admite el contexto y con que efecto semantico._

### solicitar_notificacion
_Esta subseccion detalla solicitar_notificacion dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `eventType`, `eventVersion`, `eventId`, `recipientRef`, `channel`, `templateCode`, `payload`, `idempotencyKey`.
- Precondiciones:
  - evento origen valido y destinatario resoluble.
- Postcondiciones:
  - solicitud creada en `PENDING`.
- Errores:
  - `destinatario_invalido`, `conflicto_idempotencia`.

### ejecutar_envio_notificacion
_Esta subseccion detalla ejecutar_envio_notificacion dentro del contexto del documento._
- Input esperado:
  - `notificationId`, `provider`, `attemptNumber`, `idempotencyKey`.
- Precondiciones:
  - solicitud `PENDING`.
- Postcondiciones:
  - estado `SENT` o `FAILED`.
- Errores:
  - `canal_no_disponible`, `provider_timeout`.

### reintentar_notificacion
_Esta subseccion detalla reintentar_notificacion dentro del contexto del documento._
- Input esperado:
  - `notificationId`, `reasonCode`, `attemptNumber`, `idempotencyKey`.
- Precondiciones:
  - solicitud `FAILED` y intentos < maximo.
- Postcondiciones:
  - nuevo intento programado.

### descartar_notificacion
_Esta subseccion detalla descartar_notificacion dentro del contexto del documento._
- Input esperado:
  - `notificationId`, `reasonCode`, `idempotencyKey`.
- Postcondiciones:
  - solicitud en `DISCARDED`.
