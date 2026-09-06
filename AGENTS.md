# AGENTS.md — config-server

Config Server centralizado de Convivo (Spring Cloud Config Server, perfil `native`). Sirve configuración vía HTTP a los demás servicios del workspace.

**Para el agente que trabaje en este microservicio:**

- Sin emojis en código, PR, docs generadas ni output — usar solo como último recurso si no existe alternativa real.
- En commits rige Gitmoji (§11.2) — ahí el emoji es obligatorio por convención.
- Nada de solución genérica de tutorial. Cada decisión responde a Convivo, no a un boilerplate de curso.
- **Nota de dominio**: el paquete Java es `com.convivo.config_server`. `config/` sirve solo YAML de microservicios que existen hoy en `Microservicios/` — `discovery-server.yml` y `ms-espacios-comunes.yml`, ambos consumidos en runtime por sus respectivos servicios (`ms-espacios-comunes` lo hace best-effort vía HTTP en su arranque, ver `ms-espacios-comunes/AGENTS.md` §2). Se eliminaron los YAML de servicios que no correspondían a ningún microservicio real del MVP de Convivo (`cloud-native/mvp.md` §2.1/§4.4: el perímetro de API es Amazon API Gateway administrado, no un servicio Spring propio; el BFF real se llama `bff`). `bff` y `ms-gastos-comunes` son componentes reales del MVP (mvp.md) pero **sin código en el workspace todavía** — cuando se creen esas carpetas se agrega su YAML, no antes.

## 0. Jerarquía de reglas

1. Seguridad y corrección — nunca se sacrifican por ninguna otra regla.
2. Convenciones del proyecto (stack, estilo, arquitectura) — se siguen salvo instrucción explícita en contrario.
3. Minimalismo (Ponytail) — se aplica solo después de satisfacer 1 y 2.

## 1. Resumen del proyecto

`config-server` es el servidor de configuración centralizado de Convivo (Spring Cloud Config Server). Sirve archivos YAML de configuración a los demás microservicios vía HTTP, con backend `native` (filesystem, `classpath:/config`) en vez de un repositorio Git remoto. Puerto `8888`. No expone lógica de negocio propia.

## 2. Stack técnico

- Lenguaje: Java 21
- Framework: Spring Boot 4.1.1 + Spring Cloud 2025.1.1 (`spring-cloud-config-server`)
- Build: Maven (wrapper `mvnw`/`mvnw.cmd`)
- Tests: JUnit 5 (`spring-boot-starter-test`)
- Contenedores: Docker (multi-stage, `eclipse-temurin:21-jre-alpine` en runtime) + docker-compose

## 3. Estructura del proyecto

```text
src/main/java/com/convivo/config_server/
  ConfigServerApplication.java   # @SpringBootApplication + @EnableConfigServer
src/main/resources/
  application.yml                # puerto, perfil native, exposición actuator
  config/
    discovery-server.yml         # servicio real, lo consume
    ms-espacios-comunes.yml      # servicio real, lo consume best-effort al arrancar
src/test/java/com/convivo/config_server/
  ConfigServerApplicationTests.java   # smoke test de contexto
dockerfile                       # build multi-stage
docker-compose.yml                # dev: monta el código como volumen, mvn spring-boot:run
```

## 4. Comandos

```bash
# instalar / compilar
./mvnw clean install -DskipTests

# test completo
./mvnw test

# test acotado a una clase
./mvnw test -Dtest=ConfigServerApplicationTests

# levantar local
./mvnw spring-boot:run -Dspring-boot.run.profiles=native

# levantar con docker-compose (dev, hot-reload por volumen montado)
docker compose up --build

# build de la imagen de producción (multi-stage, sin volumen)
docker build -t config-server .
```

Todo comando de arriba es ejecutable tal cual desde la raíz de `config-server/`.

## 5. Estilo de código

`(no aplica: sin código propio más allá de la clase de arranque — config-server no tiene controladores, servicios ni repositorios propios, toda su función la resuelve spring-cloud-config-server por configuración)`. Si se agrega lógica propia (ej. un `EnvironmentRepository` custom), seguir las convenciones estándar de Spring Boot (constructor injection, sin campos `@Autowired` mutables).

## 6. Disciplina anti-sobreingeniería (Ponytail)

Configuración global del agente — no duplicar aquí. Aplica a código nuevo a escribir, no autoriza podar documentación existente.

## 7. Pruebas

`(no aplica cobertura formal: servicio de configuración sin lógica de negocio propia)`. Único test existente: `ConfigServerApplicationTests` (smoke test de contexto — `contextLoads()`). Framework: JUnit 5 vía `spring-boot-starter-test`. Ubicación: `src/test/java/com/convivo/config_server/`.

Si se agrega lógica propia (ej. filtrado o transformación de configuración), esa lógica sí exige tests con la pirámide estándar (unitario > integración > e2e) y técnica de diseño de casos declarada.

## 8. Métricas de claridad

`(no aplica: sin lógica de negocio propia que justifique métricas formales)`.

## 9. Procedimientos QA

Checklist pre-entrega: build en verde (`./mvnw clean install`), YAML de `config/` válido (parseable, sin claves duplicadas), sin secrets hardcodeados en ningún YAML servido, documentación actualizada si cambia el mecanismo de resolución de configuración (`native` → Git, por ejemplo).

| Severidad | Acción |
| --- | --- |
| Crítico | bloquea el merge |
| Mayor | corregir antes del merge |
| Menor | issue post-merge |

## 10. Seguridad

**Hallazgo real, sin corregir en este cambio**: `application.yml` no configura ninguna autenticación sobre los endpoints de Spring Cloud Config (`/{application}/{profile}`) ni restringe `management.endpoints.web.exposure` (expone `health, info`). Cualquiera con red al puerto `8888` puede leer toda la configuración servida, incluidos secretos si algún YAML de `config/` llegara a tenerlos en claro. Esto es OWASP A02 (configuración insegura) real, no hipotético — pendiente de decisión del equipo (ej. Spring Security Basic Auth; `spring.cloud.config.server.native` sigue funcionando igual detrás de auth).

**OWASP Top 10:2025 — alcance real en este proyecto:**

- **A01 Control de acceso roto**: sin control de acceso implementado — ver hallazgo arriba.
- **A02 Configuración insegura**: ver hallazgo arriba; además, sin HTTPS configurado (HTTP plano en `8888`).
- **A03 Fallos de cadena de suministro**: dependencias resueltas vía `pom.xml` con `spring-cloud-dependencies` como BOM; sin CVEs conocidos al momento de escribir esto — revisar con `dependency-audit` antes de cada release.
- **A04 Fallos criptográficos**: `(no aplica: sin criptografía propia, sin secretos manejados directamente por este servicio)`.
- **A05 Inyección**: `(no aplica: sin DB relacional, sin queries)`.
- **A06 Diseño inseguro**: `(no aplica: servicio de configuración sin flujo de negocio propio)`.
- **A07 Fallos de autenticación**: `(no aplica: sin autenticación de usuarios — ver A01 para el control de acceso al servicio en sí)`.
- **A08 Fallos de integridad**: `(no aplica: sin deserialización de input externo)`.
- **A09 Fallos de logging**: sin logging de eventos de seguridad configurado — no hay eventos de seguridad propios que loggear mientras no exista auth (ver A01).
- **A10 Condiciones excepcionales**: comportamiento por defecto de Spring Boot (errores no filtran stack trace en producción salvo `server.error.include-stacktrace` explícito, que no está seteado).

`(no aplica: sin superficie LLM)`.

## 11. Commits y PR

Conventional Commits v1.0.0 + Gitmoji. Formato: `:emoji: <tipo>(<alcance>)?(!)?: <sujeto>`.

- Idioma: sujeto/cuerpo/footer en español; tipo siempre en inglés (estándar commitlint).
- Sujeto: imperativo presente, minúsculas, sin punto final, ≤72 chars (ideal ≤50). Detalle en el cuerpo.
- Alcance opcional, kebab-case del área tocada: `config`, `native`, `docker`, `ci`, `deps` — omitir si es transversal.
- Cuerpo: tras línea en blanco, qué y por qué, no cómo.
- Footer: tras línea en blanco; `Closes #N`/`Fixes #N`; breaking con `BREAKING CHANGE:` o sufijo `!`.
- Nunca agregar `Co-Authored-By`, firma de agente/IA, ni enlaces de sesión a un commit o PR, salvo pedido explícito del usuario para ese commit puntual.

### 11.0 Reglas de la spec (MUST)

- Header: tipo + alcance opcional + `:` + espacio + sujeto.
- `feat` para funcionalidad nueva, `fix` para corrección de bug.
- Cuerpo: qué y por qué, nunca cómo.
- Footer: `Closes #N` / `Fixes #N` para issues.
- Breaking change: footer `BREAKING CHANGE:` o `!` antes de `:`.

### 11.1 Reglas del proyecto

- Sujeto/cuerpo en español, tipo en inglés.
- Sujeto: imperativo presente, minúsculas, sin punto, ≤72 chars.
- Alcance: lista cerrada — `config`, `native`, `docker`, `ci`, `deps`.
- Enforcement: sin commitlint instalado — el agente valida manualmente.

### 11.2 Gitmoji (adoptado)

Formato: `:emoji: <tipo>(<alcance>)?: <sujeto>`

**Prioridad de selección de emoji (menor a mayor):**

1. **Por defecto según tipo** (gitmoji.dev):

| Tipo | Emoji |
| --- | --- |
| `feat` | ✨ |
| `fix` | 🐛 |
| `docs` | 📝 |
| `style` | 🎨 |
| `refactor` | ♻️ |
| `perf` | ⚡️ |
| `test` | ✅ |
| `build` | 📦️ |
| `ci` | 👷 |
| `chore` | 🔧 |
| `revert` | ⏪️ |

2. **Específico del catálogo** si encaja mejor: 💥 breaking, 🎉 inicio proyecto, 🔥 quitar código, 💫 animaciones/transiciones, 💄 UI, 🔒️ seguridad, 🚀 deploy, ⬆️/⬇️ dependencias, 🙈 gitignore, 🐋 Docker.
3. **Personalizado libre** si el significado no es ambiguo.

**Versionado semántico:** `feat` → MINOR, `fix` → PATCH. Breaking en cualquier tipo → MAJOR (footer `BREAKING CHANGE:` o `!` antes de `:`).

**Ejemplos:**
```
:bug: fix(config): valida YAML sin claves duplicadas antes de servir
:sparkles: feat(config): agrega ms-espacios-comunes.yml
:whale: build(docker): agrega healthcheck al contenedor de config-server
```

### 11.3 Ramas (Git Flow completo — modelo Driessen)

```
main    ●─────●───────────●───────●──────────●───►
         ▲(tag v1.0) ▲(tag v1.0.1)      ▲(tag v1.1.0)
         │  merge    │ merge             │  merge
  release/1.0.0          │        release/1.1.0
       ▲                 │             ▲
       │  merge          │hotfix/1.0.1 │  merge
develop ●──●───●───●──────●─────●───────●───●───►
          \   \   \            \       /
      feature/a  feature/b   feature/c
```

**Ramas permanentes:** `main` (producción, siempre tagueada), `develop` (integración).

**Ramas de soporte:**

| Tipo | Nace de | Mergea a | Naming |
| --- | --- | --- | --- |
| `feature/*` | `develop` | `develop` | `feature/descripcion-corta` |
| `release/*` | `develop` | `main` + `develop` | `release/x.y.z` |
| `hotfix/*` | `main` | `main` + `develop` | `hotfix/descripcion-corta` |
| `support/*` | tag vieja | solo a sí misma | `support/1.x` |

`--no-ff` siempre. Sin force-push a `main`/`develop`. Sin commit directo a `main`/`develop`.

`(sin repositorio git propio inicializado todavía en config-server/ — este esquema es el que se adopta cuando se inicialice, no describe un estado actual con ramas reales)`.

## 12. Límites del agente

**Siempre** (sin pedir permiso): editar código, tests, docs dentro del repo; crear commits locales.

**Preguntar primero**: force-push, `git reset --hard`, agregar/actualizar dependencias, deploy a staging.

**Nunca sin aprobación explícita**:
- Configuración de CI/CD (`.github/workflows/docker-publish.yml`).
- Archivos de secretos o `.env`.
- Deploy a producción.
- Borrar `discovery-server.yml` (único YAML real) o agregar YAML especulativo para servicios sin código todavía (`bff`, `ms-gastos-comunes`) sin decisión explícita del equipo (ver nota de dominio al inicio de este archivo).

## 13. Deploy

```bash
# CI ya existente: .github/workflows/docker-publish.yml
# se dispara en push a main, construye y publica a Docker Hub
# tag: ${DOCKER_USERNAME}/config-server:latest

# build manual local equivalente
docker build -t config-server .
docker run -p 8888:8888 config-server
```

## 14. Monorepo

Este microservicio es parte del workspace Convivo. El `AGENTS.md` de la raíz del workspace (`cloud-native/AGENTS.md`) define reglas transversales (infraestructura, Trello, etc.). Este archivo gana sobre ese para código dentro de `config-server/`.

## 15. Enforcement

Orientativo, no forzado mecánicamente. Las reglas críticas (secretos, auth del endpoint de config) deben reforzarse con CI, no depender solo de este texto.

| Regla | Hook local | CI | Solo texto |
| --- | --- | --- | --- |
| Secretos (§10) | — | sin escaneo configurado hoy | — |
| Build/tests (§4, §7) | — | sin CI de build/test hoy (solo publish) | — |
| Commits (§11) | — | — | validación manual |

## 16. Mantenimiento

Tratar como código. Revisar cuando se cree `bff/` o `ms-gastos-comunes/` (agregar su YAML a `config/`) o cuando se agregue autenticación al endpoint de config (§10).

## 17. Normativa y cumplimiento

Normas que aplican: ISO/IEC 27001 (controles técnicos, ver hallazgo de §10) + Ley 21.719 (si los YAML de `config/` llegaran a contener datos personales, cosa que hoy no ocurre).

### 17.1 ISO/IEC 25010

`(no aplica: microservicio de infraestructura sin interfaz propia — los atributos de interacción y usabilidad no aplican)`

### 17.2 ISO/IEC 27001 — SGSI

| Propiedad | Control mínimo | Evidencia |
| --- | --- | --- |
| Confidencialidad | **ausente** — sin auth ni TLS en el endpoint de config (ver §10) | `(no aplica: brecha conocida, sin evidencia de control)` |
| Integridad | YAML de configuración committeados en el propio repo, sin edición en runtime | historial de git del repo |
| Disponibilidad | sin health check propio más allá de `management.endpoints.web.exposure: health, info` | endpoint `/actuator/health` |

Controles del Anexo A aplicables:

| Control | Dónde vive |
| --- | --- |
| A.8.3 Restricción de acceso | **no implementado** — pendiente (§10) |
| A.8.9 Gestión de configuración | `application.yml` sin defaults inseguros más allá de la falta de auth ya señalada |
| A.8.24 Uso de criptografía | `(no aplica: sin criptografía propia)` |

### 17.3 ISO 9001 / IEEE 730 / ISO/IEC/IEEE 29119

`(no aplica: sin proceso formal de pruebas documentado)`

### 17.4 Cruce con normativa chilena

`(no aplica: config-server no trata datos personales directamente — si algún YAML de config/ llegara a incluirlos, aplicaría la Ley 21.719 igual que en el resto del workspace, ver cloud-native/AGENTS.md §17.4)`
