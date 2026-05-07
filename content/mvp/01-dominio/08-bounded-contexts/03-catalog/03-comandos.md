---
title: "Comandos"
linkTitle: "4. Comandos"
weight: 4
url: "/mvp/dominio/contextos-delimitados/catalogo/comandos/"
---

## Marco de comandos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Catalogo de comandos de `catalog`.

## Lista de comandos
_Responde: que comandos admite el contexto y con que efecto semantico._

### registrar_producto
_Esta subseccion detalla registrar_producto dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `productCode`, `name`, `brandId`, `categoryId`, `tags`, `idempotencyKey`.
- Precondiciones:
  - codigo de producto no duplicado.
  - marca y categoria activas como taxonomia referencial valida.
- Postcondiciones:
  - producto creado en `DRAFT`.
- Errores:
  - `product_code_duplicado`, `brand_o_categoria_invalida`.

### actualizar_producto
_Esta subseccion detalla actualizar_producto dentro del contexto del documento._
- Input esperado:
  - `productId`, `tenantId`, `patch`, `idempotencyKey`.
- Precondiciones:
  - si el patch cambia taxonomia, la nueva marca/categoria debe existir y estar activa.
- Postcondiciones:
  - metadata actualizada.

### activar_producto
_Esta subseccion detalla activar_producto dentro del contexto del documento._
- Input esperado:
  - `productId`, `reason`, `idempotencyKey`.
- Precondiciones:
  - producto valido para activacion.
- Errores:
  - `producto_no_activo`.

### retirar_producto
_Esta subseccion detalla retirar_producto dentro del contexto del documento._
- Input esperado:
  - `productId`, `reason`, `idempotencyKey`.
- Postcondiciones:
  - producto `RETIRED`, variantes no vendibles.

### crear_variante
_Esta subseccion detalla crear_variante dentro del contexto del documento._
- Input esperado:
  - `productId`, `tenantId`, `sku`, `name`, `attributes[]`, `idempotencyKey`.
- Precondiciones:
  - sku unico.
- Postcondiciones:
  - variante creada en `DRAFT`.
- Errores:
  - `sku_no_unico`.

### actualizar_variante
_Esta subseccion detalla actualizar_variante dentro del contexto del documento._
- Input esperado:
  - `productId`, `variantId`, `patch`, `idempotencyKey`.

### marcar_variante_vendible
_Esta subseccion detalla marcar_variante_vendible dentro del contexto del documento._
- Input esperado:
  - `productId`, `variantId`, `sellableFrom`, `sellableUntil`, `idempotencyKey`.
- Precondiciones:
  - producto `ACTIVE`, atributos requeridos completos.
- Errores:
  - `producto_no_activo`, `required_attributes_missing`.

### descontinuar_variante
_Esta subseccion detalla descontinuar_variante dentro del contexto del documento._
- Input esperado:
  - `productId`, `variantId`, `reason`, `idempotencyKey`.

### actualizar_precio_vigente
_Esta subseccion detalla actualizar_precio_vigente dentro del contexto del documento._
- Input esperado:
  - `variantId`, `amount`, `currency`, `effectiveFrom`, `idempotencyKey`.
- Precondiciones:
  - variante vendible, monto > 0.
- Errores:
  - `variante_no_vendible`, `precio_invalido`, `periodo_precio_solapado`.

### programar_cambio_precio
_Esta subseccion detalla programar_cambio_precio dentro del contexto del documento._
- Input esperado:
  - `variantId`, `amount`, `currency`, `effectiveFrom`, `effectiveUntil`, `idempotencyKey`.

### carga_masiva_precios
_Esta subseccion detalla carga_masiva_precios dentro del contexto del documento._
- Input esperado:
  - `items[]`, `operationRef`, `idempotencyKey`.
- Postcondiciones:
  - lote aplicado idempotentemente.

### resolver_variante_checkout
_Esta subseccion detalla resolver_variante_checkout dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `variantId`, `at`, `currency`.
- Postcondiciones:
  - respuesta de vendible + precio vigente.

## Nota de alcance MVP
- `catalog` no define comandos CRUD independientes para `marca` o `categoria` en `MVP`.
- La taxonomia se mantiene como referencia local controlada del servicio y se valida durante `registrar_producto` y `actualizar_producto`.
