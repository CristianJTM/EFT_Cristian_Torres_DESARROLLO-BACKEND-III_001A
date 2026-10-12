# Banco XYZ — Microservicios, OAuth 2.0, Resiliencia, Kafka y Docker

## 1. Descripción del proyecto

Banco XYZ es una solución bancaria desarrollada con **Java 21, Spring Boot y Spring Cloud**, basada en una arquitectura de microservicios que separa las responsabilidades de gestión de cuentas, consulta de información financiera, ejecución de operaciones bancarias, auditoría y procesamiento batch.

Durante la evolución del proyecto, el antiguo microservicio `backend-core` fue eliminado y sus responsabilidades se distribuyeron entre tres servicios independientes:

- **`account-service`:** consulta de cuentas y transacciones.
- **`account-management-service`:** creación, consulta, actualización y eliminación de cuentas.
- **`payment-service`:** ejecución de operaciones de retiro y publicación de eventos de transacciones.

Como parte de esta refactorización, se incorporó configuración de seguridad en los tres nuevos microservicios. Los BFF mantienen sus propias configuraciones de seguridad y continúan actuando como puntos de acceso para sus respectivos clientes.

El proyecto integra:

- Arquitectura de microservicios.
- Configuración centralizada con Spring Cloud Config.
- Descubrimiento de servicios con Netflix Eureka.
- Comunicación entre servicios mediante OpenFeign y Spring Cloud LoadBalancer.
- Autenticación y autorización mediante OAuth 2.0 y JWT.
- Tolerancia a fallos mediante Resilience4j.
- Comunicación asíncrona mediante Apache Kafka.
- Auditoría de eventos de cuentas y transacciones.
- Procesamiento de datos mediante Spring Batch.
- Persistencia con MySQL 8.4.
- Contenedorización con Docker.
- Orquestación con Docker Compose.
- Infraestructura Kafka desplegada de manera independiente en Amazon EC2.

## 2. Objetivos

La solución busca:

- Separar las responsabilidades del negocio en microservicios independientes.
- Gestionar las cuentas y consultar su información financiera.
- Ejecutar operaciones bancarias con validaciones de negocio.
- Proteger los endpoints mediante mecanismos de autenticación y autorización.
- Facilitar la comunicación entre los servicios mediante descubrimiento dinámico.
- Incorporar mecanismos de resiliencia ante fallos temporales.
- Desacoplar la publicación y el consumo de eventos mediante Kafka.
- Registrar eventos de cuentas y transacciones en el servicio de auditoría.
- Procesar archivos financieros mediante Spring Batch.
- Facilitar el despliegue y la ejecución mediante contenedores Docker.

## 3. Arquitectura de la solución

La infraestructura se divide en dos entornos:

1. **Docker Compose local:** contiene los microservicios, MySQL y los componentes de infraestructura Spring Cloud.
2. **Amazon EC2:** contiene el clúster Kafka y los componentes asociados a su ejecución.

Esta separación permite mantener Kafka independiente de los servicios de negocio.

### 3.1 Diagrama general

```text
                         CLIENTES
               ┌──────────┼──────────┐
               │          │          │
               ▼          ▼          ▼
           BFF Web    BFF Mobile   BFF ATM
            :8082       :8083       :8084
               │          │          │
               └──────────┼──────────┘
                          │
                    OpenFeign
                          │
          ┌───────────────┼────────────────┐
          │               │                │
          ▼               ▼                ▼
   Account Service  Account Management  Payment Service
       :8081             :8089             :8088
          │               │                │
          └───────────────┼────────────────┘
                          │
                          ▼
                       MySQL
                        :3307

    ┌──────────────────────────────────────────────┐
    │          Infraestructura Spring Cloud        │
    │                                              │
    │   Config Server :8888   Discovery :8761      │
    │             Auth Server :9000                │
    └──────────────────────────────────────────────┘

                  EVENTOS ASÍNCRONOS
                          │
                          ▼
                 Kafka Cluster - EC2
                  ┌────────┴────────┐
                  │                 │
                  ▼                 ▼
          transacciones-       cuentas-eventos
           bancarias                │
                  │                 │
                  └────────┬────────┘
                           ▼
                    Auditoria Service
                         :8086

                    PROCESAMIENTO BATCH
                           │
                           ▼
                      Banco Batch
                         :8080
                           │
                           ▼
                          MySQL
```

El diagrama representa las principales relaciones funcionales. La comunicación efectiva de cada servicio depende de las configuraciones definidas en Docker Compose, Eureka y los archivos de propiedades.

### 3.2 Entorno local

| Componente | Puerto |
|---|---:|
| Banco Batch | `8080` |
| Account Service | `8081` |
| BFF Web | `8082` |
| BFF Mobile | `8083` |
| BFF ATM | `8084` |
| Auditoria Service | `8086` |
| Payment Service | `8088` |
| Account Management Service | `8089` |
| Auth Server | `9000` |
| Discovery Server / Eureka | `8761` |
| Config Server | `8888` |
| MySQL, puerto del host | `3307` |

Dentro de Docker Compose, los servicios se comunican mediante los nombres DNS definidos en la configuración de los contenedores. Por ejemplo, la conexión de MySQL utiliza el nombre de servicio `mysql` y el puerto interno `3306`.

### 3.3 Infraestructura Kafka en Amazon EC2

El clúster Kafka se ejecuta de forma independiente del entorno local.

Componentes de infraestructura:

- Tres brokers Kafka.
- ZooKeeper, según la configuración actual del despliegue.
- Kafka UI.
- Servicio de inicialización de tópicos.

Puertos externos configurados para Kafka:

```text
29092
39092
49092
```

Puerto de Kafka UI:

```text
8090
```

Los microservicios utilizan la variable de entorno `KAFKA_BOOTSTRAP_SERVERS` para definir las direcciones de los brokers.

Ejemplo de configuración:

```text
KAFKA_BOOTSTRAP_SERVERS=<BROKER_1>:29092,<BROKER_2>:39092,<BROKER_3>:49092
```

Los valores deben corresponder a las direcciones anunciadas por los brokers y ser accesibles desde los contenedores que se conectan a Kafka.

## 4. Estructura del proyecto

El repositorio está organizado por microservicio. Cada módulo mantiene sus propias clases Java, configuración y recursos.

```text
Banco-XYZ/
├── account-service/
├── account-management-service/
├── payment-service/
├── auditoria-service/
├── auth-server/
├── config-server/
├── discovery-server/
├── bff-web/
├── bff-mobile/
├── bff-atm/
├── banco-batch/
├── docker-compose.yaml
├── docker-compose.kafka.yaml
└── README.md
```

La estructura anterior es una vista resumida de los módulos principales. Los archivos `pom.xml`, `Dockerfile` y otros archivos de despliegue deben mantenerse según la configuración real de cada módulo.

### 4.1 Organización interna

Los módulos utilizan paquetes con responsabilidades diferenciadas, según las necesidades de cada servicio:

| Paquete | Responsabilidad |
|---|---|
| `config` | Configuración de seguridad, Kafka, clientes y otros componentes. |
| `controllers` / `controller` | Exposición de endpoints HTTP. |
| `clients` | Clientes para comunicación con otros microservicios. |
| `dtos` | Objetos de transferencia de datos y contratos de API. |
| `exceptions` / `exception` | Excepciones y manejo de errores. |
| `kafka` | Productores, consumidores y contratos de eventos. |
| `model` | Entidades y modelos de negocio. |
| `repositories` / `repository` | Acceso a los datos persistidos. |
| `services` / `service` | Lógica de negocio y coordinación de operaciones. |
| `processor` | Validación y transformación de registros batch. |
| `writer` | Escritura de resultados de los procesos batch. |
| `listener` | Eventos de ejecución y tratamiento de registros omitidos. |
| `tasklet` | Tareas específicas dentro de los jobs batch. |

No todos los módulos contienen todos estos paquetes. Su estructura depende de la responsabilidad de cada servicio.

## 5. Microservicios

### 5.1 Account Service

**Puerto:** `8081`

Se encarga de consultar información de cuentas y transacciones bancarias.

Responsabilidades principales:

- Consultar cuentas.
- Consultar transacciones por identificador.
- Consultar transacciones asociadas a una cuenta.
- Exponer DTOs para la comunicación con los BFF y otros clientes autorizados.
- Acceder a los datos persistidos mediante repositorios.
- Validar las solicitudes conforme a su configuración de seguridad.

Clases principales:

```text
account-service/
└── src/main/java/com/bancoxyz/account_service/
    ├── config/
    │   └── SecurityConfig.java
    ├── controllers/
    │   ├── CuentaController.java
    │   └── TransaccionController.java
    ├── dtos/
    │   ├── CuentaDTO.java
    │   └── TransaccionDTO.java
    ├── exceptions/
    ├── model/
    ├── repositories/
    └── services/
```

### 5.2 Account Management Service

**Puerto:** `8089`

Centraliza las operaciones de administración de cuentas.

Responsabilidades principales:

- Crear cuentas.
- Listar cuentas.
- Consultar una cuenta por identificador.
- Actualizar cuentas.
- Eliminar cuentas.
- Validar las solicitudes recibidas.
- Publicar eventos de cuenta mediante Kafka.
- Aplicar la configuración de seguridad del servicio.

Clases principales:

```text
account-management-service/
└── src/main/java/com/bancoxyz/accountmanagement/
    ├── config/
    │   └── SecurityConfig.java
    ├── controllers/
    │   └── CuentaController.java
    ├── dtos/
    │   ├── ActualizarCuentaRequest.java
    │   ├── CrearCuentaRequest.java
    │   └── CuentaResponse.java
    ├── exceptions/
    ├── kafka/
    │   ├── CuentaEvento.java
    │   ├── CuentaProducer.java
    │   └── KafkaConfig.java
    ├── model/
    ├── repositories/
    └── services/
```

### 5.3 Payment Service

**Puerto:** `8088`

Se encarga de ejecutar operaciones de pago y retiro de acuerdo con las reglas de negocio implementadas.

Responsabilidades principales:

- Procesar retiros.
- Validar los montos.
- Validar la existencia de la cuenta.
- Comprobar que exista saldo suficiente para las operaciones que lo requieran.
- Persistir las transacciones.
- Publicar eventos de transacciones mediante Kafka.
- Aplicar la configuración de seguridad del servicio.

Clases principales:

```text
payment-service/
└── src/main/java/com/bancoxyz/payment_service/
    ├── config/
    │   └── SecurityConfig.java
    ├── controllers/
    │   └── CuentaController.java
    ├── dtos/
    ├── exceptions/
    ├── kafka/
    │   ├── KafkaConfig.java
    │   ├── TransaccionEvento.java
    │   └── TransaccionProducer.java
    ├── model/
    ├── repositories/
    └── services/
```

### 5.4 BFF Web

**Puerto:** `8082`

Backend for Frontend para clientes web.

Coordina consultas de información financiera y operaciones de administración de cuentas mediante clientes separados:

- `AccountServiceClient`
- `AccountManagementClient`

También incluye servicios de resiliencia, DTOs específicos para la presentación web y manejo de excepciones.

### 5.5 BFF Mobile

**Puerto:** `8083`

Backend for Frontend orientado a clientes móviles.

Utiliza `AccountServiceClient` para consultar información de cuentas y transacciones. Expone DTOs adaptados a las necesidades del cliente móvil y aplica su configuración de seguridad y resiliencia.

### 5.6 BFF ATM

**Puerto:** `8084`

Backend for Frontend destinado a las operaciones de cajeros automáticos.

Utiliza clientes independientes para comunicarse con:

- `AccountServiceClient`
- `PaymentServiceClient`

Esta separación permite consultar información de cuentas y delegar la ejecución de retiros al servicio responsable de los pagos.

### 5.7 Auditoria Service

**Puerto:** `8086`

Consume eventos publicados por los servicios bancarios a través de Kafka.

Actualmente utiliza dos consumidores independientes:

- `CuentaConsumer`
- `TransaccionConsumer`

Cada consumidor dispone de una fábrica configurada para deserializar su tipo de evento correspondiente.

Tópicos consumidos:

```text
cuentas-eventos
transacciones-bancarias
```

Grupo de consumidores:

```text
auditoria-group
```

El servicio registra los eventos recibidos en sus logs. La estructura actual mostrada no incluye repositorios ni entidades de persistencia propios para almacenar el historial de auditoría en MySQL.

### 5.8 Auth Server

**Puerto:** `9000`

Implementa la función de Authorization Server mediante Spring Authorization Server.

Responsabilidades:

- Gestionar la emisión de tokens de acceso.
- Implementar la configuración de OAuth 2.0.
- Emitir tokens JWT conforme a la configuración del servidor.
- Publicar los metadatos necesarios para los servicios que validan los tokens.

### 5.9 Config Server

**Puerto:** `8888`

Centraliza la configuración de los microservicios mediante Spring Cloud Config Server.

### 5.10 Discovery Server

**Puerto:** `8761`

Implementa el descubrimiento de servicios mediante Netflix Eureka. Permite que los microservicios registrados puedan ser localizados dinámicamente por sus clientes.

### 5.11 Banco Batch

**Puerto:** `8080`

Ejecuta procesos batch mediante Spring Batch para importar, validar, transformar y persistir información financiera.

Los archivos de entrada utilizados en esta versión son:

```text
estados_financieros_anuales.csv
intereses_trimestrales.csv
movimientos_financieros_diarios.csv
```

Los procesos se organizan mediante jobs independientes y un job coordinador para ejecutar el proceso batch completo.

Entre los componentes se encuentran:

- Configuración de lectores y datos batch.
- Procesadores de validación y transformación.
- Writers para persistir resultados.
- Listeners para gestionar registros omitidos.
- Tasklets para tareas específicas.
- Repositorios para resultados, resúmenes y anomalías.
- Servicio para iniciar y coordinar ejecuciones batch.

Los nombres de los archivos deben coincidir con los configurados en la aplicación y estar disponibles en las rutas esperadas por los lectores.

## 6. Seguridad OAuth 2.0 y JWT

La arquitectura utiliza un Auth Server central y configuraciones de seguridad en los servicios que deben proteger sus endpoints.

La seguridad se incorporó a los tres servicios que reemplazaron las responsabilidades del antiguo `backend-core`:

- `account-service`
- `account-management-service`
- `payment-service`

Además, los tres BFF mantienen sus configuraciones de seguridad propias.

### 6.1 Flujo general

```text
Cliente
   │
   │ Solicita token de acceso
   ▼
Auth Server :9000
   │
   │ Emite JWT
   ▼
Cliente
   │
   │ Authorization: Bearer <token>
   ▼
BFF / Servicio protegido
   │
   │ Validación del token y autorización
   ▼
Endpoint solicitado
```

El Auth Server emite los tokens y los servicios configurados como OAuth2 Resource Server validan los tokens recibidos. La validación de JWT puede incluir la firma, las fechas de validez y el emisor, según la configuración del servicio.

La seguridad efectiva de cada endpoint depende de las reglas definidas en su `SecurityConfig.java`, incluidos los endpoints públicos y los scopes requeridos.

Referencia: [Spring Security — OAuth 2.0 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

### 6.2 Credenciales y configuración

Los valores utilizados en el entorno académico deben mantenerse fuera del código fuente cuando sea posible. Las credenciales, secretos y parámetros de conexión se pueden suministrar mediante variables de entorno o un gestor de secretos.

No se deben considerar las credenciales de demostración como adecuadas para un entorno productivo.

## 7. Comunicación entre microservicios

La comunicación síncrona se realiza mediante HTTP y OpenFeign.

Los BFF utilizan clientes separados para invocar las responsabilidades correspondientes:

```text
BFF Web
 ├── AccountServiceClient
 └── AccountManagementClient

BFF Mobile
 └── AccountServiceClient

BFF ATM
 ├── AccountServiceClient
 └── PaymentServiceClient
```

Eureka permite localizar los servicios registrados y Spring Cloud LoadBalancer participa en la resolución y balanceo de las solicitudes realizadas mediante clientes compatibles.

Los DTOs utilizados por los clientes representan los contratos de comunicación entre servicios. Cada módulo mantiene sus propias clases de DTO para evitar depender directamente de las clases internas de otro microservicio.

## 8. Resiliencia con Resilience4j

Los BFF utilizan Resilience4j para gestionar fallos temporales en las llamadas remotas.

Los mecanismos disponibles en la configuración incluyen:

- **Retry:** reintenta llamadas cuando se producen fallos que cumplen las condiciones configuradas.
- **Circuit Breaker:** limita llamadas a un servicio que presenta fallos reiterados.
- **Manejo de excepciones:** permite devolver respuestas controladas cuando una dependencia no está disponible.

La aplicación de cada mecanismo depende de las anotaciones y de los nombres de instancia definidos en la configuración del servicio.

En operaciones que modifican el estado, como retiros o creación de cuentas, los reintentos deben configurarse cuidadosamente para evitar duplicar operaciones. Una estrategia segura requiere considerar la idempotencia de la solicitud.

## 9. Comunicación asíncrona con Apache Kafka

Kafka desacopla la publicación de eventos de su procesamiento por parte de auditoría.

### 9.1 Eventos de cuenta

`account-management-service` publica eventos en:

```text
cuentas-eventos
```

El productor utiliza `CuentaProducer` y el contrato `CuentaEvento`.

Un evento de cuenta puede contener los siguientes campos:

```json
{
  "evento": "CuentaCreada",
  "cuentaId": 122,
  "saldo": 10000,
  "tipo": "ahorro",
  "fecha": "2026-10-12T01:20:16.134485574"
}
```

El ejemplo ilustra la estructura de un evento de creación de cuenta; los valores dependen de la operación realizada.

### 9.2 Eventos de transacción

`payment-service` publica eventos de transacciones en:

```text
transacciones-bancarias
```

El productor utiliza `TransaccionProducer` y el contrato `TransaccionEvento`.

Los eventos pueden incluir información de la operación, como el tipo de evento, la cuenta, el monto, la fecha y el saldo posterior.

### 9.3 Consumo de eventos

`auditoria-service` consume ambos tópicos mediante consumidores independientes:

```text
Kafka Cluster
     │
     ├── cuentas-eventos
     │       └── CuentaConsumer
     │
     └── transacciones-bancarias
             └── TransaccionConsumer
```

Ambos consumidores utilizan el grupo:

```text
auditoria-group
```

La configuración Kafka del servicio define una fábrica de consumidores para `CuentaEvento` y otra para `TransaccionEvento`, evitando que los mensajes de cuenta se intenten convertir al tipo de una transacción.

Los tópicos se crean mediante la configuración de la infraestructura Kafka. En el despliegue actual se utiliza un factor de replicación de tres y tres particiones por tópico.

Referencia: [Spring for Apache Kafka — Serialization, Deserialization and Message Conversion](https://docs.spring.io/spring-kafka/reference/kafka/serdes.html).

## 10. Procesamiento batch con Spring Batch

`banco-batch` procesa los archivos CSV de entrada mediante jobs configurados en Spring Batch.

### 10.1 Archivos de entrada

| Archivo | Finalidad |
|---|---|
| `estados_financieros_anuales.csv` | Procesamiento de información para estados financieros anuales. |
| `intereses_trimestrales.csv` | Procesamiento de información relacionada con el cálculo de intereses trimestrales. |
| `movimientos_financieros_diarios.csv` | Procesamiento de movimientos y transacciones financieras diarias. |

Los encabezados, tipos de datos, reglas de validación y rutas de los archivos dependen de la configuración de lectores y procesadores del proyecto.

### 10.2 Componentes batch

El módulo incorpora:

- `EstadosAnualesJobConfig`
- `InteresesJobConfig`
- `TransaccionesJobConfig`
- `ProcesoBatchCompletoJobConfig`
- `BatchDataConfig`
- `BatchTaskExecutorConfig`
- `BatchJobController`
- `BatchJobService`
- Procesadores específicos para los datos.
- Writers y repositorios para almacenar los resultados.
- Listeners para el tratamiento de registros omitidos.
- Un tasklet para el resumen de anomalías.

El flujo general de procesamiento es:

```text
Archivo CSV
    │
    ▼
ItemReader
    │
    ▼
ItemProcessor
    │
    ├── Validación
    ├── Transformación
    └── Detección de datos inválidos
    │
    ▼
ItemWriter
    │
    ▼
MySQL
```

Los registros inválidos pueden tratarse mediante las políticas de tolerancia a errores y los listeners definidos para cada job.

El módulo también incorpora componentes para registrar anomalías de transacciones, resultados procesados y resúmenes.

## 11. Persistencia con MySQL

La aplicación utiliza MySQL 8.4 como base de datos relacional.

Configuración del entorno local:

```text
Puerto del host: 3307
Base de datos: banco_xyz
Usuario de aplicación: banco_user
```

Desde los contenedores de Docker Compose, la conexión utiliza el nombre del servicio MySQL y su puerto interno:

```text
jdbc:mysql://mysql:3306/banco_xyz
```

Para ejecución local desde el equipo anfitrión:

```text
jdbc:mysql://localhost:3307/banco_xyz
```

La base de datos contiene las tablas utilizadas por los servicios bancarios y los resultados del procesamiento batch. Las tablas concretas dependen de las entidades, repositorios y configuraciones JPA y Spring Batch presentes en cada módulo.

El proyecto utiliza un volumen Docker para conservar los datos de MySQL entre reinicios de los contenedores.

**Advertencia:** no se debe utilizar `docker compose down -v` cuando se quieran conservar los datos, porque ese comando elimina los volúmenes declarados por el proyecto.

## 12. Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje de programación. |
| Spring Boot | Desarrollo de microservicios. |
| Spring Cloud | Componentes de arquitectura distribuida. |
| Spring Cloud Config | Configuración centralizada. |
| Netflix Eureka | Descubrimiento de servicios. |
| Spring Cloud OpenFeign | Comunicación HTTP entre servicios. |
| Spring Cloud LoadBalancer | Balanceo de llamadas entre instancias. |
| Spring Security | Seguridad de endpoints. |
| Spring Authorization Server | Implementación del Authorization Server. |
| OAuth 2.0 | Protocolo de autorización. |
| JWT | Tokens de acceso. |
| Resilience4j | Tolerancia a fallos. |
| Spring Kafka | Integración entre Spring y Kafka. |
| Apache Kafka | Mensajería y eventos asíncronos. |
| Spring Batch | Procesamiento por lotes. |
| Spring Data JPA | Persistencia relacional. |
| Hibernate | ORM. |
| MySQL 8.4 | Base de datos relacional. |
| Docker | Contenedorización. |
| Docker Compose | Orquestación de contenedores. |
| Amazon EC2 | Infraestructura para Kafka. |
| Maven | Gestión de dependencias y construcción. |
| Git y GitHub | Control de versiones. |

## 13. Puertos de los componentes

| Componente | Puerto |
|---|---:|
| Banco Batch | `8080` |
| Account Service | `8081` |
| BFF Web | `8082` |
| BFF Mobile | `8083` |
| BFF ATM | `8084` |
| Auditoria Service | `8086` |
| Payment Service | `8088` |
| Account Management Service | `8089` |
| Auth Server | `9000` |
| Discovery Server / Eureka | `8761` |
| Config Server | `8888` |
| MySQL en el host | `3307` |
| Kafka Broker 1 | `29092` |
| Kafka Broker 2 | `39092` |
| Kafka Broker 3 | `49092` |
| Kafka UI | `8090` |

## 14. Requisitos previos

Para ejecutar el proyecto se necesita:

- Java 21.
- Docker.
- Docker Compose.
- Git.
- Acceso a la infraestructura Kafka desplegada en EC2.
- Los archivos CSV requeridos por los procesos batch.
- Configuración válida para las conexiones a MySQL, Eureka, Auth Server y Kafka.

Los módulos Maven incluyen o deben disponer de su Maven Wrapper para construir los JAR sin depender de una instalación global de Maven.

## 15. Configuración del entorno

Antes de iniciar los servicios, comprueba las variables de entorno utilizadas por Docker Compose y los archivos `application.properties`.

Entre las variables de configuración se encuentran:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
EUREKA_SERVER_URL
AUTH_SERVER_URL
AUTH_SERVER_ISSUER
KAFKA_BOOTSTRAP_SERVERS
```

Los nombres concretos y su uso dependen de la configuración de cada módulo.

Ejemplos de valores de conexión dentro de Docker Compose:

```text
DB_URL=jdbc:mysql://mysql:3306/banco_xyz
EUREKA_SERVER_URL=http://discovery-server:8761/eureka/
AUTH_SERVER_URL=http://auth-server:9000
```

El `issuer-uri` de los Resource Servers debe coincidir con el emisor que aparece en los JWT emitidos por Auth Server.

Las direcciones de los brokers Kafka deben corresponder a las direcciones anunciadas por el clúster y ser accesibles desde los contenedores.

No se deben publicar contraseñas reales ni secretos de OAuth 2.0 en el repositorio.

## 16. Ejecución con Docker Compose

Desde la raíz del proyecto, donde se encuentra `docker-compose.yaml`, ejecutar:

```bash
docker compose up -d --build
```

Comprobar el estado de los contenedores:

```bash
docker compose ps
```

Consultar los logs de un servicio:

```bash
docker compose logs -f account-service
docker compose logs -f account-management-service
docker compose logs -f payment-service
docker compose logs -f auditoria-service
docker compose logs -f banco-batch
```

Consultar los logs de los componentes de infraestructura:

```bash
docker compose logs -f auth-server
docker compose logs -f discovery-server
docker compose logs -f config-server
```

Detener los servicios:

```bash
docker compose down
```

Este comando detiene y elimina los contenedores, pero conserva los datos que estén almacenados en los volúmenes persistentes.

No utilizar `docker compose down -v` si se necesita conservar la base de datos.

## 17. Ejecución de Kafka en EC2

La infraestructura Kafka utiliza un archivo Compose independiente:

```text
docker-compose.kafka.yaml
```

En la instancia EC2, ejecutar:

```bash
docker compose -f docker-compose.kafka.yaml up -d
```

Comprobar el estado de los contenedores:

```bash
docker compose -f docker-compose.kafka.yaml ps
```

Consultar los logs de un broker:

```bash
docker compose -f docker-compose.kafka.yaml logs -f <nombre-del-broker>
```

La infraestructura debe estar disponible y los tópicos requeridos deben existir para que los productores y consumidores puedan intercambiar mensajes.

Tópicos utilizados por la aplicación:

```text
cuentas-eventos
transacciones-bancarias
```

La conectividad externa depende de las reglas de red, los puertos publicados y la configuración de listeners y advertised listeners de Kafka.

## 18. Construcción y actualización individual de un microservicio

Cada microservicio puede compilarse de forma independiente.

Por ejemplo, para `auditoria-service`, desde la carpeta del módulo:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Después, desde la raíz del proyecto:

```powershell
docker compose up -d --build --no-deps --force-recreate auditoria-service
```

Este procedimiento permite reconstruir y recrear únicamente el servicio actualizado, sin reiniciar los demás contenedores ni eliminar los datos persistentes de MySQL.

El mismo procedimiento se puede aplicar a otros módulos, cambiando la carpeta de compilación y el nombre del servicio de Docker Compose.

## 19. Control de versiones

El código fuente se mantiene en Git y GitHub.

Ejemplo de flujo de trabajo:

```bash
git status
git add .
git commit -m "Actualiza arquitectura de microservicios y seguridad"
git push origin main
```

Se recomienda revisar los cambios antes de agregarlos al repositorio para evitar subir credenciales, archivos de entorno, datos sensibles o artefactos de compilación.

## 20. Consideraciones de despliegue

- `backend-core` fue eliminado y sus responsabilidades se distribuyeron entre `account-service`, `account-management-service` y `payment-service`.
- Los tres servicios nuevos incorporan configuración de seguridad.
- Los BFF se comunican con los servicios responsables mediante clientes OpenFeign.
- Kafka se mantiene separado de la infraestructura local de microservicios.
- `auditoria-service` consume eventos de cuentas y transacciones mediante tópicos independientes.
- `banco-batch` procesa archivos CSV con Spring Batch y persiste los resultados en MySQL.
- Las variables de entorno deben corresponder a las direcciones accesibles desde cada contenedor.
- Los cambios de código deben desplegarse reconstruyendo el JAR y la imagen del servicio correspondiente.
- Los volúmenes persistentes deben conservarse cuando no se pretenda reinicializar la base de datos.

## 21. Resumen de arquitectura

```text
Banco XYZ
│
├── Infraestructura Spring Cloud
│   ├── Config Server
│   ├── Discovery Server
│   └── Auth Server
│
├── BFF
│   ├── BFF Web
│   ├── BFF Mobile
│   └── BFF ATM
│
├── Servicios de negocio
│   ├── Account Service
│   ├── Account Management Service
│   └── Payment Service
│
├── Auditoría
│   └── Auditoria Service
│       ├── CuentaConsumer
│       └── TransaccionConsumer
│
├── Procesamiento batch
│   └── Banco Batch
│       ├── estados_financieros_anuales.csv
│       ├── intereses_trimestrales.csv
│       └── movimientos_financieros_diarios.csv
│
├── Persistencia
│   └── MySQL
│
└── Infraestructura externa
    └── Kafka Cluster en Amazon EC2
        ├── cuentas-eventos
        └── transacciones-bancarias
```
