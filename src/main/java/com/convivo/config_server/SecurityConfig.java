package com.convivo.config_server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Seguridad HTTP del config-server: Basic Auth en todos los endpoints salvo {@code /actuator/health}.
 */
@Configuration
public class SecurityConfig {

    /**
     * Cadena de filtros: health público para healthchecks, el resto exige credenciales
     * ({@code CONFIG_SERVER_USER}/{@code CONFIG_SERVER_PASSWORD}).
     *
     * @param http builder de seguridad HTTP
     * @return cadena de filtros construida
     * @throws Exception si la configuración de seguridad falla
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/encrypt/**", "/decrypt/**"))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'; sandbox"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults());
        return http.build();
    }
}
