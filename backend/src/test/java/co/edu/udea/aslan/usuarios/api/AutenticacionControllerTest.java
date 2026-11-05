package co.edu.udea.aslan.usuarios.api;

import co.edu.udea.aslan.usuarios.aplicacion.ServicioAutenticacion;
import co.edu.udea.aslan.usuarios.dominio.ContrasenaInseguraException;
import co.edu.udea.aslan.usuarios.dominio.CorreoEnUsoException;
import co.edu.udea.aslan.usuarios.dominio.CredencialesInvalidasException;
import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.CuentaBloqueadaException;
import co.edu.udea.aslan.usuarios.dominio.Estudiante;
import co.edu.udea.aslan.usuarios.dominio.SesionAutenticacion;
import co.edu.udea.aslan.usuarios.dominio.TokenFijo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutenticacionController.class)
class AutenticacionControllerTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioAutenticacion servicio;

    @Test
    void registroExitosoDevuelve201ConElEstudiante() throws Exception {
        Estudiante estudiante = Estudiante.registrar("María Parra", AHORA);
        when(servicio.registrar("María Parra", "Maria@UdeA.edu.co", "segura123")).thenReturn(estudiante);

        enviar("/api/auth/registro", """
                {"nombre":"María Parra","correo":"Maria@UdeA.edu.co","contrasena":"segura123"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(estudiante.getId().toString()))
                .andExpect(jsonPath("$.nombre").value("María Parra"))
                .andExpect(jsonPath("$.correo").value("maria@udea.edu.co"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void correoEnUsoDevuelve409() throws Exception {
        when(servicio.registrar(anyString(), anyString(), anyString())).thenThrow(new CorreoEnUsoException());

        enviar("/api/auth/registro", """
                {"nombre":"Alejandro","correo":"alejo@aslan.app","contrasena":"segura123"}""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_EN_USO"))
                .andExpect(jsonPath("$.mensaje").value(
                        "Este correo ya está en uso. Intenta iniciar sesión o utiliza otro correo para crear tu cuenta."));
    }

    @Test
    void contrasenaInseguraDevuelve400ConLosErrores() throws Exception {
        when(servicio.registrar(anyString(), anyString(), anyString())).thenThrow(
                new ContrasenaInseguraException("Requisitos.", List.of("Debe incluir al menos un número.")));

        enviar("/api/auth/registro", """
                {"nombre":"María","correo":"maria@udea.edu.co","contrasena":"cortaaaa"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CONTRASENA_INSEGURA"))
                .andExpect(jsonPath("$.detalles.errores[0]").value("Debe incluir al menos un número."));
    }

    @Test
    void nombreVacioDevuelve400SinLlamarAlServicio() throws Exception {
        enviar("/api/auth/registro", """
                {"nombre":"  ","correo":"maria@udea.edu.co","contrasena":"segura123"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.mensaje").value("Ingresa tu nombre."));
        verifyNoInteractions(servicio);
    }

    @Test
    void correoInvalidoDevuelve400SinLlamarAlServicio() throws Exception {
        enviar("/api/auth/registro", """
                {"nombre":"María","correo":"maria@sin-dominio","contrasena":"segura123"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ingresa un correo electrónico válido."));
        verifyNoInteractions(servicio);
    }

    @Test
    void cuerpoMalFormadoDevuelve400() throws Exception {
        enviar("/api/auth/registro", "{no es json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void loginExitosoDevuelveElJwtYSuExpiracion() throws Exception {
        Estudiante estudiante = Estudiante.registrar("María", AHORA);
        Cuenta cuenta = Cuenta.abrir(estudiante.getId(), "maria@udea.edu.co", "hash");
        SesionAutenticacion sesion = SesionAutenticacion.abrir(estudiante, cuenta, AHORA, new TokenFijo("jwt-firmado"));
        when(servicio.iniciarSesion("maria@udea.edu.co", "segura123")).thenReturn(sesion);

        enviar("/api/auth/login", """
                {"correo":"maria@udea.edu.co","contrasena":"segura123"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-firmado"))
                .andExpect(jsonPath("$.expiracion").value(AHORA.plus(Duration.ofHours(2)).toString()));
    }

    @Test
    void credencialesIncorrectasDevuelven401() throws Exception {
        when(servicio.iniciarSesion(anyString(), anyString())).thenThrow(new CredencialesInvalidasException());

        enviar("/api/auth/login", """
                {"correo":"maria@udea.edu.co","contrasena":"mala"}""")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña inválida."));
    }

    @Test
    void cuentaBloqueadaDevuelve423ConLaHoraDeDesbloqueo() throws Exception {
        Instant hasta = AHORA.plus(Duration.ofMinutes(15));
        when(servicio.iniciarSesion(anyString(), anyString())).thenThrow(new CuentaBloqueadaException(hasta));

        enviar("/api/auth/login", """
                {"correo":"maria@udea.edu.co","contrasena":"mala"}""")
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.codigo").value("CUENTA_BLOQUEADA"))
                .andExpect(jsonPath("$.detalles.bloqueadaHasta").value(hasta.toString()));
    }

    @Test
    void loginSinCamposDevuelve400SinLlamarAlServicio() throws Exception {
        enviar("/api/auth/login", "{\"correo\":\"\",\"contrasena\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña inválida."));
        verifyNoInteractions(servicio);
    }

    @Test
    void cerrarSesionInvalidaElTokenYDevuelve204() throws Exception {
        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer jwt-firmado"))
                .andExpect(status().isNoContent());
        verify(servicio).cerrarSesion("jwt-firmado");
    }

    @Test
    void cerrarSesionSinTokenTambienDevuelve204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent());
        verifyNoInteractions(servicio);
    }

    private ResultActions enviar(String ruta, String json) throws Exception {
        return mockMvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
