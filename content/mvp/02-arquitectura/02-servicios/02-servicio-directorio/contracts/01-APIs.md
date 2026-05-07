---
title: "APIs"
linkTitle: "1. APIs"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-directorio/contratos/apis/"
---

## Proposito
Definir contratos API REST reactivos completos para `directory-service`, incluyendo gestion de organizacion, direcciones, contactos institucionales, validaciones para checkout y parametros operativos por pais para FR-011/NFR-011.

## Alcance y fronteras
- Incluye endpoints publicos y administrativos de Directory.
- Incluye payloads, errores, versionado, idempotencia y politicas de seguridad.
- Incluye solo contratos REST publicos del MVP para `organization`, `address`, `contact` y politica operativa por pais.
- Excluye de este artefacto la sincronizacion interna de `organization_user_profile` desde eventos IAM; ese slice no expone REST publico en `MVP`.
- Excluye serializacion OpenAPI formal (`yaml/json`) de fase 04.

## Mapa de endpoints Directory
```mermaid
flowchart TB
  B2B["Admin Organizacional B2B"] --> O1["POST /api/v1/directory/organizations"]
  B2B --> O2["PATCH /api/v1/directory/organizations/{organizationId}"]
  B2B --> O3["PATCH /api/v1/directory/organizations/{organizationId}/legal-data"]
  ARKA["Operador Arka"] --> O4["POST /api/v1/directory/organizations/{organizationId}/activate"]
  ARKA --> O5["POST /api/v1/directory/organizations/{organizationId}/suspend"]
  B2B --> A1["POST /api/v1/directory/organizations/{organizationId}/addresses"]
  B2B --> A2["PATCH /api/v1/directory/organizations/{organizationId}/addresses/{addressId}"]
  B2B --> A3["POST /api/v1/directory/organizations/{organizationId}/addresses/{addressId}/set-default"]
  B2B --> A4["POST /api/v1/directory/organizations/{organizationId}/addresses/{addressId}/deactivate"]
  B2B --> A5["GET /api/v1/directory/organizations/{organizationId}/addresses"]
  B2B --> A6["GET /api/v1/directory/organizations/{organizationId}/addresses/{addressId}"]
  B2B --> C1["POST /api/v1/directory/organizations/{organizationId}/contacts"]
  B2B --> C2["PATCH /api/v1/directory/organizations/{organizationId}/contacts/{contactId}"]
  B2B --> C3["POST /api/v1/directory/organizations/{organizationId}/contacts/{contactId}/set-primary"]
  B2B --> C4["POST /api/v1/directory/organizations/{organizationId}/contacts/{contactId}/deactivate"]
  B2B --> C5["GET /api/v1/directory/organizations/{organizationId}/contacts"]
  B2B --> C6["GET /api/v1/directory/organizations/{organizationId}/contacts/{contactId}"]
  ORD["order-service"] --> V1["POST /api/v1/directory/organizations/{organizationId}/checkout-address-validations"]
  B2B --> P1["PUT /api/v1/directory/organizations/{organizationId}/operational-country-settings/{countryCode}"]
  ORD --> P2["GET /api/v1/directory/organizations/{organizationId}/operational-country-settings/{countryCode}"]
  B2B --> S1["GET /api/v1/directory/organizations/{organizationId}/summary"]
  B2B --> O6["GET /api/v1/directory/organizations/{organizationId}"]
```

## Resumen de contratos por endpoint
| Endpoint | Objetivo | Auth requerida | Idempotencia |
|---|---|---|---|
| `POST /api/v1/directory/organizations` | Crear organizacion y datos base | `arka_admin` | `Idempotency-Key` requerida |
| `PATCH /api/v1/directory/organizations/{organizationId}` | Actualizar perfil organizacional | `arka_admin` o `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `PATCH /api/v1/directory/organizations/{organizationId}/legal-data` | Actualizar taxId y metadatos legales | `arka_admin` | `Idempotency-Key` requerida |
| `POST /api/v1/directory/organizations/{organizationId}/activate` | Activar organizacion | `arka_admin` | `Idempotency-Key` requerida |
| `POST /api/v1/directory/organizations/{organizationId}/suspend` | Suspender organizacion | `arka_admin` | `Idempotency-Key` requerida |
| `POST /api/v1/directory/organizations/{organizationId}/addresses` | Registrar direccion | `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `PATCH /api/v1/directory/organizations/{organizationId}/addresses/{addressId}` | Actualizar direccion | `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `POST /api/v1/directory/organizations/{organizationId}/addresses/{addressId}/set-default` | Definir direccion default | `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `POST /api/v1/directory/organizations/{organizationId}/addresses/{addressId}/deactivate` | Desactivar direccion | `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `GET /api/v1/directory/organizations/{organizationId}/addresses` | Listar direcciones activas/inactivas | `tenant_user` same tenant | N/A |
| `GET /api/v1/directory/organizations/{organizationId}/addresses/{addressId}` | Obtener direccion puntual | `tenant_user` same tenant | N/A |
| `POST /api/v1/directory/organizations/{organizationId}/contacts` | Registrar contacto institucional | `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `PATCH /api/v1/directory/organizations/{organizationId}/contacts/{contactId}` | Actualizar contacto institucional | `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `POST /api/v1/directory/organizations/{organizationId}/contacts/{contactId}/set-primary` | Definir contacto institucional primario | `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `POST /api/v1/directory/organizations/{organizationId}/contacts/{contactId}/deactivate` | Desactivar contacto institucional | `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `GET /api/v1/directory/organizations/{organizationId}/contacts` | Listar contactos institucionales | `tenant_user` same tenant | N/A |
| `GET /api/v1/directory/organizations/{organizationId}/contacts/{contactId}` | Obtener contacto institucional puntual | `tenant_user` same tenant | N/A |
| `POST /api/v1/directory/organizations/{organizationId}/checkout-address-validations` | Validar direccion para checkout | `trusted_service(order-service)` | `Idempotency-Key` opcional |
| `PUT /api/v1/directory/organizations/{organizationId}/operational-country-settings/{countryCode}` | Configurar parametros operativos por pais para la organizacion | `arka_admin` o `tenant_admin` same tenant | `Idempotency-Key` requerida |
| `GET /api/v1/directory/organizations/{organizationId}/operational-country-settings/{countryCode}` | Resolver parametros operativos vigentes por pais para runtime | `trusted_service(order-service)` o `tenant_user` same tenant | N/A |
| `GET /api/v1/directory/organizations/{organizationId}/summary` | Obtener resumen consolidado | `tenant_user` same tenant | N/A |
| `GET /api/v1/directory/organizations/{organizationId}` | Obtener perfil organizacional puntual | `tenant_user` same tenant | N/A |

## Regla de mapeo regional (404 vs 409)
- Este endpoint de resolucion (`GET .../operational-country-settings/{countryCode}`)
  representa consulta directa del recurso tecnico de politica regional.
- Si no existe version vigente, retorna `404 configuracion_pais_no_disponible`.
- Los servicios consumidores (`order`, `reporting`) pueden mapear esa ausencia a
  `409 configuracion_pais_no_disponible` cuando bloquean una operacion de
  negocio por precondicion regional no satisfecha.
- No existe fallback global implicito.

## Contratos detallados
### 1) POST /api/v1/directory/organizations
Request:
```json
{
  "organizationCode": "org-ec-001",
  "legalName": "TecnoMayoristas Andinos S.A.S.",
  "tradeName": "TecnoAndinos",
  "countryCode": "EC",
  "currencyCode": "USD",
  "timezone": "America/Guayaquil",
  "segmentTier": "A",
  "legalData": {
    "taxIdType": "RUC",
    "taxId": "1790012345001",
    "fiscalRegime": "GENERAL"
  },
  "organizationOperationalPreferences": {
    "preferredChannel": "EMAIL",
    "creditEnabled": true,
    "monthlyCreditLimit": 25000.0
  }
}
```

Response 201:
```json
{
  "organizationId": "org_01JX...",
  "organizationCode": "org-ec-001",
  "status": "ONBOARDING",
  "legalName": "TecnoMayoristas Andinos S.A.S.",
  "countryCode": "EC",
  "createdAt": "2026-03-02T16:05:00Z"
}
```

Errores:
- `409 TAX_ID_ALREADY_EXISTS`
- `422 TAX_ID_INVALID`
- `403 FORBIDDEN_ACTION`

### 2) PATCH /api/v1/directory/organizations/{organizationId}
Request:
```json
{
  "tradeName": "TecnoAndinos B2B",
  "segmentTier": "A",
  "billingEmail": "facturacion@tecnoandinos.ec",
  "websiteUrl": "https://tecnoandinos.ec"
}
```

Response 200:
```json
{
  "organizationId": "org_01JX...",
  "tradeName": "TecnoAndinos B2B",
  "segmentTier": "A",
  "updatedAt": "2026-03-02T16:09:00Z"
}
```

### 3) PATCH /api/v1/directory/organizations/{organizationId}/legal-data
Request:
```json
{
  "taxIdType": "RUC",
  "taxId": "1790012345001",
  "fiscalRegime": "GENERAL",
  "legalRepresentative": "Maria Fernanda Ruiz"
}
```

Response 200:
```json
{
  "organizationId": "org_01JX...",
  "taxIdType": "RUC",
  "taxIdMasked": "179******5001",
  "verificationStatus": "VERIFIED",
  "verifiedAt": "2026-03-02T16:12:00Z"
}
```

### 4) POST /api/v1/directory/organizations/{organizationId}/activate
Request:
```json
{
  "reason": "ONBOARDING_COMPLETED"
}
```

Response 200:
```json
{
  "organizationId": "org_01JX...",
  "status": "ACTIVE",
  "updatedAt": "2026-03-02T16:20:00Z"
}
```

### 5) POST /api/v1/directory/organizations/{organizationId}/suspend
Request:
```json
{
  "reason": "COMPLIANCE_REVIEW"
}
```

Response 200:
```json
{
  "organizationId": "org_01JX...",
  "status": "SUSPENDED",
  "updatedAt": "2026-03-02T16:25:00Z"
}
```

### 6) POST /api/v1/directory/organizations/{organizationId}/addresses
Request:
```json
{
  "addressType": "SHIPPING",
  "alias": "Bodega Norte Quito",
  "line1": "Av. Galo Plaza Lasso 1234",
  "line2": "Bodega 4",
  "city": "Quito",
  "state": "Pichincha",
  "postalCode": "170104",
  "countryCode": "EC",
  "reference": "Frente al parque industrial",
  "geoLocation": {
    "latitude": -0.102,
    "longitude": -78.486
  }
}
```

Response 201:
```json
{
  "addressId": "addr_01JX...",
  "organizationId": "org_01JX...",
  "addressType": "SHIPPING",
  "status": "ACTIVE",
  "isDefault": true,
  "validatedAt": "2026-03-02T16:30:00Z"
}
```

### 7) PATCH /api/v1/directory/organizations/{organizationId}/addresses/{addressId}
Request:
```json
{
  "line2": "Bodega 6",
  "reference": "Ingreso por puerta lateral",
  "postalCode": "170105"
}
```

Response 200:
```json
{
  "addressId": "addr_01JX...",
  "status": "ACTIVE",
  "validationStatus": "VERIFIED",
  "updatedAt": "2026-03-02T16:36:00Z"
}
```

### 8) POST /api/v1/directory/organizations/{organizationId}/addresses/{addressId}/set-default
Request:
```json
{
  "addressType": "SHIPPING",
  "reason": "PRIMARY_OPERATIONAL_SITE"
}
```

Response 200:
```json
{
  "organizationId": "org_01JX...",
  "addressType": "SHIPPING",
  "defaultAddressId": "addr_01JX...",
  "updatedAt": "2026-03-02T16:40:00Z"
}
```

### 9) POST /api/v1/directory/organizations/{organizationId}/addresses/{addressId}/deactivate
Request:
```json
{
  "reason": "WAREHOUSE_CLOSED"
}
```

Response 200:
```json
{
  "addressId": "addr_01JX...",
  "status": "INACTIVE",
  "updatedAt": "2026-03-02T16:45:00Z"
}
```

### 10) GET /api/v1/directory/organizations/{organizationId}/addresses
Query params:
- `status=ACTIVE|INACTIVE|ALL`
- `type=SHIPPING|BILLING|WAREHOUSE|ALL`
- `page`, `size`

Response 200:
```json
{
  "items": [
    {
      "addressId": "addr_01JX...",
      "addressType": "SHIPPING",
      "alias": "Bodega Norte Quito",
      "city": "Quito",
      "isDefault": true,
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 1
}
```

### 11) POST /api/v1/directory/organizations/{organizationId}/contacts
Request:
```json
{
  "contactType": "EMAIL",
  "label": "PURCHASING",
  "contactValue": "compras@tecnoandinos.ec",
  "isPrimary": true
}
```

Response 201:
```json
{
  "contactId": "ctc_01JX...",
  "organizationId": "org_01JX...",
  "contactType": "EMAIL",
  "label": "PURCHASING",
  "status": "ACTIVE",
  "isPrimary": true
}
```

### 12) PATCH /api/v1/directory/organizations/{organizationId}/contacts/{contactId}
Request:
```json
{
  "label": "SUPPORT",
  "contactValue": "+593997001100"
}
```

Response 200:
```json
{
  "contactId": "ctc_01JX...",
  "status": "ACTIVE",
  "label": "SUPPORT",
  "contactValueMasked": "+593******1100",
  "updatedAt": "2026-03-02T16:52:00Z"
}
```

### 13) POST /api/v1/directory/organizations/{organizationId}/contacts/{contactId}/set-primary
Request:
```json
{
  "contactType": "EMAIL",
  "reason": "RESPONSIBLE_CHANGED"
}
```

Response 200:
```json
{
  "organizationId": "org_01JX...",
  "contactType": "EMAIL",
  "primaryContactId": "ctc_01JX...",
  "updatedAt": "2026-03-02T16:55:00Z"
}
```

### 14) POST /api/v1/directory/organizations/{organizationId}/contacts/{contactId}/deactivate
Request:
```json
{
  "reason": "NO_LONGER_WITH_COMPANY"
}
```

Response 200:
```json
{
  "contactId": "ctc_01JX...",
  "status": "INACTIVE",
  "updatedAt": "2026-03-02T16:59:00Z"
}
```

### 15) GET /api/v1/directory/organizations/{organizationId}/contacts
Query params:
- `status=ACTIVE|INACTIVE|ALL`
- `type=EMAIL|PHONE|WHATSAPP|WEBSITE|ALL`
- `label=PURCHASING|FINANCE|SUPPORT|GENERAL|ALL`
- `page`, `size`

Response 200:
```json
{
  "items": [
    {
      "contactId": "ctc_01JX...",
      "contactType": "EMAIL",
      "label": "PURCHASING",
      "contactValueMasked": "co*****@tecnoandinos.ec",
      "isPrimary": true,
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 1
}
```

### 16) POST /api/v1/directory/organizations/{organizationId}/checkout-address-validations
Request:
```json
{
  "addressId": "addr_01JX...",
  "orderIntentId": "ordintent_01JX...",
  "requestedByService": "order-service"
}
```

Response 200:
```json
{
  "valid": true,
  "reasonCode": "OK",
  "organizationId": "org_01JX...",
  "addressSnapshot": {
    "addressId": "addr_01JX...",
    "line1": "Av. Galo Plaza Lasso 1234",
    "line2": "Bodega 6",
    "city": "Quito",
    "state": "Pichincha",
    "postalCode": "170105",
    "countryCode": "EC",
    "reference": "Ingreso por puerta lateral"
  },
  "validatedAt": "2026-03-02T17:05:00Z"
}
```

### 17) PUT /api/v1/directory/organizations/{organizationId}/operational-country-settings/{countryCode}
Request:
```json
{
  "currencyCode": "COP",
  "weekStartsOn": "MONDAY",
  "weeklyCutoffLocalTime": "17:00:00",
  "timezone": "America/Bogota",
  "reportingRetentionDays": 730,
  "effectiveFrom": "2026-03-01T00:00:00Z",
  "effectiveTo": null
}
```

Response 200:
```json
{
  "organizationId": "org_01JX...",
  "countryCode": "CO",
  "policyVersion": 4,
  "currencyCode": "COP",
  "weekStartsOn": "MONDAY",
  "weeklyCutoffLocalTime": "17:00:00",
  "timezone": "America/Bogota",
  "reportingRetentionDays": 730,
  "status": "ACTIVE",
  "updatedAt": "2026-03-02T17:07:00Z"
}
```

Errores:
- `409 configuracion_pais_no_disponible` (solapamiento de vigencia o estado organizacion invalido)
- `422 validation_error` (`countryCode`/`currencyCode`/zona horaria invalida)
- `403 FORBIDDEN_ACTION`

### 18) GET /api/v1/directory/organizations/{organizationId}/operational-country-settings/{countryCode}
Response 200:
```json
{
  "organizationId": "org_01JX...",
  "countryCode": "CO",
  "policyVersion": 4,
  "currencyCode": "COP",
  "weekStartsOn": "MONDAY",
  "weeklyCutoffLocalTime": "17:00:00",
  "timezone": "America/Bogota",
  "reportingRetentionDays": 730,
  "effectiveFrom": "2026-03-01T00:00:00Z",
  "effectiveTo": null,
  "status": "ACTIVE",
  "resolvedAt": "2026-03-02T17:08:00Z"
}
```

Errores:
- `404 configuracion_pais_no_disponible`
- `403 TENANT_MISMATCH`

### 19) GET /api/v1/directory/organizations/{organizationId}/summary
Response 200:
```json
{
  "organization": {
    "organizationId": "org_01JX...",
    "status": "ACTIVE",
    "legalName": "TecnoMayoristas Andinos S.A.S.",
    "taxIdMasked": "179******5001"
  },
  "defaultAddresses": {
    "billingAddressId": "addr_01JX_BILLING",
    "shippingAddressId": "addr_01JX_SHIPPING"
  },
  "primaryContacts": [
    {
      "contactType": "EMAIL",
      "label": "PURCHASING",
      "contactId": "ctc_01JX_MAIL"
    },
    {
      "contactType": "PHONE",
      "label": "GENERAL",
      "contactId": "ctc_01JX_PHONE"
    }
  ],
  "generatedAt": "2026-03-02T17:08:00Z"
}
```

### 20) GET /api/v1/directory/organizations/{organizationId}
Response 200:
```json
{
  "organizationId": "org_01JX...",
  "organizationCode": "org-ec-001",
  "legalName": "TecnoMayoristas Andinos S.A.S.",
  "tradeName": "TecnoAndinos B2B",
  "countryCode": "EC",
  "currencyCode": "USD",
  "timezone": "America/Guayaquil",
  "segmentTier": "A",
  "status": "ACTIVE",
  "billingEmail": "facturacion@tecnoandinos.ec",
  "websiteUrl": "https://tecnoandinos.ec",
  "organizationOperationalPreferences": {
    "preferredChannel": "EMAIL",
    "creditEnabled": true,
    "monthlyCreditLimit": 25000.0
  },
  "updatedAt": "2026-03-02T17:09:00Z"
}
```

Errores:
- `404 ORGANIZATION_NOT_FOUND`
- `403 TENANT_MISMATCH`

### 21) GET /api/v1/directory/organizations/{organizationId}/addresses/{addressId}
Response 200:
```json
{
  "addressId": "addr_01JX...",
  "organizationId": "org_01JX...",
  "addressType": "SHIPPING",
  "alias": "Bodega Norte Quito",
  "line1": "Av. Galo Plaza Lasso 1234",
  "line2": "Bodega 6",
  "city": "Quito",
  "state": "Pichincha",
  "postalCode": "170105",
  "countryCode": "EC",
  "reference": "Ingreso por puerta lateral",
  "isDefault": true,
  "status": "ACTIVE",
  "validationStatus": "VERIFIED",
  "validatedAt": "2026-03-02T16:36:00Z"
}
```

Errores:
- `404 ADDRESS_NOT_FOUND`
- `403 TENANT_MISMATCH`

### 22) GET /api/v1/directory/organizations/{organizationId}/contacts/{contactId}
Response 200:
```json
{
  "contactId": "ctc_01JX...",
  "organizationId": "org_01JX...",
  "contactType": "EMAIL",
  "label": "PURCHASING",
  "contactValueMasked": "co*****@tecnoandinos.ec",
  "isPrimary": true,
  "status": "ACTIVE",
  "updatedAt": "2026-03-02T16:52:00Z"
}
```

Errores:
- `404 CONTACT_NOT_FOUND`
- `403 TENANT_MISMATCH`

## Envelope de error estandar
```json
{
  "timestamp": "2026-03-02T17:10:00Z",
  "traceId": "trc_01JX...",
  "correlationId": "cor_01JX...",
  "code": "ADDRESS_NOT_VALID_FOR_CHECKOUT",
  "message": "Address is inactive or does not belong to organization",
  "details": [
    {
      "field": "addressId",
      "issue": "ownership_or_status_violation"
    }
  ]
}
```

## Politicas transversales
| Tema | Politica |
|---|---|
| Versionado | Path versioning (`/api/v1`), breaking changes en `/api/v2` |
| Compatibilidad | Campos nuevos solo opcionales en `v1` |
| Idempotencia | Obligatoria para operaciones mutantes de organization/address/contact |
| Seguridad | TLS obligatorio, JWT con `tenantId`, validacion de ownership en cada operacion |
| Rate limiting | mutantes 40 req/min por `organizationId`; consultas 120 req/min |
| Auditoria | Toda mutacion Directory registra `directory_audit` |
| Regionalizacion | Operaciones criticas deben resolver politica activa por `countryCode`; no hay fallback global implicito |

## Riesgos y mitigaciones
- Riesgo: exposicion accidental de PII en respuestas/listados.
  - Mitigacion: masking por default para `contactValue` en respuestas de lista y detalle resumido.
- Riesgo: operaciones cross-tenant por mala propagacion de claims.
  - Mitigacion: validacion doble gateway + politica en dominio (`TenantIsolationPolicy`).
- Riesgo: checkout mutante sin configuracion operativa vigente por pais.
  - Mitigacion: exponer endpoint de resolucion de politica regional y devolver `configuracion_pais_no_disponible` como contrato estable.
