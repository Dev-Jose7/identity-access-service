---
title: "Fallos de Autenticacion"
linkTitle: "3. Fallos de Autenticacion"
weight: 3
url: "/mvp/operacion/runbooks/fallos-autenticacion/"
---

## Proposito
Restaurar autenticacion/autorizacion sin degradar aislamiento tenant.

## Senal de entrada
- Incremento de 401/403 anomalo.
- Errores de validacion de token o JWKS.

## Impacto esperado
Usuarios sin acceso a operaciones permitidas.

## Diagnostico inicial
- Validar disponibilidad de identity-access-service.
- Confirmar vigencia de llaves publicas JWKS y cache.
- Revisar reloj/sincronizacion y expiracion de token.

## Contencion
- Renovar cache de llaves en consumidores.
- Restaurar endpoint JWKS si presenta caida.
- Aplicar rollback de cambios de seguridad recientes.

## Recuperacion
- Corregir emision/validacion de token.
- Revalidar roles base y contexto principal.

## Verificacion posterior
- Tasa de login normalizada.
- 401/403 regresan a baseline esperado.

## Escalamiento
Sev 1 si bloqueo general de acceso en prod.

## Evidencia a registrar
- logs de autenticacion
- estado de JWKS
- trazas de solicitudes fallidas

## Artefactos relacionados
- 11-Seguridad-Operacional/03-Eventos-de-Seguridad
- identity-access-service/security/01-Arquitectura-de-Seguridad
