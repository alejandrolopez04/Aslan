package co.edu.udea.aslan.usuarios.api;

import co.edu.udea.aslan.usuarios.aplicacion.CuentaRegistrada;
import co.edu.udea.aslan.usuarios.aplicacion.ServicioRegistro;
import co.edu.udea.aslan.usuarios.aplicacion.SolicitudRegistro;
import co.edu.udea.aslan.usuarios.dominio.ContrasenaInseguraException;
import co.edu.udea.aslan.usuarios.dominio.CorreoEnUsoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutenticacionController.class)
class AutenticacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioRegistro servicioRegistro;

    @Test
    void registroExitosoDevuelve201ConLaCuenta() throws Exception {
        UUID id = UUID.randomUUID();
        when(servicioRegistro.registrar(new SolicitudRegistro("María Parra", "maria@udea.edu.co", "segura123")))
                .thenReturn(new CuentaRegistrada(id, "María Parra", "maria@udea.edu.co"));

        registrar("""
                {"nombre":"María Parra","correo":"maria@udea.edu.co","contrasena":"segura123"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nombre").value("María Parra"))
                .andExpect(jsonPath("$.correo").value("maria@udea.edu.co"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void correoEnUsoDevuelve409() throws Exception {
        when(servicioRegistro.registrar(any())).thenThrow(new CorreoEnUsoException());

        registrar("""
                {"nombre":"Alejandro","correo":"alejo@aslan.app","contrasena":"segura123"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_EN_USO"))
                .andExpect(jsonPath("$.mensaje").value(
                        "Este correo ya está en uso. Intenta iniciar sesión o utiliza otro correo para crear tu cuenta."));
    }

    @Test
    void contrasenaInseguraDevuelve400() throws Exception {
        when(servicioRegistro.registrar(any())).thenThrow(new ContrasenaInseguraException());

        registrar("""
                {"nombre":"María","correo":"maria@udea.edu.co","contrasena":"corta"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CONTRASENA_INSEGURA"));
    }

    @Test
    void nombreVacioDevuelve400SinLlamarAlServicio() throws Exception {
        registrar("""
                {"nombre":"  ","correo":"maria@udea.edu.co","contrasena":"segura123"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.mensaje").value("Ingresa tu nombre."));
        verifyNoInteractions(servicioRegistro);
    }

    @Test
    void correoInvalidoDevuelve400SinLlamarAlServicio() throws Exception {
        registrar("""
                {"nombre":"María","correo":"maria@sin-dominio","contrasena":"segura123"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ingresa un correo electrónico válido."));
        verifyNoInteractions(servicioRegistro);
    }

    @Test
    void cuerpoMalFormadoDevuelve400() throws Exception {
        registrar("{no es json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    private ResultActions registrar(String json) throws Exception {
        return mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
