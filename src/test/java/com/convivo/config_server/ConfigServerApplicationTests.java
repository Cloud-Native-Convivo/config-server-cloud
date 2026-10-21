package com.convivo.config_server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.security.user.name=test_user",
    "server.ssl.enabled=false"
})
class ConfigServerApplicationTests {

    /** Password guardada como hash BCrypt, igual que en produccion; el cliente envia texto plano. */
    @DynamicPropertySource
    static void credenciales(DynamicPropertyRegistry registry) {
        registry.add("spring.security.user.password",
            () -> "{bcrypt}" + new BCryptPasswordEncoder().encode("test_pass"));
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void contextLoads() {
        // Smoke test: valida que el contexto de Spring Boot inicialice correctamente.
    }

    @Test
    void healthEsPublico() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void configSinCredencialesDevuelve401() throws Exception {
        mvc.perform(get("/ms-espacios-comunes/default")).andExpect(status().isUnauthorized());
    }

    @Test
    void configConCredencialesDevuelve200() throws Exception {
        mvc.perform(get("/ms-espacios-comunes/default").with(httpBasic("test_user", "test_pass")))
            .andExpect(status().isOk());
    }

}
