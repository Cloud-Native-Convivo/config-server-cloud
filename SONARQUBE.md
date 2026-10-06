# Guía de Configuración y Ejecución de SonarQube

Este documento describe la configuración implementada en el repositorio **config-server** (Convivo) para la integración con **SonarQube** (análisis estático de código - SAST / Calidad), así como los pasos para iniciar el servidor y ejecutar el análisis.

---

## 1. Archivos Configurados en el Repositorio

### 1.1 `sonar-project.properties`

Archivo de configuración principal ubicado en la raíz del repositorio. Define el alcance del análisis, rutas de código fuente Java y cobertura JaCoCo:

```properties
# Identificador único del proyecto en SonarQube
sonar.projectKey=convivo_config-**server**

# Nombre descriptivo visible en el panel de SonarQube
sonar.projectName=Convivo - Config Server

# Versión del proyecto
sonar.projectVersion=0.4.0

# Rutas de código fuente y tests a ser analizadas
sonar.sources=src/main/java,src/main/resources
sonar.tests=src/test/java
sonar.java.binaries=target/classes

# Codificación de los archivos fuente
sonar.sourceEncoding=UTF-8

# Reportes de cobertura JaCoCo
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml

# Archivos y carpetas excluidas del análisis de código
sonar.exclusions=**/target/**,**/.scannerwork/**

# URL por defecto del servidor SonarQube
sonar.host.url=http://localhost:9000
```

### 1.2 `pom.xml`

Se incorporó el plugin `sonar-maven-plugin` para habilitar el escaneo directo mediante Maven Wrapper (`./mvnw`):

```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>5.0.0.4389</version>
</plugin>
```

### 1.3 `docker-compose.yml`

Se incorporaron los servicios `sonarqube` y `scanner` para permitir el despliegue local y análisis mediante contenedores:

- Servicio `sonarqube-db` (`postgres:15-alpine`) como base de datos de producción persistente.
- Servicio `sonarqube` (`sonarqube:community`) exponiendo el puerto `9000` y conectado vía JDBC a PostgreSQL.
- Servicio `scanner` (`sonarsource/sonar-scanner-cli:latest`) con perfil `tools` montando la raíz `./:/usr/src`.
- Volúmenes persistentes: `sonarqube_pgdata`, `sonarqube_data`, `sonarqube_extensions`, `sonarqube_logs`.

### 1.4 `.gitignore`

Se añadió la exclusión de los artefactos de análisis:

- `.scannerwork/`

---

## 2. Paso a Paso para Ejecutar el Análisis

### Paso 1: Iniciar SonarQube

#### Opción A (Docker Compose - Recomendada)

Levantar el servicio de SonarQube en segundo plano:

```bash
docker compose up -d sonarqube
```

#### Opción B (Local / Binario)

Si se dispone del binario en Windows:

1. Ejecutar `StartSonar.bat`.
2. Esperar el inicio de Elasticsearch y Web Server.

Acceso web:

- URL: **[http://localhost:9000](http://localhost:9000)**
- Credenciales iniciales:
  - **Usuario:** `admin`
  - **Contraseña:** `admin`
- En el primer inicio, solicitará actualizar la contraseña.

---

## 3. Configurar Token en `.env`

Copia el archivo de plantilla y añade tu token para evitar pasarlo manualmente por línea de comandos:

```powershell
cp .env.example .env
```

Edita `.env` y define:
```dotenv
SONAR_TOKEN=squ_tu_token_aqui
```

---

## 4. Ejecutar el Escaneo

### Opción A: Vía Contenedor Docker Scanner (Carga `.env` automáticamente)

Al estar definido en `.env`, Docker Compose inyecta `SONAR_TOKEN` directamente en el contenedor `scanner`:

```powershell
docker compose --profile tools run --rm scanner
```

*(Si no defines `.env`, puedes pasar la variable temporalmente: `$env:SONAR_TOKEN="tu_token"; docker compose --profile tools run --rm scanner`)*.

### Opción B: Vía Maven Wrapper (Recomendado para Java / Spring Boot)

```powershell
# 1. Compilación, tests y generación de reporte JaCoCo
./mvnw clean verify

# 2. Ejecución de SonarQube con el token
./mvnw sonar:sonar -Dsonar.token="TU_TOKEN_GENERADO"
```

---

## 5. Visualización de Resultados

Al finalizar el escaneo, la consola confirmará el análisis con la URL correspondiente:

```text
INFO: ANALYSIS SUCCESSFUL, you can browse http://localhost:9000/dashboard?id=convivo_config-server
```

En el dashboard se evaluarán:

- **Vulnerabilidades y Seguridad (SAST):** Detección de fallas OWASP Top 10 y gestión de configuraciones inseguras.
- **Security Hotspots:** Puntos sensibles para auditoría manual.
- **Code Smells y Mantenibilidad:** Reglas de código limpio y deuda técnica.
- **Bugs y Confiabilidad:** Detección de fallos en runtime.
- **Cobertura:** Mapeo de pruebas unitarias importadas desde JaCoCo (`jacoco.xml`).
