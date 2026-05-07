---
title: "Datos Sensibles en Logs"
linkTitle: "4. Datos Sensibles en Logs"
weight: 4
url: "/mvp/operacion/seguridad-operacional/datos-sensibles-logs/"
---

## Regla base
No registrar payloads sensibles completos ni datos personales innecesarios.

## Permitido
- identificadores tecnicos
- codigos de error
- metadatos de trazabilidad (traceId, correlationId, tenantId)
- actorId tecnico cuando corresponda a auditoria

## No permitido
- tokens completos
- secretos
- payloads completos con PII sensible
- credenciales de broker, DB o proveedores externos
- llaves privadas o material criptografico sensible

## Politica minima de masking/redaction
- enmascarar correos, telefonos y documentos personales cuando se requiera log.
- registrar solo ultimos caracteres de identificadores sensibles si se necesita
  correlacion operativa.
- aplicar redaccion de campos sensibles antes de persistir logs.

## Control operacional
- revisiones periodicas de logs y sanitizacion en adaptadores de entrada/salida.
- validacion de que errores de negocio no expongan payloads sensibles.
- evidencia de revision en incidentes de seguridad y postmortems aplicables.
- centralizar lectura de logs desde `stdout/stderr` del runtime del contenedor.
