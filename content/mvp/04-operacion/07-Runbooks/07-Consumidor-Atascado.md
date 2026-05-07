---
title: "Consumidor Atascado"
linkTitle: "7. Consumidor Atascado"
weight: 7
url: "/mvp/operacion/runbooks/consumidor-atascado/"
---

## Proposito
Recuperar consumidor de eventos con lag creciente o throughput nulo.

## Senal de entrada
- Lag de consumidor creciendo sin drenaje.
- Throughput cercano a cero.

## Impacto esperado
Atraso de proyecciones y sincronizacion entre servicios.

## Diagnostico inicial
- Revisar estado de particiones y offsets.
- Verificar dependencia externa bloqueando procesamiento.

## Contencion
- Escalar replicas de consumidor si aplica.
- Reducir carga no critica.

## Recuperacion
- Reiniciar consumidor con control de offset.
- Corregir error de deserializacion o contrato.

## Verificacion posterior
- Lag decreciente sostenido.
- Reanudacion de eventos derivados.

## Escalamiento
Sev 2; Sev 1 si afecta checkout/pedido.

## Evidencia a registrar
- curva de lag
- offsets recuperados
- acciones de remediacion

## Artefactos relacionados
- 09-Eventos-y-DLQ/01-Operacion-del-Broker
- 09-Eventos-y-DLQ/03-Lag-Reintentos-e-Idempotencia
