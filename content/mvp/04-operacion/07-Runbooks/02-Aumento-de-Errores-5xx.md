---
title: "Aumento de Errores 5xx"
linkTitle: "2. Aumento de Errores 5xx"
weight: 2
url: "/mvp/operacion/runbooks/aumento-errores-5xx/"
---

## Proposito
Reducir rapidamente errores 5xx y recuperar estabilidad del API.

## Senal de entrada
- Alerta de error rate 5xx por encima de umbral.
- Aumento sostenido de excepciones no controladas.

## Impacto esperado
Fallas intermitentes o continuas de operaciones criticas.

## Diagnostico inicial
- Identificar endpoint y servicio origen.
- Separar errores por tipo: timeout, dependencia, validacion interna.
- Revisar correlacion con release reciente.

## Contencion
- Aplicar rollback si coincide con cambio reciente.
- Ajustar circuit breaker/retry para evitar cascada.
- Limitar trafico de endpoint no critico si aplica.

## Recuperacion
- Corregir configuracion o dependencia degradada.
- Reprocesar operaciones idempotentes fallidas si aplica.
- Confirmar disminucion sostenida de 5xx.

## Verificacion posterior
- Error rate bajo umbral durante dos ventanas.
- Sin crecimiento de colas de reintento.

## Escalamiento
Sev 2 o Sev 1 segun impacto en checkout/pedido.

## Evidencia a registrar
- panel de error rate
- logs estructurados con traceId
- decision de rollback o fix forward

## Artefactos relacionados
- 06-SLI-SLO-y-Alertas
- 04-Estrategia-de-Releases-y-Deployments
