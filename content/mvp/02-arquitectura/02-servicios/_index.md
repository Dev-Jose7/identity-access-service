---
title: "Servicios"
linkTitle: "2. Servicios"
weight: 2
url: "/mvp/arquitectura/servicios/"
---

## Marco de servicios
_Responde: que organiza esta capa de detalle y como se relaciona cada servicio con los bounded contexts del dominio._

Cada dossier de servicio aterriza el bounded context correspondiente en
arquitectura interna, contratos, datos, rendimiento y seguridad.

## Convenciones del dossier
_Responde: como se documenta cada servicio sin duplicar autoridad ni arrastrar ruido editorial._

- producto sigue siendo la fuente de necesidad y prioridad del servicio;
- dominio sigue siendo la fuente de semantica, ownership e invariantes;
- arquitectura documenta solo la realizacion tecnica local del servicio;
- `legacy` no se trata como evidencia activa dentro de los dossiers;
- las decisiones abiertas se centralizan en `arc42/09-Decisiones-Arquitectonicas`;
- los artefactos locales no repiten secciones boilerplate de entradas, checklist o preguntas abiertas cuando no aportan evidencia nueva.

## Convencion transversal de autenticacion y autorizacion
_Responde: como se separan autenticacion, autorizacion contextual y legitimidad del trigger entre plataforma y servicios._

- `identity-access-service` autentica al usuario y emite JWT.
- `api-gateway-service` autentica requests HTTP y solo enruta llamadas con token confiable.
- cada servicio HTTP materializa `PrincipalContext`, valida permiso base con `PermissionEvaluatorPort`, aplica `TenantIsolationPolicy` y cierra ownership/reglas del dominio; el gateway no reemplaza esa decision.
- los listeners, schedulers y callbacks internos materializan `TriggerContext`, validan `tenant`, caller tecnico, dedupe y legitimidad del trigger sin asumir JWT de usuario.


## Convencion transversal de errores y excepciones
_Responde: como se clasifican y cierran los errores sin mezclar reglas de dominio con fallos tecnicos._

- `domain` expresa solo errores semanticos: invariantes, aislamiento por `tenant`, autorizacion contextual, conflictos y ausencia de recurso.
- `application` traduce idempotencia, fallos de orquestacion y dependencias previas a la decision sin exponer errores crudos del proveedor.
- `infrastructure` encapsula fallos tecnicos en familias retryable/no-retryable y los entrega al borde correspondiente.
- `adapter-in HTTP` mapea el error a un envelope canonico con `errorCode`, `category`, `traceId`, `correlationId` y `timestamp`.
- `adapter-in async` trata duplicados como `noop idempotente` y usa retry o DLQ segun la naturaleza del fallo.

## Catalogo de servicios
_Responde: que servicios componen el baseline tecnico del MVP._
- [Servicio de Identidad y Acceso](/mvp/arquitectura/servicios/servicio-identidad-acceso/)
- [Servicio de Directorio](/mvp/arquitectura/servicios/servicio-directorio/)
- [Servicio de Catalogo](/mvp/arquitectura/servicios/servicio-catalogo/)
- [Servicio de Inventario](/mvp/arquitectura/servicios/servicio-inventario/)
- [Servicio de Pedidos](/mvp/arquitectura/servicios/servicio-pedidos/)
- [Servicio de Notificaciones](/mvp/arquitectura/servicios/servicio-notificaciones/)
- [Servicio de Reporteria](/mvp/arquitectura/servicios/servicio-reporteria/)
