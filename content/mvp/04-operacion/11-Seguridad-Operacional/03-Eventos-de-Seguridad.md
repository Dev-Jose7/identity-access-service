---
title: "Eventos de Seguridad"
linkTitle: "3. Eventos de Seguridad"
weight: 3
url: "/mvp/operacion/seguridad-operacional/eventos-seguridad/"
---

## Eventos a monitorear
- intentos fallidos de autenticacion anormal
- errores de validacion JWT/JWKS
- cambios de configuracion critica
- acceso privilegiado fuera de patron
- rotaciones de secretos y fallas post-rotacion
- errores de autorizacion con potencial cross-tenant
- fallos de arranque por configuracion/secretos faltantes en contenedor

## Respuesta
- clasificar severidad segun impacto
- abrir incidente cuando corresponda
- registrar evidencia y accion de contencion

## Fuentes operativas
- logs estructurados de gateway e IAM;
- auditoria de accesos y cambios operativos;
- metricas de error authn/authz por servicio;
- trazas de flujos criticos con fallo de seguridad.

## JWKS
JWKS es postura oficial del baseline para publicacion de llaves y
validacion interoperable de JWT en consumidores autorizados.

## Tratamiento operacional de JWKS
- monitorear disponibilidad del endpoint JWKS de IAM;
- monitorear fallas de validacion por desactualizacion de llaves;
- forzar refresco de cache en consumidores cuando aplique;
- escalar como incidente de seguridad si hay bloqueo generalizado de acceso.
