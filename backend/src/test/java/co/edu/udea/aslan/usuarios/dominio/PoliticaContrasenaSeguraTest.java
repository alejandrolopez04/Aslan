package co.edu.udea.aslan.usuarios.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaContrasenaSeguraTest {

    private final IPoliticaContrasena politica = new PoliticaContrasenaSegura();

    @ParameterizedTest
    @ValueSource(strings = {"abcdefg1", "ABCDEFG1", "contraseña2026", "1234567a"})
    void aceptaContrasenasConOchoCaracteresLetraYNumero(String contrasena) {
        ResultadoValidacion resultado = politica.validar(contrasena);

        assertThat(resultado.esValido()).isTrue();
        assertThat(resultado.errores()).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc1234", "abcdefgh", "12345678", "        "})
    void rechazaContrasenasQueNoCumplenLaPolitica(String contrasena) {
        assertThat(politica.validar(contrasena).esValido()).isFalse();
    }

    @Test
    void indicaCadaRequisitoIncumplido() {
        assertThat(politica.validar("abc").errores()).containsExactly(
                "Debe tener al menos 8 caracteres.", "Debe incluir al menos un número.");
        assertThat(politica.validar("12345678").errores()).containsExactly("Debe incluir al menos una letra.");
    }

    @Test
    void describeLosRequisitosParaElMensajeDelGherkin() {
        assertThat(politica.describirRequisitos())
                .isEqualTo("La contraseña aún no es válida. Debe tener al menos 8 caracteres, una letra y un número.");
    }
}
