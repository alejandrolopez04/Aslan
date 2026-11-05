package co.edu.udea.aslan.usuarios.dominio;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CuentaTest {

    @Test
    void abrirNormalizaElCorreoYEmpiezaSinIntentosNiBloqueo() {
        UUID estudianteId = UUID.randomUUID();

        Cuenta cuenta = Cuenta.abrir(estudianteId, "  Maria@UdeA.edu.co ", "hash");

        assertThat(cuenta.getEstudianteId()).isEqualTo(estudianteId);
        assertThat(cuenta.getCorreo()).isEqualTo("maria@udea.edu.co");
        assertThat(cuenta.getIntentosFallidos()).isZero();
        assertThat(cuenta.getBloqueadaHasta()).isEmpty();
    }

    @Test
    void verificarDelegaLaComparacionEnElHasher() {
        Cuenta cuenta = Cuenta.abrir(UUID.randomUUID(), "maria@udea.edu.co", "hash-guardado");
        IServicioHash hasher = mock(IServicioHash.class);
        when(hasher.coincide("segura123", "hash-guardado")).thenReturn(true);

        assertThat(cuenta.verificar("segura123", hasher)).isTrue();
        assertThat(cuenta.verificar("otra", hasher)).isFalse();
    }
}
