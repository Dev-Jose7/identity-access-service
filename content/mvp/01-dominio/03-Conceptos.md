---
title: "Conceptos"
linkTitle: "3. Conceptos"
weight: 3
url: "/mvp/dominio/conceptos/"
---

## Marco de conceptos
_Responde: para que existe esta guia y que tipo de conceptos o patrones aclara dentro del pilar de dominio._

Definir, en lenguaje operativo, el significado de los patrones y conceptos
tecnicos usados en el pilar de dominio para evitar interpretaciones distintas
entre contextos.

## Uso de la guia
_Responde: como debe consultarse esta guia para mantener consistencia al leer y extender el dominio._
- Usar esta guia como referencia rapida al leer `Mapa de Contexto` y `Contratos de Integracion`.
- Si aparece un patron o termino nuevo en una relacion entre BCs, agregarlo aqui antes de adoptarlo.
- Mantener ejemplos alineados con los flujos reales de `MVP`.

## Conceptos del negocio
_Responde: que significan los conceptos de negocio mas importantes y como se distinguen de otros cercanos._
| Concepto | Explicacion simple | Como se diferencia de conceptos cercanos |
|---|---|---|
| `Tenant` | frontera de aislamiento del sistema para una operacion y sus recursos | no es lo mismo que la entidad comercial llamada `Organizacion` |
| `Organizacion` | cliente B2B que compra, tiene direcciones, contactos institucionales y opera con reglas por pais | vive dentro de un tenant y no reemplaza la nocion de sesion/rol |
| `Perfil de usuario organizacional` | representacion local del usuario IAM dentro de la organizacion para contexto operativo | no reemplaza autenticacion, sesion ni permisos efectivos |
| `Contacto institucional` | canal institucional de la organizacion para ventas, soporte, facturacion u operacion | no representa una persona ni una identidad autenticable |
| `Carrito` | lista editable de intencion de compra antes del compromiso comercial | aun no es un pedido ni una venta cerrada |
| `Checkout` | validacion final antes de crear pedido | no es un estado permanente; es una verificacion de negocio |
| `Pedido` | compromiso comercial ya materializado desde un checkout valido | deja de ser editable como carrito y pasa a ciclo de pedido |
| `Reserva` | apartado temporal de stock para sostener el checkout | no es inventario definitivo ni venta consumada |
| `Variante (SKU)` | unidad concreta que se vende | no es el producto completo ni garantiza stock |
| `Disponibilidad` | stock realmente reservable en este momento | no equivale a `vendible` ni a `stock fisico` bruto |
| `Solicitud de notificacion` | instruccion operacional para intentar un envio | no garantiza entrega ni cambia verdad comercial |
| `Hecho analitico` | dato derivado que ayuda a medir y reportar | no se usa para mutar BCs core |

## Relaciones conceptuales
_Responde: como se conectan entre si los conceptos principales del dominio y cual es la lectura correcta de esas relaciones._
| Concepto origen | Relacion | Concepto destino | Lectura correcta |
|---|---|---|---|
| `Tenant` | delimita | `usuario`, `organizacion`, `pedido`, `stock` | todo recurso mutable vive dentro de una frontera de tenant |
| `Organizacion` | posee | `direcciones`, `perfiles de usuario organizacionales`, `contactos institucionales`, `parametros operativos por pais` | `directory` define esta verdad y `order`/`notification`/`reporting` la consumen segun el caso |
| `Usuario B2B` | se materializa localmente como | `perfil de usuario organizacional` | `directory` preserva contexto local sin duplicar credenciales ni sesion |
| `Producto` | agrupa | `variantes (SKU)` | el producto organiza; la variante se vende |
| `Variante (SKU)` | se cruza con | `stock` | `catalog` define vendible; `inventory` define disponibilidad |
| `Carrito` | contiene | `items con reservationRef` | cada item del carrito depende de una reserva vigente o revalidable |
| `Checkout` | valida | `direccion`, `reservas`, `politica regional` | si falla, no existe pedido valido |
| `Pedido` | congela snapshot de | `precio`, `direccion`, `politica regional` | lo confirmado no se reescribe por cambios posteriores |
| `Evento de negocio` | alimenta | `notificacion` y `reporting` | los BCs de soporte reaccionan sin gobernar la verdad core |

## DDD sin tecnicismos
_Responde: como entender los terminos de DDD del pilar en lenguaje simple y sin bajar todavia a implementacion._
DDD es una forma de organizar un sistema para que siga la logica real del
negocio y no se vuelva una mezcla de reglas sueltas.

| Termino DDD | Explicacion no tecnica | Ejemplo simple en ArkaB2B |
|---|---|---|
| `Dominio` | El problema real que se quiere resolver. | Venta B2B con carrito, stock, pedidos y pagos. |
| `Subdominio` | Una parte del problema total. | Catalogo, inventario, pedidos, notificaciones. |
| `Contexto delimitado` | Una "zona" con reglas y lenguaje propios. | En `catalog` "vendible" no significa lo mismo que "disponible" en `inventory`. |
| `Lenguaje ubicuo` | Palabras acordadas para evitar confusiones. | Todos usan "reserva" con el mismo significado. |
| `Entidad` | Algo con identidad propia que cambia en el tiempo. | Un pedido con `orderId`. |
| `Objeto de valor` | Dato que importa por su valor, no por su identidad. | Un precio (`monto + moneda`) o una direccion. |
| `Agregado` | Grupo de datos que se debe cuidar como una sola unidad. | `OrderAggregate` protege pedido, lineas y estado. |
| `Raiz de agregado` | La "puerta principal" para cambiar un agregado. | Solo el agregado de pedido decide si puede confirmar/cancelar. |
| `Regla de negocio` | Politica del negocio que dice que se permite o no. | No confirmar pedido sin reservas vigentes. |
| `Invariante` | Condicion que nunca se puede romper dentro del agregado. | `stock_fisico` nunca puede quedar negativo. |
| `Comando` | Una accion pedida al sistema. | `confirmar_pedido`. |
| `Evento de dominio` | Un hecho que ya ocurrio en el negocio. | `OrderCreated`, `OrderConfirmed`, `StockReserved`. |
| `Politica` | Criterio para decidir ante una situacion. | Si carrito supera cierto tiempo, marcar abandono. |
| `Repositorio` | Forma de guardar/recuperar agregados. | Guardar y cargar pedidos por `orderId`. |
| `ACL (Anti-Corruption Layer)` | Traductor entre dos contextos para que no se "contaminen". | `order` traduce datos de `directory` antes de usarlos. |

Idea clave:
- Regla de negocio: define lo que el negocio quiere.
- Invariante: garantiza que el estado del agregado nunca quede invalido.
- Comando: intenta cambiar algo.
- Evento: confirma que ese cambio ocurrio.

## Patrones entre contextos
_Responde: que patrones de colaboracion existen entre bounded contexts, cuando aplican y que significan en este dominio._
| Patron | Significado en este dominio | Cuando usarlo | Ejemplo actual |
|---|---|---|---|
| `Customer/Supplier` | El downstream consume una capacidad del upstream con contrato explicito. | El consumidor necesita validacion o dato actual en linea. | `order -> directory` para validar direccion y politica regional antes de confirmar pedido. |
| `Customer/Supplier + ACL` | Igual a `Customer/Supplier`, pero con una capa de traduccion semantica entre modelos. | El contrato externo no coincide 1:1 con el modelo interno del consumidor. | `order -> directory` para traducir direccion organizacional a `address_snapshot_valida`. |
| `Conformist + snapshot` | El consumidor acepta la semantica del proveedor y congela una foto de datos al momento de la operacion. | Se necesita estabilidad historica aunque cambie el dato fuente. | `order -> catalog` para resolver variante vendible y congelar `price_snapshot` en el pedido. |
| `Conformist pasivo` | El consumidor recibe eventos del upstream sin imponer cambios de contrato y sin orquestar al productor. | Proyecciones o analitica derivada basada en hechos publicados. | `reporting <- notification` para consolidar entrega/descarte por canal. |
| `Open Host` | El upstream expone eventos estables para que multiples consumidores se suscriban sin acoplamiento directo. | El productor debe notificar hechos sin llamadas sync a cada consumidor. | `notification <- order` para enviar mensajes por eventos de pedido/pago. |

## Tipos de integracion
_Responde: que formas de integracion usa el dominio y que tradeoff semantico implica cada una._
| Tipo | Significado | Ventaja | Costo/Riesgo |
|---|---|---|---|
| `Sync API` | Solicitud/respuesta en linea. | Validacion inmediata y resultado determinista. | Acoplamiento temporal; si el proveedor cae, el flujo puede bloquearse. |
| `Async eventos` | Publicacion/suscripcion desacoplada por eventos. | Resiliencia y menor acoplamiento operativo. | Eventual consistency, reintentos e idempotencia obligatoria. |

## Terminos tecnicos clave
_Responde: que terminos tecnicos aparecen en el pilar y cual es su significado operativo minimo para leerlo correctamente._
| Concepto | Definicion operativa | Ejemplo en `MVP` |
|---|---|---|
| `Upstream` | BC que publica la capacidad o hechos consumidos por otro BC. | `catalog` es upstream de `order` para vendible/precio. |
| `Downstream` | BC que depende del contrato del upstream. | `reporting` es downstream de `order` y `notification`. |
| `ACL` | Capa que traduce modelo/errores externos a semantica canonica interna. | `ACL-ORD-DIR` evita que `order` dependa del modelo interno de `directory`. |
| `Snapshot` | Foto inmutable del dato relevante al confirmar una accion de negocio. | `order` conserva precio/direccion al confirmar pedido. |
| `Idempotencia` | Repetir una operacion/evento no cambia el resultado final. | `reporting` deduplica por `eventId` al reprocesar eventos. |
| `Outbox` | Publicacion confiable de eventos acoplada a la mutacion local del BC. | `order` e `inventory` publican hechos core sin perdida de evento. |
| `At-least-once` | Garantia de entrega que permite duplicados. | Consumidores de `notification` y `reporting` deben deduplicar. |
| `Eventual consistency` | Los BCs convergen en el tiempo, no en la misma transaccion. | Reportes semanales se actualizan por eventos asincronos. |
| `traceId` | Identificador de traza tecnica de una operacion distribuida. | Un checkout mantiene el mismo `traceId` en llamadas y eventos. |
| `correlationId` | Identificador de correlacion de negocio entre comandos/eventos relacionados. | Pedido, pago y notificacion comparten `correlationId` del flujo. |
| `Versionado de contrato` | Evolucion explicita de payload sin romper consumidores existentes. | Topic canonico `v<major>` para eventos entre BCs. |

## Ejemplos de lectura
_Responde: como debe interpretarse una relacion real entre contextos al leer el mapa y los contratos del dominio._
| Relacion | Lectura correcta | Nota de diseno |
|---|---|---|
| `order -> directory (Customer/Supplier + ACL, Sync API)` | `order` consulta/valida direccion y parametros por pais; ACL traduce a su modelo interno antes de mutar el agregado. | La ACL esta fuera del agregado; el agregado recibe datos ya canonicos. |
| `order -> catalog (Conformist + snapshot, Sync API)` | `order` acepta semantica de vendible/precio y congela foto de precio en el pedido. | Cambios posteriores de precio no reescriben pedidos confirmados. |
| `notification <- order (Open Host, Async eventos)` | `order` publica hechos de pedido/pago y `notification` decide canal/plantilla sin bloquear `order`. | Falla de notificacion no revierte la transaccion de pedido. |
| `reporting <- notification (Conformist pasivo, Async eventos)` | `reporting` consume eventos de entrega/descarte tal como vienen y consolida indicadores por canal. | Reporting depende de calidad semantica del contrato de eventos. |

## Criterio de extension
_Responde: que condiciones debe cumplir un patron o termino nuevo antes de agregarse a esta guia._
- Debe existir una definicion operativa y un ejemplo con BCs reales del ciclo.
- Debe indicar riesgo de mal uso (acoplamiento, drift semantico o inconsistencia).
- Debe quedar trazable con `Mapa de Contexto` y `Contratos de Integracion`.
