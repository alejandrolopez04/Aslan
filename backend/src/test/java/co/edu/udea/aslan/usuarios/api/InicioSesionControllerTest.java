package co.edu.udea.aslan.usuarios.api;

import co.edu.udea.aslan.usuarios.aplicacion.ResumenCuenta;
import co.edu.udea.aslan.usuarios.aplicacion.ServicioAutenticacion;
import co.edu.udea.aslan.usuarios.aplicacion.ServicioRegistro;
import co.edu.udea.aslan.usuarios.aplicacion.SesionIniciada;
import co.edu.udea.aslan.usuarios.dominio.CredencialesInvalidasException;
import co.edu.udea.aslan.usuarios.dominio.CuentaBloqueadaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutenticacionController.class)
class InicioSesionControllerTest {

    private static final Instant EXPIRA = Instant.parse("2026-11-05T17:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioRegistro servicioRegistro;

    @MockitoBean
    private ServicioAutenticacion servicioAutenticacion;

    @Test
    void loginExitosoDevuelveTokenYCuenta() throws Exception {
        UUID id = UUID.randomUUID();
        when(servicioAutenticacion.iniciarSesion("maria@udea.edu.co", "segura123"))
                .thenReturn(new SesionIniciada("token-123", EXPIRA, new ResumenCuenta(id, "María", "maria@udea.edu.co")));

        login("""
                {"correo":"maria@udea.edu.co","contrasena":"segura123"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.expiraEn").value("2026-11-05T17:00:00Z"))
                .andExpect(jsonPath("$.cuenta.id").value(id.toString()))
                .andExpect(jsonPath("$.cuenta.nombre").value("María"));
    }

    @Test
    void credencialesInvalidasDevuelve401() throws Exception {
        when(servicioAutenticacion.iniciarSesion(anyString(), anyString()))
                .thenThrow(new CredencialesInvalidasException());

        login("""
                {"correo":"maria@udea.edu.co","contrasena":"mala"}""")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña inválida."))
                .andExpect(jsonPath("$.detalles").doesNotExist());
    }

    @Test
    void cuentaBloqueadaDevuelve423ConLaHoraDeDesbloqueo() throws Exception {
        when(servicioAutenticacion.iniciarSesion(anyString(), anyString()))
                .thenThrow(new CuentaBloqueadaException(EXPIRA));

        login("""
                {"correo":"maria@udea.edu.co","contrasena":"mala"}""")
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.codigo").value("CUENTA_BLOQUEADA"))
                .andExpect(jsonPath("$.mensaje").value(
                        "Alcanzaste el límite de intentos. Espera antes de volver a intentarlo."))
                .andExpect(jsonPath("$.detalles.bloqueadaHasta").value("2026-11-05T17:00:00Z"));
    }

    @Test
    void camposVaciosDevuelven400SinLlamarAlServicio() throws Exception {
        login("""
                {"correo":"","contrasena":""}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña inválida."));
        verifyNoInteractions(servicioAutenticacion);
    }

    private ResultActions login(String json) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
