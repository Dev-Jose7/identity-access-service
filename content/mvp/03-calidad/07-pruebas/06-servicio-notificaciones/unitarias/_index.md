---
title: "Unitarias - servicio-notificaciones"
linkTitle: "1. Unitarias"
weight: 1
url: "/mvp/calidad/pruebas/servicio-notificaciones/unitarias/"
---

## Objetivo
Validar invariantes de solicitud/intento y politicas de canal, retry y descarte sin dependencias externas.

## Cobertura objetivo
- `RN-NOTI-01`, `I-NOTI-01`.
- `I-ACC-02`, `D-CROSS-01` en validacion contextual.
- estados de solicitud: `PENDING`, `SENT`, `FAILED`, `DISCARDED`.

## Fixtures base sugeridos
- `fixture_notification_request_pending_email.yaml`
- `fixture_notification_request_failed_retryable.yaml`
- `fixture_notification_request_failed_non_retryable.yaml`
- `fixture_notification_attempt_created.yaml`
- `fixture_notification_provider_policy_default.yaml`
- `fixture_notification_callback_payload_valid.yaml`

## Matriz detallada de casos unitarios
| ID | Escenario | Given | When | Then | Trazabilidad |
|---|---|---|---|---|---|
| NOTI-UT-001 | crear solicitud valida | evento fuente y destinatario resolubles | registrar solicitud | estado `PENDING` | FR-006 |
| NOTI-UT-002 | dedupe por clave de negocio | mismo `eventId+channel+recipient` | registrar solicitud repetida | no duplica side effect | RN-NOTI-01 |
| NOTI-UT-003 | destinatario invalido se rechaza | recipientRef no resoluble | registrar solicitud | error `destinatario_invalido` | FR-006 |
| NOTI-UT-004 | dispatch exitoso cierra en `SENT` | solicitud `PENDING` + provider ok | despachar | estado `SENT` | FR-006 |
| NOTI-UT-005 | fallo retryable marca `FAILED` | timeout/provider transitorio | despachar | estado `FAILED`, `retryable=true` | FR-006, NFR-007 |
| NOTI-UT-006 | fallo no retryable descarta | error terminal de provider/payload | despachar | estado `DISCARDED` | NFR-007 |
| NOTI-UT-007 | retry permitido bajo maxAttempts | solicitud `FAILED` con attempts < max | ejecutar retry | nuevo intento creado | FR-006 |
| NOTI-UT-008 | retry no permitido por maxAttempts | attempts == maximo | ejecutar retry | error `maximo_reintentos_excedido` + `DISCARDED` | FR-006, NFR-007 |
| NOTI-UT-009 | estado terminal no muta | solicitud `SENT` o `DISCARDED` | retry/discard/dispatch | error `estado_invalido` | I-NOTI-01 |
| NOTI-UT-010 | callback valido reconcilia entrega | providerRef existente + firma valida | procesar callback | actualiza intento/solicitud coherente | FR-006 |
| NOTI-UT-011 | callback duplicado es noop | mismo providerRef+eventId callback | procesar callback repetido | sin doble side effect | NFR-009 |
| NOTI-UT-012 | callback invalido se rechaza | firma/token invalido | procesar callback | error seguridad sin mutacion | NFR-005 |
| NOTI-UT-013 | falla notificacion no afecta core | evento `OrderConfirmed` previo | falla envio | solo estado Notification cambia | RN-NOTI-01, I-NOTI-01 |
| NOTI-UT-014 | comando sin tenant invalido | mutacion sin `tenantId` | validar policy | rechazo por aislamiento | NFR-005, D-CROSS-01 |
| NOTI-UT-015 | acceso cruzado en solicitud | actor tenant A sobre solicitud tenant B | validar ownership | error `acceso_cruzado_detectado` | NFR-005, I-ACC-02 |

## Criterio de exito unitario
- Escenarios `NOTI-UT-001..015` en estado `Disenado` o superior, segun corrida y evidencia.
- Se debe verificar explicitamente el desacople no bloqueante respecto al core.
- Rechazos semanticos sin mutaciones indebidas.
