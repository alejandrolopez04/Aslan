package co.edu.udea.aslan.usuarios.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaContrasenaSeguraTest {

    private final PoliticaContrasenaSegura politica = new PoliticaContrasenaSegura();

    @ParameterizedTest
    @ValueSource(strings = {"abcdefg1", "ABCDEFG1", "contraseña2026", "1234567a"})
    void aceptaContrasenasConOchoCaracteresLetraYNumero(String contrasena) {
        assertThat(politica.esValida(contrasena)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc1234", "abcdefgh", "12345678", "        "})
    void rechazaContrasenasQueNoCumplenLaPolitica(String contrasena) {
        assertThat(politica.esValida(contrasena)).isFalse();
    }

    @Test
    void verificarLanzaExcepcionConElMensajeDelFront() {
        assertThatThrownBy(() -> politica.verificar("corta1"))
                .isInstanceOf(ContrasenaInseguraException.class)
                .hasMessageStartingWith("La contraseña aún no es válida");
    }

    @Test
    void verificarNoLanzaNadaSiEsValida() {
        assertThatNoException().isThrownBy(() -> politica.verificar("segura123"));
    }
}
