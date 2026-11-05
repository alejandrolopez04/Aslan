package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.aplicacion.AccionCiclo;
import co.edu.udea.aslan.sesiones.aplicacion.ServicioConfiguracionPomodoro;
import co.edu.udea.aslan.sesiones.aplicacion.ServicioSesionEstudio;
import co.edu.udea.aslan.sesiones.dominio.ConfiguracionPomodoro;
import co.edu.udea.aslan.sesiones.dominio.ConfirmacionRequeridaException;
import co.edu.udea.aslan.sesiones.dominio.DuracionFueraDeRangoException;
import co.edu.udea.aslan.sesiones.dominio.SeleccionIncompletaException;
import co.edu.udea.aslan.sesiones.dominio.SesionEstudio;
import co.edu.udea.aslan.sesiones.dominio.TipoSonido;
import co.edu.udea.aslan.shared.aplicacion.IValidadorAcceso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SesionesEstudioController.class, ConfiguracionPomodoroController.class})
class SesionesEstudioControllerTest {

    private final UUID estudiante = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioSesionEstudio servicio;

    @MockitoBean
    private ServicioConfiguracionPomodoro configuraciones;

    @MockitoBean
    private IValidadorAcceso validador;

    @BeforeEach
    void autenticar() {
        when(validador.validarAcceso("token")).thenReturn(Optional.of(estudiante));
    }

    @Test
    void configurarDuracionValidaDevuelveLaConfiguracion() throws Exception {
        ConfiguracionPomodoro cfg = ConfiguracionPomodoro.estandar(estudiante);
        cfg.establecerDuracion(40);
        cfg.desactivarMonitoreo();
        when(configuraciones.configurar(estudiante, 40, false)).thenReturn(cfg);

        enviar(put("/api/configuracion-pomodoro"), "{\"duracionTrabajoMin\":40,\"monitoreoActivo\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duracionTrabajoMin").value(40))
                .andExpect(jsonPath("$.monitoreoActivo").value(false));
    }

    @Test
    void duracionFueraDeRangoIndicaElRango() throws Exception {
        when(configuraciones.configurar(estudiante, 70, true)).thenThrow(new DuracionFueraDeRangoException(15, 60));

        enviar(put("/api/configuracion-pomodoro"), "{\"duracionTrabajoMin\":70,\"monitoreoActivo\":true}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DURACION_FUERA_DE_RANGO"))
                .andExpect(jsonPath("$.mensaje").value("La duración debe estar entre 15 y 60 minutos."))
                .andExpect(jsonPath("$.detalles.minimo").value(15));
    }

    @Test
    void iniciarSesionDevuelve201ConElPrimerCiclo() throws Exception {
        UUID materia = UUID.randomUUID();
        UUID tema = UUID.randomUUID();
        SesionEstudio sesion = SesionEstudio.iniciar(estudiante, materia, tema, TipoSonido.LOFI, "Pomodoro clásico",
                ConfiguracionPomodoro.estandar(estudiante), Instant.parse("2026-11-05T15:00:00Z"));
        when(servicio.iniciarSesion(estudiante, materia, tema, TipoSonido.LOFI)).thenReturn(sesion);

        enviar(post("/api/sesiones"), "{\"materiaId\":\"" + materia + "\",\"temaId\":\"" + tema
                + "\",\"ambiente\":\"LOFI\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$.cicloActual.tipo").value("TRABAJO"))
                .andExpect(jsonPath("$.cicloActual.duracionPlaneadaSeg").value(1500))
                .andExpect(jsonPath("$.registro").doesNotExist());
    }

    @Test
    void seleccionIncompletaDevuelve400ConLosFaltantes() throws Exception {
        when(servicio.iniciarSesion(estudiante, null, null, null))
                .thenThrow(new SeleccionIncompletaException(List.of("materia", "tema")));

        enviar(post("/api/sesiones"), "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SELECCION_INCOMPLETA"))
                .andExpect(jsonPath("$.detalles.faltantes[1]").value("tema"));
    }

    @Test
    void laAccionDelCicloAceptaMinusculas() throws Exception {
        UUID sesionId = UUID.randomUUID();
        SesionEstudio sesion = SesionEstudio.iniciar(estudiante, UUID.randomUUID(), UUID.randomUUID(), null, null,
                ConfiguracionPomodoro.estandar(estudiante), Instant.now());
        when(servicio.ejecutarAccion(estudiante, sesionId, AccionCiclo.PAUSAR, 300)).thenReturn(sesion);

        enviar(patch("/api/sesiones/{id}/ciclo", sesionId), "{\"accion\":\"pausar\",\"transcurridoSeg\":300}")
                .andExpect(status().isOk());
    }

    @Test
    void finalizarConTiempoActivoPideConfirmacion() throws Exception {
        UUID sesionId = UUID.randomUUID();
        when(servicio.finalizarSesion(eq(estudiante), eq(sesionId), anyInt(), eq(false), anyBoolean()))
                .thenThrow(new ConfirmacionRequeridaException());

        enviar(post("/api/sesiones/{id}/finalizar", sesionId), "{\"transcurridoSeg\":120}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFIRMACION_REQUERIDA"));
    }

    @Test
    void sinSesionAbiertaDevuelve204() throws Exception {
        when(servicio.sesionAbierta(estudiante)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sesiones/activa").header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String json) throws Exception {
        return mockMvc.perform(peticion.header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
