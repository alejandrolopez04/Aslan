package co.edu.udea.aslan.shared.aplicacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioSaludTest {

    @Mock
    private IVerificadorBaseDatos verificadorBaseDatos;

    @InjectMocks
    private ServicioSalud servicioSalud;

    @Test
    void reportaSaludableCuandoLaBaseDeDatosResponde() {
        when(verificadorBaseDatos.estaDisponible()).thenReturn(true);

        EstadoSalud estado = servicioSalud.consultarEstado();

        assertThat(estado).isEqualTo(new EstadoSalud("UP", "UP"));
        assertThat(estado.estaSaludable()).isTrue();
    }

    @Test
    void reportaBaseDeDatosCaidaCuandoNoResponde() {
        when(verificadorBaseDatos.estaDisponible()).thenReturn(false);

        EstadoSalud estado = servicioSalud.consultarEstado();

        assertThat(estado).isEqualTo(new EstadoSalud("UP", "DOWN"));
        assertThat(estado.estaSaludable()).isFalse();
    }
}
