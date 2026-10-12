# Instrucciones de ejecución y pruebas — Banco XYZ

## 1. Objetivo

Este documento explica cómo ejecutar y comprobar los componentes del sistema Banco XYZ durante el desarrollo y las pruebas locales.

El sistema está compuesto por microservicios, servidores de configuración y autenticación, Eureka, tres BFF, una aplicación de procesamiento por lotes y una infraestructura Kafka externa.

**Importante:** la preparación del entorno de nube, el despliegue de Kafka en AWS EC2 y la configuración de GitHub Actions se explican en el archivo [`despliegue.md`](despliegue.md). Consulta ese documento antes de ejecutar las pruebas que dependan de Kafka.

## 2. Requisitos previos

Antes de comenzar, comprueba que estén instalados:

- Java 21.
- Maven o el Maven Wrapper incluido en el proyecto.
- Docker y Docker Compose.
- Git.
- Un cliente HTTP, como Postman, para probar los endpoints.
- Acceso a la instancia EC2 que ejecuta Kafka, cuando corresponda.

También deben estar disponibles los archivos de configuración de los servicios y las credenciales de desarrollo necesarias.

## 3. Componentes y puertos

| Componente | Puerto |
|---|---:|
| MySQL | 3307 |
| Config Server | 8888 |
| Discovery Server (Eureka) | 8761 |
| Auth Server | 9000 |
| Account Service | 8081 |
| BFF Web | 8082 |
| BFF Mobile | 8083 |
| BFF ATM | 8084 |
| Auditoría Service | 8086 |
| Payment Service | 8088 |
| Account Management Service | 8089 |
| Banco Batch | 8080 |
| Kafka UI, si está habilitada en EC2 | 8090 |

Los puertos corresponden a la configuración de desarrollo descrita para el proyecto. Si cambian los archivos de configuración, utiliza los valores vigentes.

## 4. Preparación del proyecto

Abre una terminal en la carpeta raíz del proyecto, donde se encuentra `docker-compose.yaml`.

Comprueba la disponibilidad de Docker:

```powershell
docker --version
docker compose version
```

Verifica también la versión de Java:

```powershell
java -version
```

Si los Dockerfiles de los módulos utilizan `target/*.jar`, genera primero los JAR necesarios. Desde la carpeta de cada módulo Maven, ejecuta:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Repite el procedimiento en los módulos que necesiten reconstruirse. Si un módulo no contiene Maven Wrapper, utiliza el comando `mvn` siempre que Maven esté instalado.

## 5. Iniciar el entorno local

Desde la raíz del proyecto, comprueba la configuración de Docker Compose:

```powershell
docker compose config
```

Si la validación finaliza correctamente, inicia los servicios:

```powershell
docker compose up -d
```

Consulta su estado:

```powershell
docker compose ps
```

Para revisar los registros de un componente:

```powershell
docker compose logs --tail=100 config-server
```

Sustituye `config-server` por el nombre real del servicio que necesites revisar.

**Nota:** este comando inicia los servicios definidos en el Compose local. No reemplaza el procedimiento para desplegar o actualizar Kafka en EC2, que se encuentra en [`despliegue.md`](despliegue.md).

## 6. Orden de inicialización y comprobaciones

Comprueba los componentes en el siguiente orden lógico. Docker Compose puede iniciar contenedores en paralelo, por lo que debes verificar que cada dependencia esté disponible antes de probar los servicios que la utilizan.

### 6.1. MySQL

Comprueba que el contenedor esté activo:

```powershell
docker compose ps mysql
```

Consulta los registros:

```powershell
docker compose logs --tail=100 mysql
```

Verifica que la base de datos `banco_xyz` esté disponible y que las tablas requeridas por los servicios existan.

El proyecto utiliza las tablas `cuentas`, `transacciones` y `estados_anuales`, además de otras que puedan crear los módulos según su configuración.

No elimines el volumen `banco_xyz_data` durante las pruebas habituales. Para detener el entorno, utiliza `docker compose down`, sin agregar `-v`.

### 6.2. Config Server

Comprueba que el servicio esté activo y que pueda entregar la configuración que necesita cada aplicación.

Si un microservicio no inicia, revisa primero los registros del Config Server y confirma que el archivo de configuración correspondiente esté disponible.

### 6.3. Discovery Server (Eureka)

Abre en el navegador:

`http://localhost:8761`

Comprueba que Eureka muestre los servicios que ya se hayan iniciado y registrado correctamente.

Si un servicio no aparece, revisa sus registros y la dirección de Eureka configurada en sus propiedades.

### 6.4. Auth Server

Comprueba que el servidor de autenticación esté activo en el puerto `9000`.

Revisa la configuración del emisor OAuth2/JWT y confirma que los BFF y microservicios utilicen la dirección correcta del servidor.

Para validar autenticación y autorización, utiliza las solicitudes definidas en los controladores y en la configuración de seguridad del proyecto. No asumas que todos los endpoints son públicos.

### 6.5. Account Service

Este servicio concentra las consultas de cuentas y transacciones.

Verifica que:
- Se inicie correctamente.
- Se registre en Eureka.
- Pueda conectarse a MySQL.
- Responda a las consultas disponibles en sus controladores.

Consulta `AccountService` y sus controladores para identificar las rutas HTTP reales.

### 6.6. Account Management Service

Este servicio administra las cuentas.

Verifica que:
- Se registre en Eureka.
- Pueda conectarse a MySQL.
- Las operaciones de administración disponibles funcionen con datos de prueba.
- Los eventos de cuenta se publiquen en Kafka cuando corresponda.

Consulta los controladores del módulo para conocer las operaciones y rutas implementadas.

### 6.7. Payment Service

Este servicio implementa operaciones de pago, incluidos los retiros disponibles en el alcance actual.

Comprueba que:
- Se registre en Eureka.
- Pueda consultar los datos necesarios.
- Las reglas de negocio se apliquen correctamente.
- Las operaciones admitidas produzcan sus eventos en Kafka.

Prueba tanto los casos válidos como los rechazos esperados por las reglas de negocio. Utiliza cuentas y montos de prueba controlados.

### 6.8. BFF Web, BFF Mobile y BFF ATM

Los BFF proporcionan puntos de entrada específicos para los distintos canales.

| BFF | Puerto | Dependencias principales |
|---|---:|---|
| Web | 8082 | Account Service y Account Management Service |
| Mobile | 8083 | Account Service |
| ATM | 8084 | Account Service y Payment Service |

Comprueba que cada BFF:
- Inicie y se registre en Eureka.
- Utilice la configuración de autenticación esperada.
- Pueda comunicarse con sus servicios mediante OpenFeign.
- Aplique las políticas de resiliencia configuradas.
- Devuelva las respuestas o errores esperados cuando una dependencia no esté disponible.

Las rutas públicas de cada BFF deben obtenerse de sus propios controladores. No utilices rutas de ejemplo como si estuvieran implementadas.

### 6.9. Auditoría Service y Kafka

El servicio de auditoría consume eventos de los tópicos:

- `cuentas-eventos`
- `transacciones-bancarias`

Para verificar el flujo completo:

1. Confirma que Kafka esté desplegado y accesible. Si todavía no está preparado, sigue [`despliegue.md`](despliegue.md).
2. Ejecuta una operación de cuenta que publique un evento.
3. Ejecuta una operación de pago válida que publique un evento.
4. Revisa los registros de `auditoria-service`.
5. Comprueba que cada consumidor interprete el tipo de evento correspondiente.

En la implementación actual, la recepción de eventos se comprueba mediante los registros del servicio de auditoría. No debe suponerse que existe persistencia histórica de auditoría en una tabla propia si no se ha implementado y verificado.

### 6.10. Banco Batch

Banco Batch procesa archivos CSV y guarda los resultados en MySQL.

Los archivos utilizados son:

- `movimientos_financieros_diarios.csv`
- `intereses_trimestrales.csv`
- `estados_financieros_anuales.csv`

El proyecto contempla trabajos para transacciones diarias, intereses trimestrales, estados anuales y la coordinación del proceso completo.

Para comprobar el servicio:

1. Confirma que MySQL esté disponible.
2. Verifica que los archivos CSV se encuentren en las rutas esperadas por la configuración.
3. Comprueba que el contenedor o la aplicación esté iniciado.
4. Ejecuta el trabajo mediante el mecanismo definido en el proyecto.
5. Revisa los registros del trabajo, los registros omitidos y las anomalías.
6. Comprueba los resultados en las tablas correspondientes de MySQL.

Consulta `BatchJobController` y la configuración de los trabajos para obtener los endpoints y parámetros reales. No asumas que un trabajo se ejecuta automáticamente al iniciar la aplicación.

## 7. Pruebas de integración

Después de verificar cada componente individualmente, realiza pruebas de extremo a extremo:

1. **Consultas:** acceder desde un BFF a los datos proporcionados por Account Service.
2. **Administración:** ejecutar una operación de cuenta disponible desde el canal correspondiente.
3. **Pagos:** ejecutar un retiro válido y comprobar el resultado.
4. **Autenticación:** probar las solicitudes con credenciales o tokens válidos e inválidos.
5. **Eventos:** verificar que las operaciones generen eventos y que Auditoría Service los reciba.
6. **Procesamiento batch:** ejecutar un trabajo y verificar sus resultados en la base de datos.
7. **Resiliencia:** comprobar el comportamiento configurado cuando una dependencia no responde.

Registra los resultados obtenidos y conserva capturas de pantalla o registros que sirvan como evidencia.

## 8. Diagnóstico de problemas

| Problema | Qué revisar |
|---|---|
| Un contenedor no inicia | `docker compose logs <servicio>` |
| Error de conexión a MySQL | URL JDBC, credenciales, puerto y estado del contenedor |
| Un servicio no aparece en Eureka | Dirección de Eureka, configuración y registros de inicio |
| Error de autenticación | Emisor, token, configuración OAuth2/JWT y reglas de seguridad |
| Error al invocar otro microservicio | Nombre de servicio, Eureka, OpenFeign y conectividad |
| No llegan eventos Kafka | Bootstrap servers, listeners anunciados, tópicos y registros de productores/consumidores |
| Falla un trabajo batch | Ruta CSV, formato, validaciones, configuración de lectores/escritores y conexión a MySQL |

## 9. Detener el entorno

Para detener los servicios locales sin eliminar los datos del volumen:

```powershell
docker compose down
```

No utilices `docker compose down -v` como parte del procedimiento habitual, porque podría eliminar el volumen de datos de MySQL.

## 10. Relación con el despliegue

Este documento se centra en la ejecución y validación funcional del sistema.

Para preparar el entorno de nube, configurar los secretos de GitHub, desplegar Kafka en EC2 y actualizar la infraestructura, consulta [`despliegue.md`](despliegue.md).