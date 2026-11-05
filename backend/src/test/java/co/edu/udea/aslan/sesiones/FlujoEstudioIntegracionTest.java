package co.edu.udea.aslan.sesiones;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class FlujoEstudioIntegracionTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private JdbcTemplate jdbc;

    private String token;

    @Test
    void estudiaUnTemaYCierraSesion() throws Exception {
        enviar(post("/api/auth/registro"), """
                {"nombre":"Lina","correo":"lina@aslan.app","contrasena":"segura123"}""")
                .andExpect(status().isCreated());
        token = leer(enviar(post("/api/auth/login"), """
                {"correo":"lina@aslan.app","contrasena":"segura123"}""")).get("token").asText();

        String materia = leer(enviar(post("/api/materias"), """
                {"nombre":"Física","color":"blue","icono":"science"}""")).get("id").asText();
        String tema = leer(enviar(post("/api/materias/" + materia + "/temas"), """
                {"nombre":"Óptica","estado":"PENDIENTE","prioridad":"ALTA"}""")).get("id").asText();

        JsonNode sesion = leer(enviar(post("/api/sesiones"),
                "{\"materiaId\":\"" + materia + "\",\"temaId\":\"" + tema + "\"}"));
        assertThat(sesion.get("tecnica").asText()).isEqualTo("Pomodoro clásico");
        String sesionId = sesion.get("id").asText();

        enviar(patch("/api/sesiones/" + sesionId + "/ciclo"), "{\"accion\":\"iniciar\"}");
        enviar(patch("/api/sesiones/" + sesionId + "/ciclo"), "{\"accion\":\"siguiente\",\"transcurridoSeg\":1500}")
                .andExpect(jsonPath("$.cicloActual.tipo").value("DESCANSO_CORTO"));
        enviar(post("/api/sesiones/" + sesionId + "/finalizar"), "{\"transcurridoSeg\":60,\"confirmado\":true}")
                .andExpect(jsonPath("$.estado").value("FINALIZADA_ANTICIPADA"))
                .andExpect(jsonPath("$.registro.ciclosCompletados").value(1));

        assertThat(jdbc.queryForObject("SELECT racha_actual FROM rachas_estudio", Integer.class)).isEqualTo(1);
        enviar(get("/api/sesiones/sugerencia?materiaId=" + materia + "&temaId=" + tema), "")
                .andExpect(jsonPath("$.personalizada").value(true));

        enviar(post("/api/auth/logout"), "").andExpect(status().isNoContent());
        enviar(get("/api/materias"), "").andExpect(status().isUnauthorized());
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String cuerpo) throws Exception {
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private JsonNode leer(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString());
    }
}
