---
title: "DLQ y Reprocesos"
linkTitle: "9.2 DLQ y Reprocesos"
weight: 2
url: "/mvp/operacion/eventos-dlq/dlq-reprocesos/"
---

## Objetivo
Definir cuando enviar mensajes a DLQ y como reprocesarlos sin romper
idempotencia ni semantica del dominio.

## Criterios de enrutamiento a DLQ
- payload invalido/no compatible con contrato vigente;
- error no recuperable de negocio en consumidor;
- agotamiento de reintentos transitorios.

## Criterios de reproceso
- existe causa raiz identificada y corregida;
- mensaje mapeable a contrato vigente;
- impacto de replay evaluado y idempotencia confirmada;
- ventana de reproceso aprobada por responsable del servicio.

## Procedimiento de reproceso
1. Congelar nuevas promociones si el incidente es activo.
2. Clasificar lote (`reprocesable`/`descartable`).
3. Reinyectar mensajes controladamente por lote.
4. Verificar dedupe, side effects y convergencia.
5. Registrar evidencia de resultado y cierre.

## Criterios de descarte
- evento corrupto sin recuperacion confiable;
- semantica obsoleta fuera de ventana de convivencia;
- duplicado confirmado sin side effects pendientes.
