---
title: "Agregados"
linkTitle: "3. Agregados"
weight: 3
url: "/mvp/dominio/contextos-delimitados/directorio/agregados/"
---

## Marco de agregados
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir agregados e invariantes de `directory`.

## Agregados
_Responde: que agregados protegen consistencia dentro del contexto._

### OrganizationAggregate
_Esta subseccion detalla organizationaggregate dentro del contexto del documento._
- Proposito: preservar integridad de datos organizacionales y regionales.
- Invariantes:
  - `I-DIR-01`: `taxId` unico por pais en organizaciones activas.
  - organizacion activa requiere perfil legal verificado.
  - organizacion activa requiere parametros operativos por pais vigentes y versionados.
  - organizacion activa requiere al menos un contacto institucional primario operable.
- Errores:
  - `tax_id_duplicado`, `tax_id_invalido`, `organizacion_inactiva`, `configuracion_pais_no_disponible`.

### AddressAggregate
_Esta subseccion detalla addressaggregate dentro del contexto del documento._
- Proposito: garantizar direcciones operables para checkout.
- Invariantes:
  - `I-DIR-02`: direccion activa pertenece a una sola organizacion.
  - `I-DIR-03`: una sola direccion default por tipo.
  - `I-DIR-05`: direccion checkout debe ser activa y del mismo tenant.
- Errores:
  - `direccion_invalida`, `direccion_no_pertenece_a_organizacion`, `direccion_default_inconsistente`.

### OrganizationUserProfileAggregate
_Esta subseccion detalla organizationuserprofileaggregate dentro del contexto del documento._
- Proposito: preservar el perfil de usuario organizacional local vinculado a IAM sin duplicar autenticacion ni credenciales.
- Invariantes:
  - un solo perfil de usuario organizacional local por `organizationId + iamUserId`.
  - un perfil de usuario organizacional local `ACTIVE` no puede seguir operativo si IAM emite `UserBlocked`.
  - los atributos locales de rol/ownership son referenciales y no reemplazan la verdad de autorizacion de `identity-access`.
- Errores:
  - `perfil_usuario_organizacion_duplicado`, `perfil_usuario_organizacion_no_encontrado`, `perfil_usuario_organizacion_inactivo`.

### OrganizationContactAggregate
_Esta subseccion detalla organizationcontactaggregate dentro del contexto del documento._
- Proposito: mantener canales institucionales de la organizacion para facturacion, soporte, ventas u operacion.
- Invariantes:
  - contacto institucional primario unico por tipo.
  - valor de contacto unico por organizacion y tipo cuando esta activo.
  - solo un contacto `ACTIVE` puede marcarse como primario.
- Errores:
  - `contacto_duplicado`, `contacto_primario_inconsistente`.

## Reglas de consistencia
_Responde: que invariantes locales debe preservar este artefacto._
- Activar organizacion valida direccion default, contacto institucional primario y politica regional vigente.
- `RoleAssigned` y `UserBlocked` reconcilian perfiles de usuario organizacionales sin duplicar sesiones ni permisos efectivos.
- Mutaciones generan auditoria y outbox.
