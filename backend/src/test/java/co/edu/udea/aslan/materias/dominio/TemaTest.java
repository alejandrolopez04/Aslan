package co.edu.udea.aslan.materias.dominio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 5);

    @Test
    void temaConDatosCompletos() {
        Tema tema = Tema.nuevo("Integrales", EstadoTema.EN_PROGRESO, Prioridad.ALTA, HOY.plusDays(3), "Repasar");

        assertThat(tema.getEstado()).isEqualTo(EstadoTema.EN_PROGRESO);
        assertThat(tema.getPrioridad()).isEqualTo(Prioridad.ALTA);
        assertThat(tema.getFechaLimite()).contains(HOY.plusDays(3));
        assertThat(tema.getNotas()).isEqualTo("Repasar");
    }

    @Test
    void temaSinFechaLimiteQuedaSinVencimiento() {
        Tema tema = Tema.nuevo("Series", null, null, null, null);

        assertThat(tema.getFechaLimite()).isEmpty();
        assertThat(tema.estaVencido(HOY.plusYears(5))).isFalse();
        assertThat(tema.getEstado()).isEqualTo(EstadoTema.PENDIENTE);
        assertThat(tema.getPrioridad()).isEqualTo(Prioridad.MEDIA);
    }

    @Test
    void nombreDeTemaVacioMuestraMensajeDeValidacion() {
        assertThatThrownBy(() -> Tema.nuevo(" ", EstadoTema.PENDIENTE, Prioridad.BAJA, null, ""))
                .isInstanceOf(NombreObligatorioException.class)
                .hasMessage("Ingresa un nombre para el tema.");
    }

    @Test
    void estaVencidoSiPasoLaFechaYNoEstaCompletado() {
        Tema pendiente = Tema.nuevo("A", EstadoTema.PENDIENTE, Prioridad.BAJA, HOY.minusDays(1), "");
        Tema completado = Tema.nuevo("B", EstadoTema.COMPLETADO, Prioridad.BAJA, HOY.minusDays(1), "");

        assertThat(pendiente.estaVencido(HOY)).isTrue();
        assertThat(pendiente.estaVencido(HOY.minusDays(1))).isFalse();
        assertThat(completado.estaVencido(HOY)).isFalse();
    }
}
