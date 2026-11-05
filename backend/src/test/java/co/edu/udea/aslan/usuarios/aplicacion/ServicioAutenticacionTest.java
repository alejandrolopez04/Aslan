package co.edu.udea.aslan.usuarios.aplicacion;

import co.edu.udea.aslan.usuarios.dominio.CredencialesInvalidasException;
import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.CuentaBloqueadaException;
import co.edu.udea.aslan.usuarios.dominio.ICifradorContrasenas;
import co.edu.udea.aslan.usuarios.dominio.IGeneradorTokens;
import co.edu.udea.aslan.usuarios.dominio.IRepositorioCuentas;
import co.edu.udea.aslan.usuarios.dominio.IRepositorioTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioAutenticacionTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");

    @Mock
    private IRepositorioCuentas repositorioCuentas;

    @Mock
    private IRepositorioTokens repositorioTokens;

    @Mock
    private ICifradorContrasenas cifrador;

    @Mock
    private IGeneradorTokens generadorTokens;

    private ServicioAutenticacion servicio;
    private Cuenta cuenta;

    @BeforeEach
    void preparar() {
        servicio = new ServicioAutenticacion(repositorioCuentas, repositorioTokens, cifrador, generadorTokens,
                Clock.fixed(AHORA, ZoneOffset.UTC));
        cuenta = Cuenta.registrar("María", "maria@udea.edu.co", "hash", AHORA.minus(Duration.ofDays(1)));
    }

    @Test
    void credencialesCorrectasEmitenUnTokenDeDosHoras() {
        when(repositorioCuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));
        when(cifrador.coincide("segura123", "hash")).thenReturn(true);
        when(generadorTokens.generar()).thenReturn("token-aleatorio");
        when(repositorioTokens.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        SesionIniciada sesion = servicio.iniciarSesion(" MARIA@udea.edu.co ", "segura123");

        assertThat(sesion.token()).isEqualTo("token-aleatorio");
        assertThat(sesion.expiraEn()).isEqualTo(AHORA.plus(Duration.ofHours(2)));
        assertThat(sesion.cuenta().correo()).isEqualTo("maria@udea.edu.co");
    }

    @Test
    void contrasenaIncorrectaSumaUnIntentoYResponde401() {
        when(repositorioCuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));
        when(cifrador.coincide("mala", "hash")).thenReturn(false);

        assertThatThrownBy(() -> servicio.iniciarSesion("maria@udea.edu.co", "mala"))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña inválida.");
        assertThat(cuenta.getIntentosFallidos()).isEqualTo(1);
        verify(repositorioCuentas).guardar(cuenta);
        verify(repositorioTokens, never()).guardar(any());
    }

    @Test
    void correoInexistenteRespondeIgualQueContrasenaIncorrecta() {
        when(repositorioCuentas.buscarPorCorreo("nadie@udea.edu.co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.iniciarSesion("nadie@udea.edu.co", "segura123"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void elQuintoIntentoFallidoBloqueaLaCuenta() {
        for (int i = 0; i < 4; i++) {
            cuenta.registrarIntentoFallido(AHORA);
        }
        when(repositorioCuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));
        when(cifrador.coincide("mala", "hash")).thenReturn(false);

        assertThatThrownBy(() -> servicio.iniciarSesion("maria@udea.edu.co", "mala"))
                .isInstanceOf(CuentaBloqueadaException.class)
                .satisfies(e -> assertThat(((CuentaBloqueadaException) e).getDetalles())
                        .containsEntry("bloqueadaHasta", AHORA.plus(Duration.ofMinutes(15)).toString()));
    }

    @Test
    void cuentaBloqueadaRechazaInclusoLaContrasenaCorrectaSinVerificarla() {
        for (int i = 0; i < 5; i++) {
            cuenta.registrarIntentoFallido(AHORA.minus(Duration.ofMinutes(5)));
        }
        when(repositorioCuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> servicio.iniciarSesion("maria@udea.edu.co", "segura123"))
                .isInstanceOf(CuentaBloqueadaException.class);
        verify(cifrador, never()).coincide(anyString(), anyString());
    }

    @Test
    void trasQuinceMinutosLaCuentaPuedeVolverAIngresar() {
        for (int i = 0; i < 5; i++) {
            cuenta.registrarIntentoFallido(AHORA.minus(Duration.ofMinutes(15)));
        }
        when(repositorioCuentas.buscarPorCorreo("maria@udea.edu.co")).thenReturn(Optional.of(cuenta));
        when(cifrador.coincide("segura123", "hash")).thenReturn(true);
        when(generadorTokens.generar()).thenReturn("token");
        when(repositorioTokens.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertThat(servicio.iniciarSesion("maria@udea.edu.co", "segura123").token()).isEqualTo("token");
        assertThat(cuenta.getIntentosFallidos()).isZero();
        assertThat(cuenta.getBloqueadaHasta()).isEmpty();
    }
}
