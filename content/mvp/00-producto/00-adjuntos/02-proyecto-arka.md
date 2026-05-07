---
title: "Adjunto 02 Proyecto Arka"
weight: 2
url: "/mvp/producto/adjuntos/proyecto-arka/"
---

# PROYECTO ARKA
**Alejandro Marcano**

---

# Definición General del Proyecto

Desarrollar una arquitectura de microservicios para una plataforma e-commerce, que permita gestionar de forma eficiente:

- Órdenes
- Inventario
- Productos
- Proveedores

Garantizando:

- Escalabilidad
- Integración con AWS
- Experiencia de usuario optimizada

---

# Módulos del Proyecto

## 1. Gestión de Órdenes

Orquestación del proceso de creación de órdenes.

- Uso del patrón **Saga**
- Eventos con **SQS**
- Funciones **Lambda**
- Coordinación de:
    - Inventario
    - Envío
    - Notificación

---

## 2. Carrito

Gestión del carrito de compras:

- Detección de carritos abandonados
- Envío de notificaciones

---

## 3. Catálogo

- Visualización del catálogo mediante integración con microservicios
- Uso de **Spring Cloud**
- Uso de **BFF (Backend for Frontend)** para adaptar información por plataforma

---

## 4. Shipping

Migración del servicio de envíos desde arquitectura monolítica hacia microservicios usando el patrón:

- **Strangler Fig**

---

## 5. Proveedores

- Administración de proveedores
- Gestión de almacenes
- Gestión de inventario
- Generación automática de órdenes de compra
- Actualización de stock

---

# Patrones de Microservicio

- API Gateway
- Registro de Servicios
- Circuit Breaker
- Saga
- Event Sourcing
- Strangler Fig
- CQS (Command Query Separation)
- Composition

---

# Servicios de AWS Utilizados

- S3
- SQS
- Lambda
- DynamoDB
- SES
- EventBridge
- DocumentDB
- SNS
- RDS
- EC2

---

# Actividades del Proyecto

---

## Actividad 1

### Objetivo principal:
Implementar sistema básico de órdenes.

### Estructuras:
- EC2
- RDS

### Opcional:
- API Gateway

### Componentes:
- Órdenes (RDS)
- Inventario (RDS)
- API Gateway

Funcionalidad:
- Recibir una orden
- Verificar disponibilidad

---

## Actividad 2

### Objetivo principal:
Implementar patrón Saga para dividir el proceso de creación de órdenes.

### Estructuras:
- SNS
- SQS
- Lambda

### Opcional:
- Patrón Saga

### Flujo:
1. Orden creada
2. Actualizar inventario
3. Crear orden de shipping
4. Orden completada

---

## Actividad 3

### Objetivo principal:
Finalizar proceso de compra y notificaciones.

### Estructuras:
- S3
- SES

### Funcionalidades:
- Envío de correos
- Estado de orden
- Plantillas de correo

---

## Actividad 4

### Objetivo principal:
Implementar servicios de carrito.

### Estructuras:
- Spring Cloud Services con AWS

### Funcionalidades:
- Agregar al carrito
- Editar carrito
- Eliminar del carrito
- Activación de servicios del catálogo

---

## Actividad 5

### Objetivo principal:
Despliegue de BFF y recomendaciones.

### Estructuras:
- BFF
- DocumentDB

---

## Actividad 6

### Objetivo principal:
Aplicar patrón Strangler Fig.

Separar la calculadora de shipping en múltiples microservicios.

---

## Actividad 7

### Objetivo principal:
Refactorización general.

- Aplicar estrategias de refactor
- Completar servicios restantes

---

## Actividad 8

### Objetivo principal:
Diseñar sistema para proveedores y órdenes de compra.

### Estructuras:
- Creación de diagramas

Nota: Solo propuesta de diseño, sin implementación.

---

## Actividad 9

### Objetivo principal:
Generar servicios adicionales.

### Estructuras:
- CronJob
- EventBridge

### Funcionalidades:
- Servicio de notificaciones
- Servicio de reportes

---

# DevOps y Calidad

## Actividad DevOps 1
Finalizar proyecto optimizando tareas y calidad del código.

Tareas:
- Jira
- Pair Programming
- Calidad de código

---

## Actividad DevOps 2
- Pair Programming
- Code Review

---

## Actividad DevOps 3
Generación de Pipeline EC2:

Develop → Push → Build → Deploy

---

## Actividad DevOps 4
Pipeline Lambda:

Develop → Push → Test → Lint → Build → Deploy

---

## Actividad DevOps 5
Generación de archivos de infraestructura.

---

## Actividad DevOps 6 – Observabilidad

Objetivos:

- Generación de alarmas
- Creación de paneles de control
- Análisis de logs
- Identificación de cuellos de botella
- Resiliencia
- Alertas de borde

---

# Hitos de Entrega

## AWS

- Actividad requerida 1: Actividad 1 a 3 (Sistema de Órdenes)
- Actividad requerida 2: Actividad 4 a 6 (Sistemas Cloud)
- Actividad requerida 3: Actividad 7 a 9 (Microservicios avanzados)

---

## DevOps

- Actividad requerida 1: Actividad 1 a 4 (Inicialización DevOps)
- Actividad requerida 2: Actividad 5 (Infraestructura)
- Actividad requerida 3: Actividad 6 (Observabilidad)

---

# Resumen de Patrones Utilizados

- API Gateway
- Registro de Servicios
- Circuit Breaker
- Saga
- Event Sourcing
- Strangler Fig
- CQS
- Composition  
