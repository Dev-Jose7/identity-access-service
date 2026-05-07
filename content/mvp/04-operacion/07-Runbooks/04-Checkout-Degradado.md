---
title: "Checkout Degradado"
linkTitle: "4. Checkout Degradado"
weight: 4
url: "/mvp/operacion/runbooks/checkout-degradado/"
---

## Proposito
Recuperar el flujo de checkout minimizando perdida de conversion.

## Senal de entrada
- Latencia alta o errores en checkout.
- Fallas en validacion de stock, direccion o pago manual.

## Senales por etapa del checkout
| Etapa | Senal tipica | Impacto |
|---|---|---|
| validacion regional | `configuracion_pais_no_disponible` | bloqueo por pais |
| validacion stock/reserva | `stock_insuficiente` o timeout de reserva | checkout inconcluso |
| creacion pedido | error 5xx o conflicto de checkout | pedido no creado |
| pago manual | rechazo o timeout en confirmacion | pedido pendiente/error |

## Impacto esperado
Pedidos no creados o checkout inconcluso.

## Diagnostico inicial
- Correlacionar fallas entre order, inventory, catalog y directory.
- Identificar paso exacto del flujo afectado.

## Decision operativa (si X entonces Y)
| Si | Entonces |
|---|---|
| falla concentrada en stock/reserva | priorizar diagnostico `inventory` + `order` |
| error regional canonico elevado | mantener bloqueo y corregir politica regional |
| latencia generalizada sin error funcional | activar degradacion controlada y monitoreo reforzado |
| fallo tras release reciente | evaluar rollback del servicio involucrado |
| falla aparece solo en entorno compose de integracion | revisar dependencias levantadas y configuracion de stack local/dev/qa |

## Contencion
- Priorizar trafico de checkout frente a operaciones no criticas.
- Aplicar fallback funcional permitido por baseline (no global por pais).

## Criterio de contencion cumplida
- tasa de error de checkout deja de crecer;
- paso degradado identificado y encapsulado;
- impacto sobre pedidos nuevos reducido.

## Recuperacion
- Corregir dependencia degradada.
- Reintentar operaciones idempotentes fallidas.

## Verificacion posterior
- Checkout feliz ejecuta dentro de SLO base.
- Tasa de exito estabilizada.

## Checklist de cierre
| Item | Criterio |
|---|---|
| flujo feliz | checkout completo sin errores criticos |
| reserva/pedido | consistencia entre reserva y pedido confirmada |
| regionalizacion | sin errores anormales por pais afectado |
| evidencia | trazas e2e + metricas + decision final registradas |

## Escalamiento
Sev 1 en prod si afecta conversion general.

## Evidencia a registrar
- trazas e2e de checkout
- metricas de conversion/errores
- acciones de mitigacion

## Artefactos relacionados
- 03-calidad/06-Evidencias/02-Matriz-de-Resultados-Esperados
- 06-SLI-SLO-y-Alertas
