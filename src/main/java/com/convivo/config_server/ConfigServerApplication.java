package com.convivo.config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Punto de entrada del servidor de configuración centralizada de Convivo.
 * Sirve los YAML de {@code classpath:/config} (perfil {@code native}) a los microservicios.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    /**
     * Arranca el contexto de Spring Boot del config-server.
     *
     * @param args argumentos de línea de comandos pasados a Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}
