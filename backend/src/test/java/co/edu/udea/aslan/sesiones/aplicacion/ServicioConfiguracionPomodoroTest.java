package co.edu.udea.aslan.sesiones.aplicacion;

import co.edu.udea.aslan.sesiones.dominio.ConfiguracionPomodoro;
import co.edu.udea.aslan.sesiones.dominio.DuracionFueraDeRangoException;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioConfiguracionPomodoroTest {

    private final UUID estudiante = UUID.randomUUID();

    @Mock
    private IRepositorio<ConfiguracionPomodoro> configuraciones;

    @InjectMocks
    private ServicioConfiguracionPomodoro servicio;

    @Test
    void sinConfiguracionGuardadaUsaLaEstandar() {
        when(configuraciones.buscarPorId(estudiante)).thenReturn(Optional.empty());

        assertThat(servicio.obtener(estudiante).getDuracionTrabajoMin()).isEqualTo(25);
    }

    @Test
    void configurarDuracionValidaYDesactivarMonitoreoLaGuarda() {
        when(configuraciones.buscarPorId(estudiante)).thenReturn(Optional.empty());

        ConfiguracionPomodoro cfg = servicio.configurar(estudiante, 45, false);

        assertThat(cfg.getDuracionTrabajoMin()).isEqualTo(45);
        assertThat(cfg.isMonitoreoActivo()).isFalse();
        verify(configuraciones).guardar(cfg);
    }

    @Test
    void duracionFueraDeRangoNoGuarda() {
        when(configuraciones.buscarPorId(estudiante)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.configurar(estudiante, 90, true))
                .isInstanceOf(DuracionFueraDeRangoException.class);
        verify(configuraciones, never()).guardar(any());
    }
}
