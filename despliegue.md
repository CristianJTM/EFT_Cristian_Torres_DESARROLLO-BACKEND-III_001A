# Despliegue del sistema Banco XYZ

## 1. Objetivo y alcance

Este documento describe cómo preparar y desplegar la infraestructura de nube utilizada por Banco XYZ, automatizar la actualización de Kafka mediante GitHub Actions y conectar el entorno de nube con los servicios de la aplicación.

El despliegue actual es **híbrido**: Kafka se ejecuta en una instancia AWS EC2, mientras que los microservicios, los BFF y MySQL se ejecutan mediante Docker Compose en el entorno local de desarrollo.

Por tanto, este procedimiento no implica que todos los componentes estén desplegados en la nube.

Para obtener las instrucciones detalladas de ejecución y las pruebas funcionales de cada microservicio, consulta [`instrucciones.md`](instrucciones.md).

## 2. Arquitectura de despliegue

| Componente | Ubicación actual | Método |
|---|---|---|
| Kafka y Kafka UI | AWS EC2 | Docker Compose y GitHub Actions |
| Config Server | Entorno local | Docker Compose |
| Discovery Server (Eureka) | Entorno local | Docker Compose |
| Auth Server | Entorno local | Docker Compose |
| Account Service | Entorno local | Docker Compose |
| Account Management Service | Entorno local | Docker Compose |
| Payment Service | Entorno local | Docker Compose |
| BFF Web, Mobile y ATM | Entorno local | Docker Compose |
| Auditoría Service | Entorno local | Docker Compose |
| Banco Batch | Entorno local | Docker Compose |
| MySQL | Entorno local | Docker Compose |

Los puertos y las pruebas de estos componentes se encuentran en `instrucciones.md`, para evitar duplicar esa información en este documento.

## 3. Requisitos previos

Antes de desplegar, asegúrate de disponer de:

- Una cuenta de AWS con acceso a una instancia EC2.
- Docker instalado en la instancia EC2.
- Docker Compose disponible en la instancia.
- Un repositorio GitHub con el archivo `docker-compose.kafka.yaml` y el workflow de despliegue.
- Una clave SSH autorizada para conectarse a la instancia.
- Reglas de red de AWS que permitan el acceso requerido.
- Los tópicos y la configuración Kafka esperados por los productores y consumidores del sistema.

El usuario de SSH debe poder ejecutar los comandos Docker necesarios. Los permisos deben configurarse de forma controlada, evitando otorgar privilegios innecesarios.

## 4. Preparar la instancia EC2

### 4.1. Instalar y verificar las herramientas

Conéctate a la instancia EC2 mediante SSH e instala Docker y Docker Compose según el sistema operativo de la instancia.

Comprueba que estén disponibles:

```bash
docker --version
docker compose version
```

Si el proyecto utiliza el comando independiente `docker-compose`, verifica que también esté instalado o adapta el workflow para utilizar `docker compose`.

### 4.2. Crear el directorio de despliegue

El workflow copia el archivo Compose a `~/banco-xyz-kafka`. Por ello, crea el directorio antes del primer despliegue:

```bash
mkdir -p ~/banco-xyz-kafka
```

Comprueba que el usuario de despliegue pueda escribir en él.

### 4.3. Configurar las reglas de red

En el grupo de seguridad de EC2, permite únicamente los accesos que se necesiten:

- SSH para la administración, restringido a las direcciones autorizadas.
- Puertos externos de Kafka utilizados por los clientes.
- Puerto de Kafka UI, solo si debe consultarse desde fuera de la instancia.

En la configuración actual se han utilizado los puertos externos `29092`, `39092` y `49092` para Kafka, y `8090` para Kafka UI. Confirma que coincidan con los listeners de `docker-compose.kafka.yaml`.

No expongas puertos de administración ni interfaces internas innecesariamente.

## 5. Configurar GitHub Actions

El repositorio utiliza un workflow que se ejecuta cuando se publica un cambio en la rama `main`. El workflow copia `docker-compose.kafka.yaml` a EC2 y ejecuta los comandos necesarios para actualizar Kafka.

### 5.1. Configurar los secretos

En GitHub, abre el repositorio y entra en:

**Settings → Secrets and variables → Actions**

Crea los siguientes secretos:

| Secreto | Contenido |
|---|---|
| `EC2_SSH_KEY` | Clave privada SSH autorizada para la instancia |
| `USER_SERVER` | Usuario de conexión SSH |
| `EC2_HOST` | Dirección pública o nombre DNS de EC2 |

No escribas la clave privada ni otras credenciales directamente en el repositorio. GitHub Actions permite almacenar credenciales en secretos y utilizarlas desde los workflows. Para información adicional, consulta la [documentación oficial de GitHub Actions sobre despliegues](https://docs.github.com/en/actions/how-tos/deploy).

### 5.2. Verificar el workflow

Comprueba que el archivo de workflow se encuentre dentro de `.github/workflows/` y que el nombre del archivo coincida con el utilizado por el repositorio.

El workflow existente realiza estas operaciones:

1. Descarga el repositorio mediante `actions/checkout`.
2. Configura la clave SSH.
3. Copia `docker-compose.kafka.yaml` a la instancia EC2.
4. Genera el archivo `.env` con `KAFKA_ADVERTISED_HOST`.
5. Valida la configuración de Compose.
6. Detiene los contenedores Kafka existentes.
7. Descarga las imágenes.
8. Inicia la infraestructura y muestra su estado.

**Importante:** el workflow mostrado utiliza `StrictHostKeyChecking=no` y `UserKnownHostsFile=/dev/null`. Esto simplifica la conexión, pero no verifica de manera estricta la identidad del servidor SSH. Para un entorno productivo, configura y valida `known_hosts`.

El workflow también ejecuta `docker-compose down` antes de volver a iniciar Kafka. Revisa cómo se almacenan los datos y la configuración de volúmenes antes de utilizarlo en un entorno con información que no se pueda perder.

## 6. Ejecutar el despliegue de Kafka

Una vez preparados EC2, el directorio remoto y los secretos:

1. Guarda los cambios de configuración en el repositorio.
2. Publica los cambios en la rama `main`.
3. Abre la pestaña **Actions** de GitHub.
4. Selecciona la ejecución del workflow de despliegue.
5. Comprueba que cada paso finalice correctamente.
6. Verifica que los contenedores estén activos en EC2.

Si el workflow falla, revisa primero el paso que presentó el error. Los problemas frecuentes son una clave SSH incorrecta, un directorio remoto inexistente, permisos insuficientes, Docker Compose ausente o errores en el archivo de configuración.

## 7. Verificar Kafka después del despliegue

Una vez que Kafka esté activo, comprueba que los brokers se encuentren disponibles y que anuncien direcciones accesibles desde el entorno que ejecutará los microservicios.

Los clientes del sistema deben utilizar el bootstrap configurado para el acceso externo. En la configuración local anterior se utilizaron tres brokers con puertos `29092`, `39092` y `49092`; actualiza esos valores si la dirección o los listeners de EC2 cambian.

Verifica también que existan los siguientes tópicos:

- `transacciones-bancarias`
- `cuentas-eventos`

La configuración actual contempla tres particiones y un factor de replicación de tres para cada tópico. Comprueba esos valores en el entorno desplegado en vez de asumir que se crearon correctamente.

Si Kafka UI está habilitada, utilízala para revisar el estado de los brokers, los tópicos y los mensajes.

## 8. Conectar los servicios de Banco XYZ con Kafka

Cuando Kafka esté disponible, revisa las variables de entorno del `docker-compose.yaml` local para confirmar que productores y consumidores utilizan el bootstrap externo correcto.

Los componentes involucrados son:

- **Payment Service:** publica eventos de transacciones.
- **Account Management Service:** publica eventos relacionados con cuentas.
- **Auditoría Service:** consume eventos de los dos tópicos.

Comprueba que los listeners anunciados por Kafka sean accesibles desde el entorno local. Que un broker acepte una conexión inicial no garantiza que el cliente pueda conectarse a todos los listeners que recibe en la respuesta de Kafka.

Para ejecutar los servicios y realizar las pruebas de integración completas, sigue las instrucciones de [`instrucciones.md`](instrucciones.md).

## 9. Actualizar el sistema

### 9.1. Actualizar Kafka

Para modificar la infraestructura Kafka:

1. Edita `docker-compose.kafka.yaml`.
2. Revisa los listeners, la configuración de los brokers, los volúmenes y los tópicos.
3. Valida los cambios en un entorno de pruebas cuando sea posible.
4. Publica los cambios en `main`.
5. Supervisa la ejecución del workflow.
6. Verifica los contenedores y los tópicos después del despliegue.

No cambies puertos, listeners o nombres de tópicos sin revisar también la configuración de los clientes.

### 9.2. Actualizar los microservicios

En el estado actual del proyecto, los microservicios se construyen y ejecutan localmente mediante Docker Compose. El workflow de Kafka no despliega automáticamente los JAR ni las imágenes de los microservicios.

Para actualizar un servicio local:

1. Compila y empaqueta el módulo Maven.
2. Reconstruye y recrea únicamente el servicio modificado.
3. Comprueba sus registros y su registro en Eureka.
4. Ejecuta las pruebas correspondientes descritas en `instrucciones.md`.

Ejemplo, desde la carpeta del módulo que se quiere actualizar:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Después, desde la raíz donde se encuentra `docker-compose.yaml`:

```powershell
docker compose up -d --build --no-deps --force-recreate <nombre-del-servicio>
```

Reemplaza el marcador por el nombre real definido en Compose. Este comando no constituye un despliegue de los microservicios en AWS.

## 10. Seguridad y mantenimiento

Para mantener el entorno controlado:

- Guarda las credenciales en secretos y variables de entorno, no en el código fuente.
- Restringe el acceso SSH y los puertos públicos a los orígenes necesarios.
- Evita exponer Kafka UI a Internet sin controles de acceso.
- Mantén Docker, las imágenes y las dependencias actualizados.
- Revisa los logs de GitHub Actions y de los contenedores tras cada actualización.
- Comprueba el estado de los volúmenes antes de ejecutar comandos que detengan o recreen contenedores.
- Planifica copias de seguridad y recuperación para los datos que deban conservarse.
- Verifica que la configuración de los clientes coincida con los listeners y tópicos reales.

## 11. Alcance actual y posibles mejoras

El despliegue descrito corresponde al entorno híbrido de desarrollo: Kafka se aloja en EC2 y los demás componentes se ejecutan localmente.

Para llevar el sistema completo a un entorno de nube, sería necesario definir y configurar un destino para MySQL y para los microservicios, gestionar sus imágenes, externalizar la configuración y credenciales, y establecer controles de red, observabilidad, persistencia y recuperación.

Estas actividades son mejoras de infraestructura; no deben presentarse como implementadas hasta que se hayan realizado y verificado.

## 12. Evidencias del despliegue

Para documentar el procedimiento, conserva evidencias de:

1. El workflow de GitHub Actions finalizado correctamente.
2. Los contenedores Kafka activos en EC2.
3. Los tópicos `cuentas-eventos` y `transacciones-bancarias`.
4. Los listeners y puertos configurados.
5. La recepción de eventos en Auditoría Service.
6. Las pruebas de integración del sistema descritas en `instrucciones.md`.

Las capturas y los registros deben corresponder a ejecuciones reales del proyecto.