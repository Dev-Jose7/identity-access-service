---
title: "Comandos"
linkTitle: "4. Comandos"
weight: 4
url: "/mvp/dominio/contextos-delimitados/directorio/comandos/"
---

## Marco de comandos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Catalogo de comandos de `directory`.

## Lista de comandos
_Responde: que comandos admite el contexto y con que efecto semantico._

### registrar_organizacion
_Esta subseccion detalla registrar_organizacion dentro del contexto del documento._
- Input esperado:
  - `organizationCode`, `legalName`, `countryCode`, `taxIdType`, `taxId`, `organizationOperationalPreferences`, `idempotencyKey`.
- Precondiciones:
  - `taxId` valido y no duplicado.
- Postcondiciones:
  - organizacion creada en `ONBOARDING`.
- Errores:
  - `tax_id_duplicado`, `tax_id_invalido`.

### actualizar_organizacion
_Esta subseccion detalla actualizar_organizacion dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `tenantId`, `profilePatch`, `idempotencyKey`.
- Precondiciones:
  - organizacion existente y tenant valido.
- Postcondiciones:
  - perfil organizacional actualizado.
- Errores:
  - `organizacion_no_encontrada`, `acceso_cruzado_detectado`.

### actualizar_datos_legales
_Esta subseccion detalla actualizar_datos_legales dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `countryCode`, `taxId`, `legalRepresentative`, `idempotencyKey`.
- Precondiciones:
  - validacion legal aprobada.
- Postcondiciones:
  - perfil legal actualizado/verificado.
- Errores:
  - `tax_id_invalido`.

### configurar_parametros_operativos_pais
_Esta subseccion detalla configurar_parametros_operativos_pais dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `countryCode`, `currencyCode`, `weeklyCutoff`, `retentionPolicyRef`, `reason`, `idempotencyKey`.
- Precondiciones:
  - organizacion existente y `countryCode` soportado.
- Postcondiciones:
  - parametros operativos por pais versionados y auditables para la organizacion.
- Errores:
  - `configuracion_pais_no_disponible`, `acceso_cruzado_detectado`.

### activar_organizacion
_Esta subseccion detalla activar_organizacion dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `reason`, `idempotencyKey`.
- Precondiciones:
  - existe direccion valida por tipo y contacto institucional primario.
- Postcondiciones:
  - organizacion `ACTIVE`.
- Errores:
  - `direccion_invalida`, `contacto_primario_inconsistente`.

### suspender_organizacion
_Esta subseccion detalla suspender_organizacion dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `reason`, `idempotencyKey`.
- Postcondiciones:
  - organizacion `SUSPENDED`.
- Errores:
  - `organizacion_no_encontrada`.

### registrar_direccion
_Esta subseccion detalla registrar_direccion dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `addressType`, `line1`, `city`, `countryCode`, `geo`, `idempotencyKey`.
- Precondiciones:
  - organizacion activa.
- Postcondiciones:
  - direccion activa registrada.
- Errores:
  - `direccion_invalida`, `organizacion_inactiva`.

### actualizar_direccion
_Esta subseccion detalla actualizar_direccion dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `addressId`, `patch`, `idempotencyKey`.
- Precondiciones:
  - direccion del mismo tenant.
- Postcondiciones:
  - direccion actualizada y revalidada.
- Errores:
  - `direccion_no_encontrada`, `acceso_cruzado_detectado`.

### establecer_direccion_predeterminada
_Esta subseccion detalla establecer_direccion_predeterminada dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `addressId`, `addressType`, `idempotencyKey`.
- Postcondiciones:
  - una unica direccion default por tipo.

### desactivar_direccion
_Esta subseccion detalla desactivar_direccion dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `addressId`, `reason`, `idempotencyKey`.
- Precondiciones:
  - no dejar organizacion activa sin direccion operativa minima.
- Errores:
  - `direccion_default_inconsistente`.

### registrar_perfil_usuario_organizacion
_Esta subseccion detalla registrar_perfil_usuario_organizacion dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `iamUserId`, `firstName`, `lastName`, `jobTitle`, `department`, `locale`, `timezone`, `idempotencyKey`.
- Precondiciones:
  - organizacion existente y usuario IAM resoluble para el mismo tenant.
- Postcondiciones:
  - perfil de usuario organizacional activo registrado.
- Errores:
  - `perfil_usuario_organizacion_duplicado`, `acceso_cruzado_detectado`.

### actualizar_perfil_usuario_organizacion
_Esta subseccion detalla actualizar_perfil_usuario_organizacion dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `profileId`, `patch`, `idempotencyKey`.
- Precondiciones:
  - perfil existente y del mismo tenant.
- Postcondiciones:
  - estado y atributos locales del perfil actualizados.
- Errores:
  - `perfil_usuario_organizacion_no_encontrado`, `acceso_cruzado_detectado`.

### desactivar_perfil_usuario_organizacion
_Esta subseccion detalla desactivar_perfil_usuario_organizacion dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `profileId`, `reason`, `idempotencyKey`.
- Postcondiciones:
  - perfil de usuario organizacional local `INACTIVE`.
- Errores:
  - `perfil_usuario_organizacion_no_encontrado`.

### registrar_contacto
_Esta subseccion detalla registrar_contacto dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `contactType`, `label`, `contactValue`, `idempotencyKey`.
- Precondiciones:
  - valor de contacto no duplicado por organizacion/tipo.
- Postcondiciones:
  - contacto institucional activo registrado.
- Errores:
  - `contacto_duplicado`.

### actualizar_contacto
_Esta subseccion detalla actualizar_contacto dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `contactId`, `patch`, `idempotencyKey`.

### establecer_contacto_primario
_Esta subseccion detalla establecer_contacto_primario dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `contactId`, `contactType`, `idempotencyKey`.
- Postcondiciones:
  - un solo contacto institucional primario por tipo.

### desactivar_contacto
_Esta subseccion detalla desactivar_contacto dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `contactId`, `reason`, `idempotencyKey`.

### validar_direccion_checkout
_Esta subseccion detalla validar_direccion_checkout dentro del contexto del documento._
- Input esperado:
  - `organizationId`, `addressId`, `checkoutCorrelationId`.
- Precondiciones:
  - direccion activa y owner correcto.
- Postcondiciones:
  - resultado de validacion (valida/no valida + reasonCode).
