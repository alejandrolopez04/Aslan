package co.edu.udea.aslan.usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class InicioSesionIntegracionTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void emiteTokenYBloqueaTrasCincoIntentosFallidos() throws Exception {
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombre":"Ana","correo":"ana@aslan.app","contrasena":"segura123"}"""))
                .andExpect(status().isCreated());

        login("segura123").andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM sesiones_autenticacion WHERE activa", Integer.class)).isEqualTo(1);

        for (int i = 0; i < 4; i++) {
            login("mala").andExpect(status().isUnauthorized());
        }
        login("mala").andExpect(status().isLocked());
        login("segura123").andExpect(status().isLocked())
                .andExpect(jsonPath("$.detalles.bloqueadaHasta").isNotEmpty());
    }

    private ResultActions login(String contrasena) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"ana@aslan.app\",\"contrasena\":\"" + contrasena + "\"}"));
    }
}
