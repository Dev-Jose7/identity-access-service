---
title: "Repositorios y Puertos"
linkTitle: "7. Repositorios y Puertos"
weight: 7
url: "/mvp/dominio/contextos-delimitados/directorio/repositorios-puertos/"
---

## Marco de puertos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir puertos de entrada/salida y contratos de persistencia/integracion de `directory`.

## Puertos de entrada (in)
_Responde: que puertos de entrada exponen casos de uso o capacidades del contexto._
| Port | Tipo | Metodos clave |
|---|---|---|
| `OrganizationUseCasePort` | in | `registrarOrganizacion`, `actualizarOrganizacion`, `configurarParametrosOperativosPais`, `activarOrganizacion`, `suspenderOrganizacion` |
| `AddressUseCasePort` | in | `registrarDireccion`, `actualizarDireccion`, `establecerDireccionPredeterminada`, `desactivarDireccion`, `validarDireccionCheckout` |
| `OrganizationUserProfileUseCasePort` | in | `registrarPerfilUsuarioOrganizacion`, `actualizarPerfilUsuarioOrganizacion`, `desactivarPerfilUsuarioOrganizacion` |
| `OrganizationContactUseCasePort` | in | `registrarContacto`, `actualizarContacto`, `establecerContactoPrimario`, `desactivarContacto` |
| `DirectoryQueryPort` | in | `listarDirecciones`, `listarContactosInstitucionales`, `listarPerfilesUsuarioOrganizacion`, `obtenerResumenOrganizacion` |

## Puertos de salida (out)
_Responde: que puertos de salida necesita el contexto para colaborar con otros sistemas._
| Port | Tipo | Metodos clave |
|---|---|---|
| `OrganizationRepositoryPort` | out | `findById`, `save`, `updateStatus` |
| `AddressRepositoryPort` | out | `findById`, `listByOrganization`, `save`, `unsetDefaultByType` |
| `OrganizationUserProfileRepositoryPort` | out | `findByIamUserId`, `findById`, `listByOrganization`, `save`, `updateStatus` |
| `OrganizationContactRepositoryPort` | out | `findById`, `listByOrganization`, `save`, `setPrimaryByType` |
| `CountryOperationalPolicyRepositoryPort` | out | `findByCountryCode`, `findByOrganizationId`, `saveVersionedPolicy` |
| `DirectoryAuditPort` | out | `record` |
| `TaxValidationPort` | out | `validateTaxId` |
| `GeoValidationPort` | out | `normalizeAndValidateAddress` |
| `OutboxPort` | out | `append`, `listPending`, `markPublished` |

## Contratos de consistencia
_Responde: que contratos preservan consistencia entre puertos, repositorios y reglas locales._
- Activacion de organizacion valida prerequisitos minimos (direccion default, contacto institucional primario y politica regional vigente).
- `RoleAssigned` y `UserBlocked` solo actualizan perfiles de usuario organizacionales locales; no duplican sesion ni autorizacion efectiva de IAM.
- Cambios de direccion default/contacto institucional primario se ejecutan de forma atomica por tipo.
- Toda mutacion publica evento de dominio via outbox.
