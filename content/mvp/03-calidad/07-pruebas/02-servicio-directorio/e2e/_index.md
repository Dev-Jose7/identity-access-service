---
title: "E2E - servicio-directorio"
linkTitle: "3. E2E"
weight: 3
url: "/mvp/calidad/pruebas/servicio-directorio/e2e/"
---

## Objetivo
Validar journeys de negocio de directorio, rutas de rechazo semantico y coherencia cross-servicio para checkout, seguridad e integracion regional.

## Alcance E2E
- ciclo de onboarding organizacional completo.
- direccion/contacto institucional operables para checkout/notificacion.
- parametrizacion regional obligatoria por pais sin fallback global.
- consumo y reaccion a eventos IAM para perfiles organizacionales de usuario.

## Escenarios E2E priorizados
| ID | Escenario | Flujo | Resultado esperado | Trazabilidad |
|---|---|---|---|---|
| DIR-E2E-001 | onboarding organizacional completo | crear organizacion -> legal-data -> activate -> summary | organizacion operable con datos consistentes y auditables | FR-009, NFR-006 |
| DIR-E2E-002 | direccion lista para checkout | registrar direccion -> set default -> validar checkout | validacion `valid=true` y evento `CheckoutAddressValidated` | FR-004, D-DIR-01 |
| DIR-E2E-003 | rechazo checkout por direccion ajena | order valida address de otra organizacion | rechazo semantico estable + sin side effects | FR-004, I-ACC-02 |
| DIR-E2E-004 | contacto institucional primario operativo | registrar 2 contactos -> set primary | primario unico y consumo correcto por notification | FR-006 |
| DIR-E2E-005 | suspension de organizacion y efecto aguas abajo | suspender organizacion -> publicar `OrganizationSuspended` | IAM bloquea acceso y order restringe operacion | FR-009, NFR-005 |
| DIR-E2E-006 | consumo `RoleAssigned` sincroniza profile local | IAM publica evento -> Directory consume | `organization_user_profile` reconciliado con dedupe | FR-009 |
| DIR-E2E-007 | consumo `UserBlocked` desactiva profile local | IAM publica bloqueo -> Directory consume | perfil local `INACTIVE` + auditoria | FR-009, NFR-006 |
| DIR-E2E-008 | politica pais vigente para operacion regional | configurar pais -> resolver politica en runtime | politica correcta por `countryCode` y version | FR-011, RN-LOC-01 |
| DIR-E2E-009 | sin fallback global por pais faltante | operar sin politica vigente | bloqueo con `configuracion_pais_no_disponible` + auditoria | FR-011, NFR-011, I-LOC-01 |
| DIR-E2E-010 | aislamiento tenant en endpoints admin y consultas | actor tenant A sobre recursos tenant B | rechazo `acceso_cruzado_detectado` | FR-009, NFR-005, D-CROSS-01 |
| DIR-E2E-011 | resiliencia outbox/eventos Directory | mutacion exitosa con falla transitoria de broker | decision persiste, outbox queda pendiente y reintenta | NFR-007, NFR-006 |
| DIR-E2E-012 | trazabilidad tecnica completa | mutacion legal/contacto/direccion + publicacion evento | cadena `request -> db -> audit -> outbox -> evento` con correlacion | NFR-006, NFR-009 |

## Criterio de exito E2E
- Escenarios `DIR-E2E-001..012` disenados para ejecucion reproducible; el estado final requiere corrida y evidencia.
- En corrida de certificacion, los flujos criticos (`002`, `005`, `009`, `010`) deben ejecutarse sin desviaciones semanticas.
- En corrida de certificacion, seguridad tenant/rol y regionalizacion obligatoria deben verificarse de punta a punta.

## Evidencia minima por corrida
- Reporte por escenario con estado (`Implementado`/`Ejecutado`/`Validado con evidencia`).
- Extractos de `directory_audit`, `outbox_event` y `processed_event` correlacionados.
- Confirmacion de contratos API/eventos `v1` sin ruptura para consumidores.
