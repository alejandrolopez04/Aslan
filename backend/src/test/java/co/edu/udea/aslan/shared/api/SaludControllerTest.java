package co.edu.udea.aslan.shared.api;

import co.edu.udea.aslan.shared.aplicacion.EstadoSalud;
import co.edu.udea.aslan.shared.aplicacion.IValidadorAcceso;
import co.edu.udea.aslan.shared.aplicacion.ServicioSalud;
import co.edu.udea.aslan.shared.infraestructura.ConfiguracionCors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaludController.class)
@Import(ConfiguracionCors.class)
@TestPropertySource(properties = "aslan.cors.origenes=http://localhost:8443")
class SaludControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IValidadorAcceso validador;

    @MockitoBean
    private ServicioSalud servicioSalud;

    @Test
    void respondeOkCuandoTodoEstaArriba() throws Exception {
        when(servicioSalud.consultarEstado()).thenReturn(EstadoSalud.con(true));

        mockMvc.perform(get("/api/salud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("UP"))
                .andExpect(jsonPath("$.baseDatos").value("UP"));
    }

    @Test
    void respondeServicioNoDisponibleCuandoLaBaseDeDatosEstaCaida() throws Exception {
        when(servicioSalud.consultarEstado()).thenReturn(EstadoSalud.con(false));

        mockMvc.perform(get("/api/salud"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.baseDatos").value("DOWN"));
    }

    @Test
    void permiteElOrigenDelFront() throws Exception {
        mockMvc.perform(options("/api/salud")
                        .header("Origin", "http://localhost:8443")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8443"));
    }

    @Test
    void rechazaOrigenesNoConfigurados() throws Exception {
        mockMvc.perform(options("/api/salud")
                        .header("Origin", "https://sitio-desconocido.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void devuelveErrorEnFormatoEstandarParaRutasInexistentes() throws Exception {
        mockMvc.perform(get("/api/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe."));
    }
}
