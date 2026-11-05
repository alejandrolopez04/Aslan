package co.edu.udea.aslan.sesiones.dominio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RachaEstudioTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 5);

    private final RachaEstudio racha = RachaEstudio.nueva(UUID.randomUUID());

    @Test
    void estudiarDiasSeguidosAumentaLaRachaYLaMaxima() {
        racha.actualizarRacha(HOY);
        racha.actualizarRacha(HOY.plusDays(1));
        racha.actualizarRacha(HOY.plusDays(1));

        assertThat(racha.getRachaActual()).isEqualTo(2);
        assertThat(racha.getRachaMaxima()).isEqualTo(2);
    }

    @Test
    void saltarseUnDiaReiniciaLaRachaPeroConservaLaMaxima() {
        racha.actualizarRacha(HOY);
        racha.actualizarRacha(HOY.plusDays(1));
        racha.actualizarRacha(HOY.plusDays(3));

        assertThat(racha.getRachaActual()).isEqualTo(1);
        assertThat(racha.getRachaMaxima()).isEqualTo(2);
        assertThat(racha.getUltimoDiaEstudio()).contains(HOY.plusDays(3));
    }
}
