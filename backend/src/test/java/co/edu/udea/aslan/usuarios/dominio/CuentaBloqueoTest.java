package co.edu.udea.aslan.usuarios.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CuentaBloqueoTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");

    private Cuenta cuenta;

    @BeforeEach
    void preparar() {
        cuenta = Cuenta.registrar("María", "maria@udea.edu.co", "hash", AHORA);
    }

    @Test
    void cuatroIntentosFallidosNoBloquean() {
        fallar(4, AHORA);

        assertThat(cuenta.estaBloqueada(AHORA)).isFalse();
        assertThat(cuenta.getIntentosFallidos()).isEqualTo(4);
    }

    @Test
    void elQuintoIntentoFallidoSeguidoBloqueaQuinceMinutos() {
        fallar(5, AHORA);

        assertThat(cuenta.estaBloqueada(AHORA)).isTrue();
        assertThat(cuenta.getBloqueadaHasta()).contains(AHORA.plus(Duration.ofMinutes(15)));
        assertThat(cuenta.estaBloqueada(AHORA.plus(Duration.ofMinutes(14)))).isTrue();
        assertThat(cuenta.estaBloqueada(AHORA.plus(Duration.ofMinutes(15)))).isFalse();
    }

    @Test
    void unIngresoExitosoReiniciaElContador() {
        fallar(4, AHORA);
        cuenta.registrarIngresoExitoso();
        fallar(4, AHORA);

        assertThat(cuenta.estaBloqueada(AHORA)).isFalse();
        assertThat(cuenta.getIntentosFallidos()).isEqualTo(4);
    }

    @Test
    void trasVencerElBloqueoLosIntentosVuelvenAContarDesdeCero() {
        fallar(5, AHORA);
        Instant despues = AHORA.plus(Duration.ofMinutes(16));

        cuenta.registrarIntentoFallido(despues);

        assertThat(cuenta.estaBloqueada(despues)).isFalse();
        assertThat(cuenta.getIntentosFallidos()).isEqualTo(1);
        assertThat(cuenta.getBloqueadaHasta()).isEmpty();
    }

    private void fallar(int veces, Instant momento) {
        for (int i = 0; i < veces; i++) {
            cuenta.registrarIntentoFallido(momento);
        }
    }
}
