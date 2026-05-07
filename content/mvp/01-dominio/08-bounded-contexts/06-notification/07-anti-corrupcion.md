---
title: "Anti-Corrupcion"
linkTitle: "8. Anti-Corrupcion"
weight: 8
url: "/mvp/dominio/contextos-delimitados/notificaciones/anti-corrupcion/"
---

## Marco de traduccion
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Documentar ACLs semanticas de `notification` con BCs productores/consumidores.

## Mapeos principales
_Responde: que traducciones principales hace el contexto al cruzar sus fronteras._
| Upstream/Downstream | Termino externo | Termino en notification | Regla |
|---|---|---|---|
| `order -> notification` | `OrderConfirmed` | `solicitud_confirmacion_pedido` | hecho confirmado comercialmente, no intencion |
| `order -> notification` | `CartAbandonedDetected` | `solicitud_recordatorio` | aplica ventana comercial |
| `inventory -> notification` | `LowStockDetected` | `alerta_operativa_stock` | destinatario interno |
| `reporting -> notification` | `WeeklyReportGenerated` | `solicitud_envio_reporte` | distribucion no bloqueante |
| `notification -> reporting` | `NotificationSent/NotificationFailed` | `hecho_comunicacion` | consumo analitico |

## Normalizacion de errores
_Responde: como traduce este contexto errores externos a su lenguaje canonico._
| Error tecnico | Error canonico de dominio |
|---|---|
| provider timeout | `notificacion_fallida_no_bloqueante` |
| invalid recipient | `destinatario_invalido` |
| duplicate send key | `conflicto_idempotencia` |

## Reglas ACL
_Responde: que reglas gobiernan la capa ACL para evitar contaminacion semantica._
- Notification no reinterpreta estados transaccionales de pedido/pago.
- Notification no muta entidades core; solo comunica hechos.
- Reporting consume resultados de envio como hechos derivados.
