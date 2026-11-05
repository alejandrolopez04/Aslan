package co.edu.udea.aslan.sesiones.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfiguracionPomodoroTest {

    private final ConfiguracionPomodoro cfg = ConfiguracionPomodoro.estandar(UUID.randomUUID());

    @Test
    void laConfiguracionEstandarEsDe25MinutosConDescansoLargoCada4Ciclos() {
        assertThat(cfg.getDuracionTrabajoMin()).isEqualTo(25);
        assertThat(cfg.getDescansoCortoMin()).isEqualTo(5);
        assertThat(cfg.getDescansoLargoMin()).isEqualTo(15);
        assertThat(cfg.getCiclosParaDescansoLargo()).isEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(ints = {15, 40, 60})
    void duracionValidaSeGuarda(int minutos) {
        cfg.establecerDuracion(minutos);

        assertThat(cfg.getDuracionTrabajoMin()).isEqualTo(minutos);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 14, 61, 120})
    void duracionFueraDeRangoIndicaElRangoPermitido(int minutos) {
        assertThatThrownBy(() -> cfg.establecerDuracion(minutos))
                .isInstanceOf(DuracionFueraDeRangoException.class)
                .hasMessage("La duración debe estar entre 15 y 60 minutos.");
        assertThat(cfg.getDuracionTrabajoMin()).isEqualTo(25);
    }

    @Test
    void desactivarMonitoreo() {
        cfg.activarMonitoreo();
        cfg.desactivarMonitoreo();

        assertThat(cfg.isMonitoreoActivo()).isFalse();
    }
}
