---
title: "Secretos y Rotacion"
linkTitle: "2. Secretos y Rotacion"
weight: 2
url: "/mvp/operacion/seguridad-operacional/secretos-rotacion/"
---

## Baseline operativo
- secretos gestionados por entorno en canal oficial de secretos.
- prohibido hardcodear secretos en codigo o repositorio documental.
- cambios de secretos con auditoria y doble validacion operativa.
- secretos inyectados al runtime por entorno; nunca embebidos en imagen Docker.

## Rotacion
- rotacion planificada periodica para secretos criticos.
- rotacion urgente via runbook ante compromiso.
- validar servicio dependiente inmediatamente tras rotacion.

## Tipos de secretos operativos
- aplicacion (tokens internos, claves API externas);
- base de datos por servicio;
- broker/event bus (publish/consume);
- observabilidad (ingesta de metricas/trazas si aplica);
- material criptografico de IAM relacionado con JWT/JWKS.

## Triggers de rotacion urgente
- sospecha de exposicion por incidente de seguridad;
- uso no autorizado detectado en auditoria;
- fuga potencial por error de configuracion.

## Validacion post-rotacion
1. Verificar conectividad/autenticacion al recurso protegido.
2. Confirmar ausencia de errores de autenticacion en logs.
3. Validar flujo critico del servicio en smoke operativo.
4. Confirmar que secreto revocado no siga en uso.
5. Confirmar reinicio/recarga controlada de contenedores afectados cuando
   aplique al entorno.

## Evidencia minima
- identificador de secreto
- fecha de rotacion
- servicios impactados
- verificacion posterior
