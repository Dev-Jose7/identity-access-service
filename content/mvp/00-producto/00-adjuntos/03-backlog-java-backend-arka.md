---
title: "Adjunto 03 Backlog Arka"
weight: 3
url: "/mvp/producto/adjuntos/backlog-java-backend-arka/"
---

# Backlog del Proyecto Java Backend - Arka

## 1. Introducción

Este backlog contiene las historias de usuario que se desarrollarán a lo largo del curso para la implementación del backend del sistema de Arka.

Su objetivo es garantizar que la empresa pueda gestionar eficientemente su inventario, órdenes de compra y ventas, optimizando así sus operaciones.

---

## 2. Historias de Usuario

### Módulo 1: Gestión de Inventario y Abastecimiento

#### HU1 - Registrar productos en el sistema

**Como** administrador  
**Quiero** registrar nuevos productos con sus características  
**Para que** los clientes puedan comprarlos

**Criterios de aceptación:**

- Se debe permitir la carga de:
    - nombre
    - descripción
    - precio
    - stock
    - categoría
- Validaciones de datos requeridos
- Mensaje de confirmación tras el registro exitoso

---

#### HU2 - Actualizar stock de productos

**Como** administrador  
**Quiero** actualizar la cantidad de productos en stock  
**Para** evitar sobreventas

**Criterios de aceptación:**

- El sistema debe permitir modificar el stock de un producto
- No se deben permitir valores negativos
- Historial de cambios en el stock

---

#### HU3 - Generar reportes de productos por abastecer

**Como** administrador  
**Quiero** recibir reportes de productos con bajo stock  
**Para** tomar decisiones de abastecimiento

**Criterios de aceptación:**

- El reporte debe generarse automáticamente cada semana
- Debe incluir productos con stock menor a un umbral configurable
- Exportación en formato CSV o PDF

---

### Módulo 2: Gestión de Órdenes de Compra

#### HU4 - Registrar una orden de compra

**Como** cliente  
**Quiero** poder registrar una orden de compra con múltiples productos  
**Para** realizar mi pedido

**Criterios de aceptación:**

- Se debe validar la disponibilidad del stock
- Registro de fecha y detalles del pedido
- Mensaje de confirmación con resumen del pedido

---

#### HU5 - Modificar una orden de compra

**Como** cliente  
**Quiero** modificar mi pedido antes de su confirmación  
**Para** corregir errores o agregar productos

**Criterios de aceptación:**

- Solo se pueden modificar pedidos en estado pendiente
- Se debe actualizar el stock en caso de eliminación de productos

---

#### HU6 - Notificación de cambio de estado de pedido

**Como** cliente  
**Quiero** recibir notificaciones sobre el estado de mi pedido  
**Para** estar informado de su progreso

**Criterios de aceptación:**

- Notificación por correo o en la plataforma
- Estados posibles:
    - pendiente
    - confirmado
    - en despacho
    - entregado

---

### Módulo 3: Reportes y Análisis de Ventas

#### HU7 - Generar reportes de ventas semanales

**Como** administrador  
**Quiero** generar reportes semanales de ventas  
**Para** analizar el rendimiento del negocio

**Criterios de aceptación:**

- El reporte debe incluir:
    - total de ventas
    - productos más vendidos
    - clientes más frecuentes
- Exportación en formato CSV o PDF

---

#### HU8 - Identificar carritos abandonados

**Como** administrador  
**Quiero** visualizar los carritos abandonados  
**Para** contactar a los clientes y recuperar ventas

**Criterios de aceptación:**

- Listado de carritos abandonados con fecha y productos
- Opción de enviar recordatorio por correo al cliente

---

## 3. Priorización

Se recomienda priorizar las historias de usuario en el siguiente orden:

1. HU1
2. HU2
3. HU4
4. HU5
5. HU6
6. HU3
7. HU7
8. HU8

---

## 4. Conclusión

Este backlog proporciona una estructura clara para el desarrollo del backend del sistema de Arka.

Con estas historias de usuario y su priorización, se garantizará la entrega de un producto funcional que atienda las necesidades principales del negocio.

A continuación, se desarrollarán los diagramas gráficos requeridos:

- Diagrama de base de datos (estructura de información)
- Diagrama de infraestructura (despliegue y componentes)
- Diagrama de arquitectura (modelo de capas y servicios)

---

# Diagramas del Sistema Arka

## Diagrama de Base de Datos

Representa las entidades principales y sus relaciones:

- Productos
- Clientes
- Órdenes
- Carritos de compras
- Detalles de pedidos

---

## Diagrama de Infraestructura

Muestra la interacción entre los diferentes componentes del sistema:

- Frontend
- API Gateway
- Servicios de Backend
- Base de Datos
- Proveedores Externos

---

## Diagrama de Arquitectura

Representa las diferentes capas del sistema:

### 1. Capa de Presentación
- Frontend

### 2. Capa de Aplicación
- API Gateway
- Servicios Backend:
    - Autenticación
    - Inventario
    - Pedidos
    - Notificaciones

### 3. Capa de Datos
- Base de Datos
- Conexión con Proveedores Externos

---

# Plan de Acción

## 1. Entregar una Base de Datos Lista para Usar

### Opción 1: Archivo SQL predefinido

Crear un script SQL con:

- Estructuras de tablas
- Relaciones
- Datos de prueba

Compatible con PostgreSQL o MySQL (AWS RDS)

### Opción 2: Base de Datos en AWS RDS

- Configurar una instancia de Amazon RDS con la estructura ya creada
- Proporcionar credenciales temporales

---

## 2. Explicar Cómo Se Relaciona con Microservicios

- Crear guía o diagrama explicando cómo cada microservicio interactúa con la base de datos

### Estrategias posibles:

**Opción 1:**  
Base de datos única con esquemas separados por servicio

**Opción 2:**  
Cada microservicio tiene su propia base de datos y se comunican vía APIs

---

## 3. Proporcionar Código Base y Ejemplos

Subir un repositorio en GitHub con un microservicio base que incluya:

- Conexión a la base de datos
- Operaciones CRUD
- Ejemplo con Spring Boot y AWS RDS

---

## 4. Sesión de Re-alineación

- Organizar sesión en vivo
- Explicar conexión con AWS
- Resolver dudas

---

# Configuración de Conexión a Base de Datos (Spring Boot + AWS)

```java
package com.arka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;

@Configuration
@EnableJpaRepositories(basePackages = "com.arka.repository")
public class DatabaseConfig {

    @Value("${aws.db.url}")
    private String dbUrl;

    @Value("${aws.db.username}")
    private String dbUsername;

    @Value("${aws.db.password}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setPackagesToScan("com.arka.model");
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return factoryBean;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
```
