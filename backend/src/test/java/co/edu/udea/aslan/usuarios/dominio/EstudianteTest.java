package co.edu.udea.aslan.usuarios.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstudianteTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");

    @Test
    void registrarAsignaIdNombreYFechaDeRegistro() {
        Estudiante estudiante = Estudiante.registrar("  María Parra ", AHORA);

        assertThat(estudiante.getId()).isNotNull();
        assertThat(estudiante.getNombre()).isEqualTo("María Parra");
        assertThat(estudiante.getFechaRegistro()).isEqualTo(AHORA);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void registrarExigeNombre(String nombre) {
        assertThatThrownBy(() -> Estudiante.registrar(nombre, AHORA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
