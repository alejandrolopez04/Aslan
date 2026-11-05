package co.edu.udea.aslan.usuarios.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentaTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");

    @Test
    void registrarNormalizaCorreoYNombre() {
        Cuenta cuenta = Cuenta.registrar("  María Parra ", "  Maria@UdeA.edu.co ", "hash", AHORA);

        assertThat(cuenta.getId()).isNotNull();
        assertThat(cuenta.getNombre()).isEqualTo("María Parra");
        assertThat(cuenta.getCorreo()).isEqualTo("maria@udea.edu.co");
        assertThat(cuenta.getContrasenaHash()).isEqualTo("hash");
        assertThat(cuenta.getCreadaEn()).isEqualTo(AHORA);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void registrarExigeNombre(String nombre) {
        assertThatThrownBy(() -> Cuenta.registrar(nombre, "a@b.co", "hash", AHORA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
