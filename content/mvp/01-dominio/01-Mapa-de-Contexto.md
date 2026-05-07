---
title: "Mapa de Contexto"
linkTitle: "1. Mapa de Contexto"
weight: 1
url: "/mvp/dominio/mapa-contexto/"
---

## Marco estrategico
_Responde: que realidad del negocio modela este mapa, para que existe y hasta donde llega su autoridad semantica._

Definir la vista estrategica del dominio ArkaB2B para el MVP backend: bounded contexts, fronteras, relaciones upstream/downstream y reglas de integracion semantica.

Modelar la operacion B2B de Arka desde la autenticacion por tenant
hasta el ciclo comercial de carrito, reserva, pedido, pago manual,
notificacion y reporte semanal, preservando verdad de negocio,
aislamiento por organizacion y regionalizacion por pais.

- Entra en el dominio de `MVP`: acceso B2B, organizacion y direcciones, catalogo comercial, inventario reservable, pedido/pago manual, notificacion no bloqueante y reporting semanal derivado.
- Queda fuera del baseline operativo: despacho, entrega, conciliacion financiera avanzada, ERP/PSP y mutaciones transaccionales desde `reporting`.
- Los pilares siguientes toman este dominio como verdad semantica, pero no redefinen fronteras, ownership ni significado de los conceptos aqui aprobados.
- El baseline semantico del dominio queda congelado para `MVP`; cualquier cambio breaking de ownership, lenguaje o invariantes requiere nuevo ciclo.

## Drivers del dominio (FR/NFR)
_Responde: que necesidades funcionales y no funcionales obligan a particionar y proteger este dominio en el MVP._
| Driver | Descripcion | IDs relacionados |
|---|---|---|
| Compra B2B sin sobreventa | Carrito y checkout con reservas vigentes y confirmables | FR-004, FR-005, NFR-004 |
| Aislamiento por organizacion | Cero acceso cruzado entre tenants | FR-009, NFR-005 |
| Trazabilidad auditable | Mutaciones de stock/pedido/pago con actor y correlacion | FR-010, NFR-006 |
| Visibilidad operativa | Notificaciones y reportes semanales para negocio/operaciones | FR-006, FR-007, FR-008, NFR-007 |
| Regionalizacion por pais | Operacion por organizacion con moneda, corte semanal y retencion resueltos por `countryCode` sin fallback global no autorizado | FR-011, NFR-011 |

## Estructura del dominio
_Responde: con que criterio se separa el dominio y como se clasifican sus capacidades en core, supporting y generic._
- Separacion por capacidad de negocio y vocabulario propio para evitar modelos ambiguos entre acceso, comercial, inventario y analitica.
- Limite por consistencia transaccional: cada BC protege invariantes locales y publica hechos para sincronizacion eventual entre BCs.
- Dependencia minima entre BCs: integracion via contratos sync/async y ACL cuando cambia la semantica entre productor y consumidor.
- Distincion entre core/supporting/generic segun impacto directo en compra B2B y riesgo operativo del MVP.
- Aislamiento multi-tenant como restriccion transversal: ningun BC asume acceso cruzado ni bypass de validaciones de tenant.

| Subdominio | Tipo | BCs principales | Justificacion |
|---|---|---|---|
| Operacion comercial B2B | Core | `catalog`, `inventory`, `order`, `directory` | genera la compra, protege disponibilidad y aplica reglas comerciales/regionales |
| Acceso y aislamiento | Core | `identity-access` | sin identidad, sesion y tenant validos no existe operacion segura del MVP |
| Comunicacion operacional | Supporting | `notification` | soporta avisos y seguimiento sin decidir verdad comercial |
| Analitica semanal | Generic | `reporting` | consolida vistas derivadas sin ser fuente transaccional |

## Mapa de bounded contexts
_Responde: que bounded contexts existen, que verdad posee cada uno y que queda explicitamente fuera de su frontera._
| BC | Clasificacion | Responsabilidad principal | Fuente de verdad | No le pertenece |
|---|---|---|---|---|
| `identity-access` | Core | autenticacion, sesion, rol y autorizacion por tenant | usuario/sesion/rol | direcciones, catalogo, stock, pedidos |
| `directory` | Core | organizacion, perfiles de usuario organizacionales, contactos institucionales, direcciones y parametros operativos por pais | organizacion/perfiles locales/contactos institucionales/direcciones/configuracion regional | sesiones, credenciales, stock, pedido |
| `catalog` | Core | producto, variante SKU, precio vigente y vendible | producto/SKU/precio | disponibilidad reservable y estado de pedido |
| `inventory` | Core | stock fisico, disponibilidad reservable, reservas y movimientos | stock/reserva/ledger | precio, vendible, autorizacion |
| `order` | Core | carrito, checkout, pedido y estado de pago agregado | carrito/pedido/pago | stock real, identidad, configuracion maestra |
| `notification` | Supporting | solicitudes/intentos de notificacion no bloqueante | solicitud/intento de notificacion | decisiones comerciales y estados core |
| `reporting` | Generic | vistas derivadas de ventas y abastecimiento | vistas derivadas (no transaccionales) | mutacion de verdad operacional o comercial |

Reglas de frontera explicita:
- `identity-access` no gestiona direcciones, catalogo, stock ni pedidos.
- `directory` no autentica usuarios ni emite sesiones.
- `catalog` define vendible/precio, pero no disponibilidad reservable.
- `inventory` decide disponibilidad/reserva, pero no crea pedidos.
- `order` crea pedido en `PENDING_APPROVAL` y solo `order` decide su paso a `CONFIRMED`.
- `notification` es verdad operacional de entrega (intentos/resultados), no autoridad de decisiones comerciales.
- `reporting` es contexto de proyeccion/analitica; no actua como sistema transaccional ni fuente de verdad operacional.

## Mapa de relaciones
_Responde: como colaboran los bounded contexts, quien consume a quien y bajo que patron semantico lo hacen._
| Downstream | Upstream | Patron | Tipo | Uso semantico |
|---|---|---|---|---|
| `order` | `identity-access` | Customer/Supplier | Sync consulta fallback + Async eventos | resolver estado real de sesion/rol solo cuando el contexto de seguridad no sea confiable |
| `order` | `directory` | Customer/Supplier + ACL | Sync API | validar direccion, pertenencia y parametros operativos por pais de la organizacion |
| `order` | `catalog` | Conformist + snapshot | Sync API | resolver variante vendible y precio vigente |
| `order` | `inventory` | Customer/Supplier + ACL | Sync API + Async observabilidad | crear/confirmar/liberar reservas y reaccionar expiraciones |
| `notification` | `order` | Open Host | Async eventos | enviar mensajes por eventos de pedido/pago |
| `notification` | `inventory` | Open Host | Async eventos | notificar expiracion de reserva y bajo stock |
| `reporting` | `notification` | Conformist pasivo | Async eventos | consolidar efectividad de entrega y descarte por canal |
| `reporting` | `catalog` | Conformist pasivo | Async eventos | actualizar hechos de catalogo/precio |
| `reporting` | `inventory` | Conformist pasivo | Async eventos | actualizar hechos de stock/reservas |
| `reporting` | `order` | Conformist pasivo | Async eventos | consolidar ventas/estado/pagos |
| `directory` | `identity-access` | Customer/Supplier | Async eventos + Sync consulta fallback | sincronizar perfiles locales vinculados a IAM ante bloqueos o cambios de rol relevantes |

Definiciones operativas y ejemplos de estos patrones: [Conceptos](/mvp/dominio/conceptos/).

## Ownership semantico
_Responde: quien es dueno de cada concepto relevante y que otros contextos solo lo consumen sin redefinirlo._
| Concepto relevante | BC owner | Consumidores principales | Regla de autoridad |
|---|---|---|---|
| identidad, sesion y rol | `identity-access` | `order`, `directory` | ningun otro BC autentica ni reinterpreta permisos efectivos |
| organizacion, perfiles de usuario organizacionales, direcciones, contactos institucionales y parametros por pais | `directory` | `order`, `notification`, `reporting` | consumidores usan snapshot/consulta; no replican verdad maestra |
| producto, variante vendible y precio vigente | `catalog` | `order`, `reporting`, `inventory` | `order` congela snapshot; `inventory` no redefine precio ni vendible |
| stock fisico, disponibilidad y reserva | `inventory` | `order`, `reporting` | solo `inventory` decide si existe disponibilidad reservable |
| carrito, pedido y estado de pago | `order` | `notification`, `reporting`, `inventory` | ningun BC externo muta estado comercial del pedido |
| solicitud/intento/resultado de envio | `notification` | `reporting` | `order` publica hechos, pero no asume exito de entrega |
| hecho analitico y reporte semanal | `reporting` | consumo humano/operacional | ninguna decision transaccional nace desde `reporting` |

## ACLs requeridas
_Responde: donde hace falta traducir lenguaje entre contextos para proteger el modelo local y evitar contaminacion semantica._
| ACL | Direccion | Traduccion clave | Riesgo mitigado |
|---|---|---|---|
| `ACL-ORD-INV` | `inventory -> order` | `StockReserved/StockReservationExpired/StockReservationConfirmed -> item_reservable/no_reservable/confirmado` | sobreventa y checkout inconsistente |
| `ACL-ORD-DIR` | `directory -> order` | `direccion_organizacional -> address_snapshot_valida` | pedido con direccion ajena o invalida |
| `ACL-ORD-CAT` | `catalog -> order` | `precio_vigente -> price_snapshot` | drift de precio durante checkout |
| `ACL-NOTI-CORE` | `core -> notification` | `hecho_negocio -> solicitud_notificacion` | acoplamiento de negocio con canal |
| `ACL-REP-CORE` | `core -> reporting` | `evento_transaccional -> hecho_analitico` | uso de reporting como fuente transaccional |

## Fronteras operativas relevantes
_Responde: que ejes operativos atraviesan el dominio y condicionan autorizacion, regionalizacion y trazabilidad._
| Frontera | Owner semantico | Impacto en el dominio |
|---|---|---|
| `tenantId` | `identity-access` + `directory` | delimita aislamiento, autorizacion y trazabilidad end-to-end |
| `organizationId` | `directory` | define pertenencia comercial de direcciones, perfiles organizacionales locales, contactos institucionales y configuracion regional |
| `countryCode` | `directory` | determina moneda, corte semanal y retencion para mutaciones y reportes |
| `actorId` + `role` | `identity-access` | condiciona autorizacion de comandos mutantes y auditoria |
| `channel` | `notification` | decide estrategia de entrega sin alterar verdad comercial |
| `warehouseId` | `inventory` | delimita stock y reservas por almacen/SKU |

## Control de fugas de contexto
_Responde: que riesgos de contaminacion o reinterpretacion del modelo existen y como se contienen._
| Fuga potencial | Impacto | Mitigacion |
|---|---|---|
| `order` interpreta vendible como disponibilidad | sobreventa | separacion estricta: `catalog=vendible`, `inventory=disponibilidad` |
| `directory` duplica estado de sesion | acceso inconsistente | `identity-access` como fuente unica de sesion |
| `reporting` usado para decisiones transaccionales | estados invalidos | regla global: `reporting` no muta core |
| `notification` asume exito de entrega como exito de negocio | falsas confirmaciones | evento `NotificationFailed` no afecta core |

## Trazabilidad resumida RF/RNF -> BC
_Responde: que requisitos quedan cubiertos por cada bounded context y donde se concentra la responsabilidad principal._
| IDs | BCs principales | Cobertura resumida |
|---|---|---|
| FR-001, FR-002, FR-003 | `catalog`, `inventory`, `reporting` | catalogo comercial, stock y abastecimiento semanal |
| FR-004, FR-005 | `order`, `inventory`, `directory`, `catalog` | checkout/pedido con reserva valida, direccion valida y precio vigente |
| FR-006, FR-008 | `order`, `notification` | cambios de estado y recuperacion de carrito via notificacion no bloqueante |
| FR-007 | `reporting`, `catalog`, `order` | reporte semanal consolidado por tenant |
| FR-009 | `identity-access`, `directory` | autenticacion/autorizacion y aislamiento por organizacion |
| FR-010 | `order`, `notification`, `reporting` | registro de pago manual, aviso y consolidacion |
| FR-011 | `directory`, `order`, `reporting` | reglas por pais para operacion y reporte |
| NFR-004, NFR-005, NFR-006 | `inventory`, `identity-access`, `order` | invariantes de sobreventa, seguridad tenant y trazabilidad auditable |
| NFR-007, NFR-011 | `notification`, `directory`, `reporting`, `order` | resiliencia de notificaciones y operacion regional por pais |
| NFR-001, NFR-002 | `order`, `catalog`, `reporting` | presupuestos de latencia y semantica de corte/reporte semanal |
| NFR-003, NFR-008, NFR-009, NFR-010 | Fuera de alcance directo de modelo de dominio | trazados en dominio; detalle tecnico/evidencia en pilares siguientes |

## Historial del mapa
_Responde: que cambios relevantes se aprobaron en este mapa y que impacto tuvieron sobre el baseline del dominio._
| Fecha | Tipo | Cambio | Impacto |
|---|---|---|---|
| 2026-03-11 | Baseline | Se consolida mapa estrategico con BCs, fronteras, relaciones, ACLs, procesos y politicas globales. | Base estable para diseno de contratos y detalle tactico por BC. |
| 2026-03-11 | Mejora de estructura | Se agregan criterios explicitos de particion y trazabilidad resumida RF/RNF -> BC. | Cierre de coherencia entre drivers, mapa estrategico y trazabilidad del pilar. |
| 2026-03-13 | Reorganizacion editorial | Se separan procesos/ownership hacia `Comportamiento Global` y se agregan subdominios, ownership semantico y fronteras operativas. | El archivo queda alineado con la capa estrategica del pilar. |

Si se propone fusionar/dividir BCs o cambiar patron sync/async de checkout:
- Registrar decision, racional y alcance en esta seccion.
- Actualizar los dossiers de BC impactados dentro de `content/mvp/01-dominio/08-bounded-contexts/`.
