---
title: "Trazabilidad de Dominio"
linkTitle: "9. Trazabilidad"
weight: 9
url: "/mvp/dominio/trazabilidad/"
---

## Marco de trazabilidad
_Responde: para que existe esta matriz, que considera cubierto en dominio y como se conecta el pilar con sus salidas y evidencias._

Consolidar la trazabilidad del pilar de dominio para `MVP`,
relacionando RF/RNF con bounded contexts y con evidencia explicita
en los artefactos del propio pilar.

## Criterio de cierre
_Responde: bajo que criterio un requisito se considera cubierto en dominio o se deja trazado fuera de su alcance directo._
- `Cubierto en dominio`: el requisito tiene BC(s) identificados y evidencia en artefactos semanticos/tacticos del pilar.
- `Trazado fuera de alcance de dominio (no bloqueante)`: el requisito se mantiene trazado, pero no requiere definicion adicional de modelo de dominio en `MVP`.

## Cobertura funcional
_Responde: que requisitos funcionales cubre el dominio, desde que bounded contexts y con que evidencia explicita dentro del pilar._
| RF | BC(s) principales | Evidencia de dominio | Estado |
|---|---|---|---|
| FR-001 | `catalog` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Lenguaje Ubicuo](/mvp/dominio/lenguaje-ubicuo/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contexto Catalogo](/mvp/dominio/contextos-delimitados/catalogo/) | Cubierto en dominio |
| FR-002 | `inventory` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contexto Inventario](/mvp/dominio/contextos-delimitados/inventario/) | Cubierto en dominio |
| FR-003 | `inventory`, `reporting` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Inventario](/mvp/dominio/contextos-delimitados/inventario/), [Contexto Reporteria](/mvp/dominio/contextos-delimitados/reporteria/) | Cubierto en dominio |
| FR-004 | `order`, `inventory`, `directory`, `catalog` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/), [Contexto Inventario](/mvp/dominio/contextos-delimitados/inventario/), [Contexto Catalogo](/mvp/dominio/contextos-delimitados/catalogo/) | Cubierto en dominio |
| FR-005 | `order`, `inventory` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/), [Contexto Inventario](/mvp/dominio/contextos-delimitados/inventario/) | Cubierto en dominio |
| FR-006 | `order`, `notification`, `directory` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/), [Contexto Notificaciones](/mvp/dominio/contextos-delimitados/notificaciones/), [Contexto Directorio](/mvp/dominio/contextos-delimitados/directorio/) | Cubierto en dominio |
| FR-007 | `reporting`, `catalog`, `order` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Reporteria](/mvp/dominio/contextos-delimitados/reporteria/), [Contexto Catalogo](/mvp/dominio/contextos-delimitados/catalogo/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/) | Cubierto en dominio |
| FR-008 | `order`, `notification` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/), [Contexto Notificaciones](/mvp/dominio/contextos-delimitados/notificaciones/) | Cubierto en dominio |
| FR-009 | `identity-access`, `directory` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Lenguaje Ubicuo](/mvp/dominio/lenguaje-ubicuo/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Contexto Identidad y Acceso](/mvp/dominio/contextos-delimitados/identidad-acceso/), [Contexto Directorio](/mvp/dominio/contextos-delimitados/directorio/) | Cubierto en dominio |
| FR-010 | `order`, `notification`, `reporting` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/), [Contexto Notificaciones](/mvp/dominio/contextos-delimitados/notificaciones/), [Contexto Reporteria](/mvp/dominio/contextos-delimitados/reporteria/) | Cubierto en dominio |
| FR-011 | `directory`, `order`, `reporting` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Directorio](/mvp/dominio/contextos-delimitados/directorio/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/), [Contexto Reporteria](/mvp/dominio/contextos-delimitados/reporteria/) | Cubierto en dominio |

## Cobertura no funcional
_Responde: que requisitos no funcionales quedan modelados en dominio y cuales solo permanecen trazados fuera de su alcance directo._
| RNF | Cobertura en dominio | Evidencia de dominio | Estado |
|---|---|---|---|
| NFR-001 | presupuestos de latencia para integraciones sync criticas | [Contratos de Integracion](/mvp/dominio/contratos-integracion/) (`Objetivos operativos por contrato`), [Contexto Catalogo](/mvp/dominio/contextos-delimitados/catalogo/), [Contexto Inventario](/mvp/dominio/contextos-delimitados/inventario/) | Cubierto en dominio |
| NFR-002 | semantica de eventos y corte/reporte semanal | [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Reporteria](/mvp/dominio/contextos-delimitados/reporteria/) | Cubierto en dominio |
| NFR-003 | continuidad operativa no agrega modelado de dominio directo en `MVP` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/) (fronteras de BC y desacople) | Trazado fuera de alcance de dominio (no bloqueante) |
| NFR-004 | invariantes anti-sobreventa y reservas consistentes | [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contexto Inventario](/mvp/dominio/contextos-delimitados/inventario/) | Cubierto en dominio |
| NFR-005 | aislamiento por tenant y control de acceso | [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Contexto Identidad y Acceso](/mvp/dominio/contextos-delimitados/identidad-acceso/) | Cubierto en dominio |
| NFR-006 | correlacion y trazabilidad semantica de comandos/eventos en BCs core | [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Contexto Identidad y Acceso](/mvp/dominio/contextos-delimitados/identidad-acceso/), [Contexto Directorio](/mvp/dominio/contextos-delimitados/directorio/), [Contexto Catalogo](/mvp/dominio/contextos-delimitados/catalogo/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/), [Contexto Notificaciones](/mvp/dominio/contextos-delimitados/notificaciones/) | Cubierto en dominio |
| NFR-007 | notificaciones no bloqueantes con manejo de fallos | [Eventos de Dominio](/mvp/dominio/eventos-dominio/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Notificaciones](/mvp/dominio/contextos-delimitados/notificaciones/), [Contexto Reporteria](/mvp/dominio/contextos-delimitados/reporteria/) | Cubierto en dominio |
| NFR-008 | capacidad/escalamiento no requiere modelado de dominio adicional en `MVP` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/) (separacion de BC y proyecciones) | Trazado fuera de alcance de dominio (no bloqueante) |
| NFR-009 | proceso de entrega no requiere modelado de dominio adicional en `MVP` | [Mapa de Contexto](/mvp/dominio/mapa-contexto/) (fronteras y no acoplamiento transaccional) | Trazado fuera de alcance de dominio (no bloqueante) |
| NFR-010 | ciclo de vida de datos no requiere modelado de dominio adicional en `MVP` | [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/) (invariantes de datos y trazabilidad) | Trazado fuera de alcance de dominio (no bloqueante) |
| NFR-011 | politicas regionales por `countryCode` para operacion y reporte | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Directorio](/mvp/dominio/contextos-delimitados/directorio/), [Contexto Reporteria](/mvp/dominio/contextos-delimitados/reporteria/) | Cubierto en dominio |

## Salida para arquitectura
_Responde: que verdades del dominio deben preservarse en la capa siguiente sin reinterpretar ownership, lenguaje ni eventos._
| Decision/verdad del dominio | Evidencia de dominio | Condicion de preservacion en la capa siguiente |
|---|---|---|
| fronteras y ownership de BCs | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Contextos Delimitados](/mvp/dominio/contextos-delimitados/) | no romper ownership de verdad ni mezclar mutaciones de BCs distintos |
| anti-sobreventa y reserva valida | [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Comportamiento Global](/mvp/dominio/comportamiento-global/), [Contexto Inventario](/mvp/dominio/contextos-delimitados/inventario/) | preservar atomicidad local e idempotencia en stock/reservas |
| sesion/rol/tenant obligatorios en mutaciones | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/) | toda mutacion debe transportar y validar contexto de actor/tenant |
| `OrderCreated` no equivale a `OrderConfirmed` | [Lenguaje Ubicuo](/mvp/dominio/lenguaje-ubicuo/), [Comportamiento Global](/mvp/dominio/comportamiento-global/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/) | no colapsar etapas del pedido ni reinterpretar eventos |
| `notification` no bloquea core | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/) | fallos de soporte no revierten hechos comerciales |
| `reporting` es proyeccion, no fuente transaccional | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/) | ninguna mutacion core nace desde reporting |
| politica regional por pais obligatoria | [Lenguaje Ubicuo](/mvp/dominio/lenguaje-ubicuo/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/) | no existe mutacion critica ni corte semanal sin configuracion regional vigente |

## Salida para pruebas
_Responde: que reglas y procesos del dominio deben convertirse en validaciones verificables en la capa de pruebas._
| Regla/proceso del dominio | Tipo de validacion esperado | Evidencia origen |
|---|---|---|
| RN-INV-01 / RN-RES-01 / I-INV-* | pruebas de agregado e integracion de inventario | [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Contexto Inventario](/mvp/dominio/contextos-delimitados/inventario/) |
| checkout y creacion de pedido | escenarios end-to-end con desvios de direccion/reserva/pais | [Comportamiento Global](/mvp/dominio/comportamiento-global/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/) |
| tenant isolation y autorizacion | pruebas de seguridad y acceso cross-tenant | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Contexto Identidad y Acceso](/mvp/dominio/contextos-delimitados/identidad-acceso/) |
| pago manual idempotente | pruebas de comando y dedupe por referencia | [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/) |
| notificacion no bloqueante | pruebas de resiliencia/retry sin rollback | [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Notificaciones](/mvp/dominio/contextos-delimitados/notificaciones/) |
| reporte semanal y hechos analiticos | pruebas de proyeccion idempotente y corte por pais | [Comportamiento Global](/mvp/dominio/comportamiento-global/), [Contexto Reporteria](/mvp/dominio/contextos-delimitados/reporteria/) |

## Salida para observabilidad
_Responde: que señales, trazas y errores deben preservarse para observar el comportamiento del dominio y auditar sus decisiones._
| Señal obligatoria | Por que importa en dominio | Evidencia origen |
|---|---|---|
| `traceId` y `correlationId` | reconstruir el flujo de checkout, pedido, pago y notificacion | [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/), [Comportamiento Global](/mvp/dominio/comportamiento-global/), [Eventos de Dominio](/mvp/dominio/eventos-dominio/) |
| `tenantId` y `actorId` | sostener auditoria y aislamiento por organizacion | [Mapa de Contexto](/mvp/dominio/mapa-contexto/), [Contratos de Integracion](/mvp/dominio/contratos-integracion/) |
| eventos `Order*`, `Stock*`, `Notification*` | explicar hechos de negocio y reacciones operativas | [Eventos de Dominio](/mvp/dominio/eventos-dominio/) |
| errores canonicos (`stock_insuficiente`, `acceso_cruzado_detectado`, `configuracion_pais_no_disponible`) | hacer visible la razon de rechazo semantico | [Lenguaje Ubicuo](/mvp/dominio/lenguaje-ubicuo/), [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/) |
| backlog/lag de consumo | controlar la salud de proyeccion y notificacion sin reinterpretar el negocio | [Contratos de Integracion](/mvp/dominio/contratos-integracion/), [Contexto Reporteria](/mvp/dominio/contextos-delimitados/reporteria/) |

## Cobertura por artefacto
_Responde: que parte del alcance del pilar cubre cada artefacto principal del dominio._
| Artefacto | Cobertura principal |
|---|---|
| [Mapa de Contexto](/mvp/dominio/mapa-contexto/) | FR-001..FR-011, NFR-004, NFR-005, NFR-011 (vista estrategica y fronteras) |
| [Lenguaje Ubicuo](/mvp/dominio/lenguaje-ubicuo/) | FR-001, FR-004, FR-009, FR-011 y soporte semantico transversal |
| [Conceptos](/mvp/dominio/conceptos/) | glosario de negocio, relaciones conceptuales y patrones de colaboracion |
| [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/) | FR-002, FR-004, FR-005, FR-009, FR-010, FR-011; NFR-004, NFR-005, NFR-006, NFR-011 |
| [Comportamiento Global](/mvp/dominio/comportamiento-global/) | procesos criticos, ownership de decisiones, transiciones de estado e idempotencia |
| [Eventos de Dominio](/mvp/dominio/eventos-dominio/) | FR-001..FR-011; NFR-002, NFR-004, NFR-006, NFR-007, NFR-011 |
| [Contratos de Integracion](/mvp/dominio/contratos-integracion/) | FR-003..FR-011; NFR-001, NFR-002, NFR-006, NFR-007, NFR-011 |
| [Contextos Delimitados](/mvp/dominio/contextos-delimitados/) | evidencia tactica por BC para todas las capacidades del pilar |

## Estado de cobertura
_Responde: que parte del dominio esta cerrada en `MVP`, que queda parcial y que permanece fuera del alcance actual._
| Estado | Alcance |
|---|---|
| Completo en `MVP` | acceso B2B, organizacion/direcciones, catalogo comercial, inventario reservable, carrito/checkout/pedido, pago manual, notificacion no bloqueante y reporte semanal |
| Parcial controlado | estados de fulfillment (`READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED`) y evolucion futura de externos |
| Fuera de alcance del dominio actual | continuidad operativa, capacidad/escalamiento, detalle del proceso de entrega y politicas de retencion/lifecycle de datos tecnicos |

## Cierre del pilar
_Responde: cual es el estado consolidado de cierre de trazabilidad del pilar para `MVP`._
- RF: `11/11` cubiertos en dominio con evidencia explicita.
- RNF: `7/11` cubiertos en dominio y `4/11` trazados como `Trazado fuera de alcance de dominio (no bloqueante)`.
- No hay brechas bloqueantes de trazabilidad interna del pilar para `MVP`.

## Historial de trazabilidad
_Responde: que decisiones relevantes modificaron esta matriz y que impacto tuvieron sobre el cierre del pilar._
| Fecha | Tipo | Cambio | Impacto |
|---|---|---|---|
| 2026-03-11 | Baseline | Se consolida matriz RF/RNF -> BC en dominio. | Primera linea base de cobertura del pilar. |
| 2026-03-11 | Mejora de consistencia | Se elimina referencia a pilares posteriores y se agrega evidencia por artefacto dentro de dominio. | Cierre coherente con regla de dependencia solo hacia pilares anteriores. |
| 2026-03-13 | Reorganizacion editorial | Se agregan trazas hacia arquitectura, pruebas y observabilidad, y se actualiza cobertura por artefacto con `Conceptos` y `Comportamiento Global`. | La trazabilidad queda cerrada sobre el nuevo orden del pilar. |
