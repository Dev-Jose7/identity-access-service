---
title: "Anti-Corrupcion"
linkTitle: "8. Anti-Corrupcion"
weight: 8
url: "/mvp/dominio/contextos-delimitados/directorio/anti-corrupcion/"
---

## Marco de traduccion
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Documentar traducciones semanticas entre `directory` y otros BC.

## Mapeos principales
_Responde: que traducciones principales hace el contexto al cruzar sus fronteras._
| Upstream/Downstream | Termino externo | Termino en directory | Regla |
|---|---|---|---|
| `identity-access -> directory` | `tenantId` del token | `organizationId` propietario | must match para mutaciones |
| `identity-access -> directory` | `userId` / `RoleAssigned` / `UserBlocked` | `organization_user_profile` | los cambios IAM actualizan estado y atributos locales del perfil sin duplicar credenciales |
| `order -> directory` | `addressId` de checkout | `direccion_checkout_valida` | direccion activa y del mismo tenant |
| `directory -> order` | `direccion_default` | `address_snapshot_valida` | snapshot inmutable al confirmar pedido |
| `directory -> notification` | `contacto_institucional_primario` | `destinatario_operativo` | canal institucional se resuelve sin mutar core |

## Normalizacion de errores
_Responde: como traduce este contexto errores externos a su lenguaje canonico._
| Error tecnico | Error canonico de dominio |
|---|---|
| tax validator unavailable | `validacion_externa_no_disponible` |
| tax invalid | `tax_id_invalido` |
| ownership mismatch | `acceso_cruzado_detectado` |
| inactive address for checkout | `direccion_invalida` |
| user blocked in IAM | `perfil_usuario_organizacion_inactivo` |

## Reglas ACL
_Responde: que reglas gobiernan la capa ACL para evitar contaminacion semantica._
- `order` no interpreta reglas internas de direcciones; consume resultado canonico de validacion.
- `directory` no replica sesion/rol de IAM; consume identidad de forma referencial y mantiene solo perfil de usuario organizacional local dentro de la organizacion.
- `notification` consume eventos de contacto institucional, no lectura directa de tablas del directorio.
