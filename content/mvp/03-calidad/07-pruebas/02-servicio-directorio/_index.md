---
title: "Servicio Directorio"
linkTitle: "2. Directorio"
weight: 2
url: "/mvp/calidad/pruebas/servicio-directorio/"
---

## Objetivo
Asegurar la verdad organizacional del sistema: organizacion, direcciones, contactos institucionales y parametros operativos por pais, con aislamiento por tenant y trazabilidad completa.

## Alcance de calidad del servicio
- Flujos HTTP de `organization`, `address`, `contact`, `checkout-address-validations` y `operational-country-settings`.
- Flujos async: publicacion de eventos de directorio por outbox y consumo idempotente de eventos IAM (`RoleAssigned`, `UserBlocked`) para `organization_user_profile`.
- Reglas de seguridad: tenant/ownership, permisos por rol, masking de PII y auditoria `directory_audit`.

## Fuentes de verdad usadas
- Producto: `FR-004`, `FR-006`, `FR-009`, `FR-011`, `NFR-005`, `NFR-006`, `NFR-009`, `NFR-010`, `NFR-011`.
- Dominio: `RN-LOC-01`, `I-LOC-01`, `D-DIR-01`, `D-DIR-02`, `I-ACC-02`, `D-CROSS-01`.
- Arquitectura Directory: contratos API/eventos, seguridad, datos y runtime interno.

## Datos de entrada comunes
- `tenant` principal: `org-co-001`.
- `tenant` alterno: `org-ec-001`.
- actores base:
  - `tenant_user`.
  - `tenant_admin`.
  - `arka_operator`.
  - `arka_admin`.
  - `trusted_service(order-service)` para endpoints tecnicos.
- trazabilidad tecnica obligatoria en mutaciones: `traceId`, `correlationId`.
- idempotencia en mutaciones administrativas via `Idempotency-Key`.

## Criterio de exito global
- Carpetas `unitarias`, `integracion` y `e2e` cubren escenarios criticos `P1` y `P2` de Directory.
- Cada escenario referencia al menos un `FR/NFR` y una regla/invariante de dominio.
- Errores canonicos de Directory cubiertos en pruebas:
  - `acceso_cruzado_detectado`
  - `direccion_no_pertenece_a_organizacion`
  - `direccion_invalida`
  - `contacto_primario_inconsistente`
  - `tax_id_duplicado`
  - `configuracion_pais_no_disponible`
- Evidencia tecnica por flujo: `directory_audit`, `outbox_event`, `processed_event`, logs estructurados con `traceId/correlationId`.

## Cobertura minima obligatoria (Directory)
| Bloque | Cobertura minima |
|---|---|
| Organizacion | alta, activacion/suspension, perfil legal y validaciones de `taxId` |
| Direcciones | alta, actualizacion, default unico, desactivacion y validacion checkout |
| Contactos institucionales | alta, actualizacion, primario unico por tipo, desactivacion |
| Regionalizacion | configurar/resolver politica por `countryCode` sin fallback global |
| Seguridad y aislamiento | tenant/ownership, RBAC, actor tecnico confiable, masking PII |
| Integracion EDA | eventos `Organization*`, `Address*`, `Contact*`, `CountryOperationalPolicyConfigured`, `CheckoutAddressValidated` + consumo IAM con dedupe |
