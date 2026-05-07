---
title: "Agregados"
linkTitle: "3. Agregados"
weight: 3
url: "/mvp/dominio/contextos-delimitados/notificaciones/agregados/"
---

## Marco de agregados
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir agregados e invariantes de `notification`.

## Agregados
_Responde: que agregados protegen consistencia dentro del contexto._

### NotificationAggregate
_Esta subseccion detalla notificationaggregate dentro del contexto del documento._
- Proposito: garantizar envio no bloqueante y trazable de notificaciones.
- Invariantes:
  - solicitud creada inicia en `PENDING`.
  - `PENDING -> SENT|FAILED|DISCARDED`.
  - `SENT` no retorna a `PENDING`.
  - dedupe por `notificationKey` evita envio duplicado.
- Errores:
  - `canal_no_disponible`, `destinatario_invalido`, `conflicto_idempotencia`.

### NotificationAttemptAggregate
_Esta subseccion detalla notificationattemptaggregate dentro del contexto del documento._
- Proposito: trazar intentos y backoff.
- Invariantes:
  - intento se asocia a una solicitud existente.
  - reintento maximo por politica.
- Errores:
  - `maximo_reintentos_excedido`.

## Reglas de consistencia
_Responde: que invariantes locales debe preservar este artefacto._
- estado `FAILED` exige registro de `errorCode` y `attemptCount`.
- `NotificationFailed` se emite sin side effects en core.
