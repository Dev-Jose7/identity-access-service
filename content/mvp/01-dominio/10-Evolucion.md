---
title: "Evolucion"
linkTitle: "10. Evolucion"
weight: 10
url: "/mvp/dominio/evolucion/"
---

## Marco de evolucion
_Responde: para que existe esta vista de evolucion y que cambios del dominio gobierna sin romper su baseline semantico._

Gobernar como cambia el dominio de ArkaB2B sin perder coherencia
conceptual: que cambios son compatibles, cuando existe ruptura
semantica, que decisiones ya estan cerradas y que deuda conceptual se
acepta explicitamente en `MVP`.

## Compatibilidad semantica
_Responde: que tipos de cambios son compatibles y cuales rompen el significado actual del dominio._
Cambios compatibles:
- agregar un estado no terminal con transicion explicita y sin cambiar el significado de los estados existentes.
- agregar un error nuevo sin redefinir errores ya publicados.
- ampliar payload de evento o contrato con campos opcionales.
- agregar una nueva proyeccion en `reporting` sin convertirla en verdad transaccional.

Cambios breaking:
- permitir pedido sin reserva valida o sin politica regional vigente.
- cambiar el significado de `vendible`, `disponible`, `OrderCreated` o `OrderConfirmed`.
- relajar el aislamiento por tenant o mover ownership de verdad entre BCs sin decision formal.
- convertir `notification` o `reporting` en dueños de decisiones comerciales.

## Versionado conceptual
_Responde: cuando un cambio exige solo registro, cuando exige decision formal y cuando obliga a nueva version conceptual._
| Tipo de cambio | Regla de versionado | Ejemplo |
|---|---|---|
| Ajuste no breaking | no exige nueva version conceptual del dominio; basta changelog | nuevo error recuperable en `notification` |
| Cambio relevante pero compatible | registrar decision y fecha efectiva | nuevo evento opcional de inventario |
| Cambio breaking semantico | requiere nueva version conceptual o nueva ventana de convivencia | redefinir el ciclo de `Order` |
| Cambio estructural de frontera | requiere decision explicita y revalidacion de trazabilidad | fusionar o dividir BCs |

## Baseline vigente
_Responde: que decisiones del dominio se consideran activas y cuales son sus efectos obligatorios en `MVP`._
| Decision | Justificacion | Impacto activo en `MVP` |
|---|---|---|
| `catalog` decide vendible/precio y `inventory` decide disponibilidad | evita mezclar verdad comercial con stock real | reduce riesgo de sobreventa y drift de precio |
| `order` crea pedido en `PENDING_APPROVAL` y solo luego confirma | separa materializacion del pedido de la confirmacion comercial final | evita ambiguedad entre `OrderCreated` y `OrderConfirmed` |
| `notification` es no bloqueante | la entrega de mensajes no debe revertir hechos core | desacople operativo del flujo comercial |
| `reporting` es una proyeccion | las vistas derivadas no gobiernan mutaciones del negocio | mantiene limpia la frontera transaccional |
| regionalizacion por `countryCode` vive en `directory` | una sola verdad para moneda, corte y retencion por organizacion | evita fallback global silencioso |
| reserva en MVP es todo-o-nada | simplifica checkout y enforcement anti-sobreventa | no existen reservas parciales en el baseline |

## Historial semantico
_Responde: que cambios relevantes se consolidaron en el dominio y como alteraron su baseline._
| Fecha | Cambio | Impacto |
|---|---|---|
| 2026-03-11 | Se consolida baseline del dominio con BCs, lenguaje, reglas, eventos y contratos. | primer cierre coherente del pilar |
| 2026-03-11 | Se fija que `reporting` no es transaccional y `notification` no bloquea core. | reduce riesgo de sobre-modelado en BCs de soporte |
| 2026-03-13 | Se reorganiza el pilar en orden estrategica -> semantica -> reglas -> comportamiento -> integracion -> tactica -> trazabilidad -> evolucion. | mejora legibilidad y cierre metodologico |

## Ventanas de convivencia
_Responde: bajo que condiciones pueden convivir significados o versiones sin romper ownership ni coherencia semantica._
- Si un evento o contrato cambia de forma breaking, debe convivir con la version previa durante la ventana acordada del ciclo.
- Si un estado o termino cambia de significado, el cambio no entra al baseline sin actualizar `Lenguaje Ubicuo`, `Comportamiento Global`, `Eventos de Dominio` y `Contratos de Integracion`.
- Ninguna ventana de convivencia permite que dos BCs distintos sean dueños simultaneos de la misma verdad.

## Capacidades reservadas y evolucion posterior
_Responde: que capacidades quedan fuera del baseline congelado de `MVP` y como se clasifican sin abrir semantica del dominio vigente._
| Tema | Clasificacion | Motivo de clasificacion |
|---|---|---|
| fulfillment (`READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED`) | reservado | no forma parte del baseline operativo de `MVP` |
| pago no manual / conciliacion avanzada | fuera de alcance `MVP` | el MVP solo requiere registro manual e idempotente |
| externos core (`PSP`, `ERP`, identidad externa`) | evolucion posterior | aun no existe requerimiento validado para incorporarlos al dominio |
| expansion de politicas regionales | evolucion posterior | `countryCode`, moneda, corte y retencion bastan para `MVP` |
| granularidad futura de reportes | evolucion posterior | la prioridad actual es cierre semanal por tenant/pais, no analitica avanzada |

## Criterio de cambio
_Responde: que condiciones debe cumplir una propuesta antes de alterar el dominio aprobado._
- Si una propuesta contradice las reglas, invariantes o ownership actuales, el cambio no se aplica sin decision formal.
- Si una propuesta no puede ubicarse en un BC owner claro, el dominio aun no esta listo para absorberla.
- Si una propuesta afecta lenguaje, eventos y contratos a la vez, debe tratarse como cambio semantico mayor.
