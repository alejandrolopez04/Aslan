package co.edu.udea.aslan.materias.api;

import co.edu.udea.aslan.materias.aplicacion.ServicioMaterias;
import co.edu.udea.aslan.materias.dominio.EstadoTema;
import co.edu.udea.aslan.materias.dominio.Materia;
import co.edu.udea.aslan.materias.dominio.MateriaDuplicadaException;
import co.edu.udea.aslan.materias.dominio.NombreObligatorioException;
import co.edu.udea.aslan.materias.dominio.Prioridad;
import co.edu.udea.aslan.materias.dominio.Tema;
import co.edu.udea.aslan.shared.aplicacion.IValidadorAcceso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MateriasController.class)
@Import(MateriasControllerTest.RelojFijo.class)
class MateriasControllerTest {

    static class RelojFijo {
        @org.springframework.context.annotation.Bean
        Clock reloj() {
            return Clock.fixed(Instant.parse("2026-11-05T15:00:00Z"), ZoneOffset.UTC);
        }
    }

    private static final String TOKEN = "Bearer token-valido";
    private final UUID estudiante = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioMaterias servicio;

    @MockitoBean
    private IValidadorAcceso validador;

    @BeforeEach
    void autenticar() {
        when(validador.validarAcceso("token-valido")).thenReturn(Optional.of(estudiante));
    }

    @Test
    void crearMateriaDevuelve201() throws Exception {
        Materia materia = Materia.crear(estudiante, "Física", "blue", "science");
        when(servicio.crearMateria(estudiante, "Física", "blue", "science", false)).thenReturn(materia);

        mockMvc.perform(post("/api/materias").header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Física\",\"color\":\"blue\",\"icono\":\"science\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Física"))
                .andExpect(jsonPath("$.progreso").value(0.0))
                .andExpect(jsonPath("$.temas").isEmpty());
    }

    @Test
    void nombreVacioDevuelve400() throws Exception {
        when(servicio.crearMateria(any(), any(), anyString(), anyString(), anyBoolean()))
                .thenThrow(new NombreObligatorioException("Ingresa un nombre para la materia."));

        mockMvc.perform(post("/api/materias").header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"color\":\"blue\",\"icono\":\"science\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("NOMBRE_OBLIGATORIO"))
                .andExpect(jsonPath("$.mensaje").value("Ingresa un nombre para la materia."));
    }

    @Test
    void nombreDuplicadoDevuelve409ComoAdvertencia() throws Exception {
        when(servicio.crearMateria(any(), any(), anyString(), anyString(), eq(false)))
                .thenThrow(new MateriaDuplicadaException());

        mockMvc.perform(post("/api/materias").header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Física\",\"color\":\"blue\",\"icono\":\"science\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("MATERIA_DUPLICADA"));
    }

    @Test
    void crearTemaSinFechaLoMarcaSinVencimiento() throws Exception {
        UUID materiaId = UUID.randomUUID();
        Tema tema = Tema.nuevo("Óptica", EstadoTema.PENDIENTE, Prioridad.ALTA, null, "");
        when(servicio.crearTema(eq(estudiante), eq(materiaId), any())).thenReturn(tema);

        mockMvc.perform(post("/api/materias/{id}/temas", materiaId).header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Óptica\",\"estado\":\"PENDIENTE\",\"prioridad\":\"ALTA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sinVencimiento").value(true))
                .andExpect(jsonPath("$.fechaLimite").doesNotExist())
                .andExpect(jsonPath("$.vencido").value(false));
    }

    @Test
    void listarMuestraLasMateriasConSusTemas() throws Exception {
        Materia materia = Materia.crear(estudiante, "Física", "blue", "science");
        materia.agregarTema(Tema.nuevo("Óptica", EstadoTema.COMPLETADO, Prioridad.BAJA, null, ""));
        when(servicio.listarMaterias(estudiante)).thenReturn(List.of(materia));

        mockMvc.perform(get("/api/materias").header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Física"))
                .andExpect(jsonPath("$[0].progreso").value(1.0))
                .andExpect(jsonPath("$[0].temas[0].nombre").value("Óptica"));
    }

    @Test
    void sinTokenValidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/materias").header("Authorization", "Bearer cerrado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        mockMvc.perform(get("/api/materias"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(servicio);
    }
}
