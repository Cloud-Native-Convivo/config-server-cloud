# config-server-cloud

Servidor centralizado de configuración de Spring Cloud Config para el ecosistema microservicios Convivo.

## Descripción

`config-server-cloud` es un servicio basado en Java 25 y Spring Boot 4.1.1 con Spring Cloud Config Server. Proporciona configuración centralizada y versionada para los servicios de la plataforma (`ms-espacios-comunes`, `ms-gastos-comunes`, etc.) a través de perfiles y endpoints cifrados con Basic Auth.

## Verificación Preventiva Local Pre-push

Antes de abrir un Pull Request o enviar commits al repositorio remoto, ejecutar las validaciones con el runner nativo de Maven:

```bash
# 1. Ejecutar pruebas unitarias y validación Gitmoji
./mvnw clean test

# 2. Ejecutar validación completa de empaquetado, enforcer y cobertura JaCoCo
./mvnw verify
```

> **Nota de CI**: `src/test/java/com/convivo/config_server/GitCommitConventionTest.java` asegura que todos los mensajes de commit cumplan la convención Gitmoji + Conventional Commits requerida por `pr-quality.yml`.
