---
title: "Reserva Inconsistente"
linkTitle: "5. Reserva Inconsistente"
weight: 5
url: "/mvp/operacion/runbooks/reserva-inconsistente/"
---

## Proposito
Corregir divergencias entre reserva de inventario y estado de pedido.

## Senal de entrada
- Reserva activa sin pedido asociado.
- Pedido confirmado sin reserva valida.

## Impacto esperado
Sobreventa o bloqueo indebido de stock.

## Diagnostico inicial
- Revisar correlacion orderId/reservationId.
- Verificar eventos publicados/consumidos y dedupe.
- Confirmar jobs de expiracion de reservas.

## Contencion
- Congelar temporalmente SKU afectado en checkout.
- Evitar nuevas confirmaciones sobre reservas dudosas.

## Recuperacion
- Ejecutar reconciliacion controlada por lote.
- Reemitir evento o reprocesar desde DLQ si corresponde.

## Verificacion posterior
- Consistencia entre stock disponible y reservas.
- Sin nuevos casos anomalos en ventana posterior.

## Escalamiento
Sev 2, o Sev 1 si afecta multiples SKU de alta demanda.

## Evidencia a registrar
- reporte de reconciliacion
- eventos reprocesados
- decision funcional aplicada

## Artefactos relacionados
- 09-Eventos-y-DLQ/02-DLQ-y-Reprocesos
- inventory-service/runtime/03-Casos-de-Uso-en-Ejecucion
