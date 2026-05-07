---
title: "Repositorios y Puertos"
linkTitle: "7. Repositorios y Puertos"
weight: 7
url: "/mvp/dominio/contextos-delimitados/catalogo/repositorios-puertos/"
---

## Marco de puertos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir puertos de entrada/salida y contratos de persistencia/integracion de `catalog`.

## Puertos de entrada (in)
_Responde: que puertos de entrada exponen casos de uso o capacidades del contexto._
| Port | Tipo | Metodos clave |
|---|---|---|
| `ProductUseCasePort` | in | `registrarProducto`, `actualizarProducto`, `activarProducto`, `retirarProducto` |
| `VariantUseCasePort` | in | `crearVariante`, `actualizarVariante`, `marcarVarianteVendible`, `descontinuarVariante` |
| `PricingUseCasePort` | in | `actualizarPrecioVigente`, `programarCambioPrecio`, `cargaMasivaPrecios` |
| `CatalogQueryPort` | in | `buscarCatalogo`, `obtenerDetalleProducto`, `listarVariantes`, `resolverVarianteCheckout` |

## Puertos de salida (out)
_Responde: que puertos de salida necesita el contexto para colaborar con otros sistemas._
| Port | Tipo | Metodos clave |
|---|---|---|
| `ProductRepositoryPort` | out | `findById`, `save`, `findByCode` |
| `VariantRepositoryPort` | out | `findById`, `findBySku`, `save`, `listByProduct` |
| `PriceRepositoryPort` | out | `findCurrent`, `expireCurrent`, `save`, `validateNoOverlap` |
| `BrandRepositoryPort` | out | `existsActiveById` |
| `CategoryRepositoryPort` | out | `existsActiveById`, `findPathById` |
| `CatalogAuditPort` | out | `record` |
| `OutboxPort` | out | `append`, `listPending`, `markPublished` |
| `InventoryHintPort` | out | `updateAvailabilityHint`, `reconcileSkuState` |

## Contratos de consistencia
_Responde: que contratos preservan consistencia entre puertos, repositorios y reglas locales._
- SKU unico por tenant validado antes de persistir variante.
- Marca/categoria activas se validan antes de persistir o retaxonomizar producto.
- Upsert de precio expira precio anterior antes de activar nuevo.
- Mutaciones generan auditoria y outbox de forma consistente.

Nota de alcance:
- `BrandRepositoryPort` y `CategoryRepositoryPort` se usan para validar taxonomia referencial local.
- `MVP` no expone puertos de entrada para CRUD independiente de marca/categoria.
