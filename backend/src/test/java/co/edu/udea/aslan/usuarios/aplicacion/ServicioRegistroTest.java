package co.edu.udea.aslan.usuarios.aplicacion;

import co.edu.udea.aslan.usuarios.dominio.ContrasenaInseguraException;
import co.edu.udea.aslan.usuarios.dominio.CorreoEnUsoException;
import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.ICifradorContrasenas;
import co.edu.udea.aslan.usuarios.dominio.IRepositorioCuentas;
import co.edu.udea.aslan.usuarios.dominio.PoliticaContrasenaSegura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioRegistroTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");

    @Mock
    private IRepositorioCuentas repositorio;

    @Mock
    private ICifradorContrasenas cifrador;

    private ServicioRegistro servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioRegistro(repositorio, cifrador, new PoliticaContrasenaSegura(),
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void registraUnaCuentaNuevaConLaContrasenaCifrada() {
        when(repositorio.existePorCorreo("maria@udea.edu.co")).thenReturn(false);
        when(cifrador.cifrar("segura123")).thenReturn("hash-bcrypt");
        when(repositorio.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        CuentaRegistrada registrada = servicio.registrar(
                new SolicitudRegistro("María Parra", "Maria@UdeA.edu.co", "segura123"));

        ArgumentCaptor<Cuenta> guardada = ArgumentCaptor.forClass(Cuenta.class);
        verify(repositorio).guardar(guardada.capture());
        assertThat(guardada.getValue().getContrasenaHash()).isEqualTo("hash-bcrypt");
        assertThat(guardada.getValue().getCreadaEn()).isEqualTo(AHORA);
        assertThat(registrada.correo()).isEqualTo("maria@udea.edu.co");
        assertThat(registrada.nombre()).isEqualTo("María Parra");
        assertThat(registrada.id()).isEqualTo(guardada.getValue().getId());
    }

    @Test
    void rechazaUnCorreoQueYaEstaEnUso() {
        when(repositorio.existePorCorreo("alejo@aslan.app")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(
                new SolicitudRegistro("Alejandro", "ALEJO@aslan.app", "segura123")))
                .isInstanceOf(CorreoEnUsoException.class)
                .hasMessageStartingWith("Este correo ya está en uso");
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void rechazaUnaContrasenaInseguraSinTocarElRepositorio() {
        assertThatThrownBy(() -> servicio.registrar(
                new SolicitudRegistro("María", "maria@udea.edu.co", "sinnumeros")))
                .isInstanceOf(ContrasenaInseguraException.class);
        verifyNoInteractions(repositorio, cifrador);
    }
}
