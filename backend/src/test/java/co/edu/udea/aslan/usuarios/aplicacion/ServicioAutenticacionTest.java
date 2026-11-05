package co.edu.udea.aslan.usuarios.aplicacion;

import co.edu.udea.aslan.shared.dominio.IRepositorio;
import co.edu.udea.aslan.usuarios.dominio.ContrasenaInseguraException;
import co.edu.udea.aslan.usuarios.dominio.CorreoEnUsoException;
import co.edu.udea.aslan.usuarios.dominio.CredencialesInvalidasException;
import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.CuentaBloqueadaException;
import co.edu.udea.aslan.usuarios.dominio.Estudiante;
import co.edu.udea.aslan.usuarios.dominio.ICuentaRepositorio;
import co.edu.udea.aslan.usuarios.dominio.IPoliticaContrasena;
import co.edu.udea.aslan.usuarios.dominio.IServicioHash;
import co.edu.udea.aslan.usuarios.dominio.IServicioTokens;
import co.edu.udea.aslan.usuarios.dominio.ReclamosToken;
import co.edu.udea.aslan.usuarios.dominio.ResultadoValidacion;
import co.edu.udea.aslan.usuarios.dominio.SesionAutenticacion;
import co.edu.udea.aslan.usuarios.dominio.TokenFijo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioAutenticacionTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");
    private static final String REQUISITOS = "Debe tener al menos 8 caracteres, una letra y un número.";

    @Mock
    private ICuentaRepositorio cuentas;

    @Mock
    private IRepositorio<Estudiante> estudiantes;

    @Mock
    private IRepositorio<SesionAutenticacion> sesiones;

    @Mock
    private IServicioHash hasher;

    @Mock
    private IPoliticaContrasena politica;

    @Mock
    private IServicioTokens tokens;

    private ServicioAutenticacion servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioAutenticacion(cuentas, estudiantes, sesiones, hasher, politica, tokens,
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Nested
    class HU01Registrarse {

        @Test
        void registroExitosoCreaEstudianteYCuentaConLaContrasenaCifrada() {
            when(politica.validar("segura123")).thenReturn(ResultadoValidacion.con(List.of()));
            when(cuentas.existeCorreo("maria@udea.edu.co")).thenReturn(false);
            when(hasher.generarHash("segura123")).thenReturn("hash-bcrypt");

            Estudiante estudiante = servicio.registrar("María Parra", "Maria@UdeA.edu.co", "segura123");

            ArgumentCaptor<Cuenta> cuenta = ArgumentCaptor.forClass(Cuenta.class);
            verify(estudiantes).guardar(estudiante);
            verify(cuentas).guardar(cuenta.capture());
            assertThat(estudiante.getNombre()).isEqualTo("María Parra");
            assertThat(estudiante.getFechaRegistro()).isEqualTo(AHORA);
            assertThat(cuenta.getValue().getEstudianteId()).isEqualTo(estudiante.getId());
            assertThat(cuenta.getValue().getCorreo()).isEqualTo("maria@udea.edu.co");
            assertThat(cuenta.getValue().getContrasenaHash()).isEqualTo("hash-bcrypt");
        }

        @Test
        void correoYaRegistradoNoGuardaNada() {
            when(politica.validar("segura123")).thenReturn(ResultadoValidacion.con(List.of()));
            when(cuentas.existeCorreo("alejo@aslan.app")).thenReturn(true);

            assertThatThrownBy(() -> servicio.registrar("Alejandro", "ALEJO@aslan.app", "segura123"))
                    .isInstanceOf(CorreoEnUsoException.class)
                    .hasMessageStartingWith("Este correo ya está en uso");
            verifyNoInteractions(estudiantes);
            verify(cuentas, never()).guardar(any());
        }

        @Test
        void contrasenaInvalidaMuestraLosRequisitosDeLaPolitica() {
            when(politica.validar("corta")).thenReturn(
                    ResultadoValidacion.con(List.of("Debe incluir al menos un número.")));
            when(politica.describirRequisitos()).thenReturn(REQUISITOS);

            assertThatThrownBy(() -> servicio.registrar("María", "maria@udea.edu.co", "corta"))
                    .isInstanceOf(ContrasenaInseguraException.class)
                    .hasMessage(REQUISITOS)
                    .satisfies(e -> assertThat(((ContrasenaInseguraException) e).getDetalles())
                            .containsEntry("errores", List.of("Debe incluir al menos un número.")));
            verifyNoInteractions(cuentas, estudiantes, hasher);
        }
    }

    @Nested
    class HU02IniciarSesion {

        private final Estudiante estudiante = Estudiante.registrar("María", AHORA.minus(Duration.ofDays(1)));
        private final Cuenta cuenta = Cuenta.abrir(estudiante.getId(), "maria@udea.edu.co", "hash");

        @Test
        void inicioExitosoAbreUnaSesionConJwtDeDosHoras() {
            when(cuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));
            when(hasher.coincide("segura123", "hash")).thenReturn(true);
            when(estudiantes.buscarPorId(estudiante.getId())).thenReturn(Optional.of(estudiante));
            when(tokens.firmar(any())).thenReturn("jwt-firmado");

            SesionAutenticacion sesion = servicio.iniciarSesion(" MARIA@udea.edu.co ", "segura123");

            assertThat(sesion.getToken()).isEqualTo("jwt-firmado");
            assertThat(sesion.getEstudianteId()).isEqualTo(estudiante.getId());
            assertThat(sesion.getExpiracion()).isEqualTo(AHORA.plus(Duration.ofHours(2)));
            verify(sesiones).guardar(sesion);
        }

        @Test
        void credencialesIncorrectasSumanUnIntento() {
            when(cuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));
            when(hasher.coincide("mala", "hash")).thenReturn(false);

            assertThatThrownBy(() -> servicio.iniciarSesion("maria@udea.edu.co", "mala"))
                    .isInstanceOf(CredencialesInvalidasException.class)
                    .hasMessage("Correo o contraseña inválida.");
            assertThat(cuenta.getIntentosFallidos()).isEqualTo(1);
            verify(cuentas).guardar(cuenta);
            verifyNoInteractions(sesiones, tokens);
        }

        @Test
        void correoInexistenteRespondeComoCredencialesIncorrectas() {
            when(cuentas.buscarPorCorreo("nadie@udea.edu.co")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicio.iniciarSesion("nadie@udea.edu.co", "segura123"))
                    .isInstanceOf(CredencialesInvalidasException.class);
        }

        @Test
        void multiplesIntentosFallidosBloqueanQuinceMinutos() {
            for (int i = 0; i < 4; i++) {
                cuenta.registrarIntentoFallido(AHORA);
            }
            when(cuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));
            when(hasher.coincide("mala", "hash")).thenReturn(false);

            assertThatThrownBy(() -> servicio.iniciarSesion("maria@udea.edu.co", "mala"))
                    .isInstanceOf(CuentaBloqueadaException.class)
                    .satisfies(e -> assertThat(((CuentaBloqueadaException) e).getDetalles())
                            .containsEntry("bloqueadaHasta", AHORA.plus(Duration.ofMinutes(15)).toString()));
        }

        @Test
        void cuentaBloqueadaRechazaInclusoLaContrasenaCorrecta() {
            for (int i = 0; i < 5; i++) {
                cuenta.registrarIntentoFallido(AHORA.minus(Duration.ofMinutes(5)));
            }
            when(cuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));

            assertThatThrownBy(() -> servicio.iniciarSesion("maria@udea.edu.co", "segura123"))
                    .isInstanceOf(CuentaBloqueadaException.class);
            verify(hasher, never()).coincide(anyString(), anyString());
        }

        @Test
        void trasQuinceMinutosPuedeVolverAIngresar() {
            for (int i = 0; i < 5; i++) {
                cuenta.registrarIntentoFallido(AHORA.minus(Duration.ofMinutes(15)));
            }
            when(cuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));
            when(hasher.coincide("segura123", "hash")).thenReturn(true);
            when(estudiantes.buscarPorId(estudiante.getId())).thenReturn(Optional.of(estudiante));
            when(tokens.firmar(any())).thenReturn("jwt");

            assertThat(servicio.iniciarSesion("maria@udea.edu.co", "segura123").getToken()).isEqualTo("jwt");
            assertThat(cuenta.getIntentosFallidos()).isZero();
        }
    }

    @Nested
    class HU03CerrarSesion {

        private final Estudiante estudiante = Estudiante.registrar("María", AHORA);
        private final Cuenta cuenta = Cuenta.abrir(estudiante.getId(), "maria@udea.edu.co", "hash");
        private final SesionAutenticacion sesion = SesionAutenticacion.abrir(estudiante, cuenta, AHORA, new TokenFijo("jwt"));
        private final ReclamosToken reclamos = new ReclamosToken(sesion.getId(), estudiante.getId(), "María",
                "maria@udea.edu.co", AHORA, sesion.getExpiracion());

        @Test
        void usuarioAutenticadoAccedeConSuToken() {
            when(tokens.leer("jwt")).thenReturn(Optional.of(reclamos));
            when(sesiones.buscarPorId(sesion.getId())).thenReturn(Optional.of(sesion));

            assertThat(servicio.validarAcceso("jwt")).contains(estudiante.getId());
        }

        @Test
        void cierreDeSesionExitosoInvalidaLaSesion() {
            when(tokens.leer("jwt")).thenReturn(Optional.of(reclamos));
            when(sesiones.buscarPorId(sesion.getId())).thenReturn(Optional.of(sesion));

            servicio.cerrarSesion("jwt");

            assertThat(sesion.isActiva()).isFalse();
            verify(sesiones).guardar(sesion);
        }

        @Test
        void trasCerrarSesionElTokenYaNoDaAcceso() {
            when(tokens.leer("jwt")).thenReturn(Optional.of(reclamos));
            when(sesiones.buscarPorId(sesion.getId())).thenReturn(Optional.of(sesion));
            servicio.cerrarSesion("jwt");

            assertThat(servicio.validarAcceso("jwt")).isEmpty();
        }

        @Test
        void tokenInvalidoNoDaAccesoNiFalla() {
            when(tokens.leer("alterado")).thenReturn(Optional.empty());

            assertThat(servicio.validarAcceso("alterado")).isEmpty();
            servicio.cerrarSesion("alterado");
            verify(sesiones, never()).guardar(any());
        }
    }
}
