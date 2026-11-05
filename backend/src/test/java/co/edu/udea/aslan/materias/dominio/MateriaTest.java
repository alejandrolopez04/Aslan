package co.edu.udea.aslan.materias.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MateriaTest {

    private final UUID estudiante = UUID.randomUUID();

    @Test
    void crearMateriaConNombreColorEIcono() {
        Materia materia = Materia.crear(estudiante, "  Cálculo ", "red", "calculator");

        assertThat(materia.getNombre()).isEqualTo("Cálculo");
        assertThat(materia.getColor()).isEqualTo("red");
        assertThat(materia.getIcono()).isEqualTo("calculator");
        assertThat(materia.isArchivada()).isFalse();
        assertThat(materia.perteneceA(estudiante)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void nombreDeMateriaVacioMuestraMensajeDeValidacion(String nombre) {
        assertThatThrownBy(() -> Materia.crear(estudiante, nombre, "red", "book"))
                .isInstanceOf(NombreObligatorioException.class)
                .hasMessage("Ingresa un nombre para la materia.");
    }

    @Test
    void calculaElProgresoConLosTemasCompletados() {
        Materia materia = Materia.crear(estudiante, "Cálculo", "red", "book");
        assertThat(materia.calcularProgreso()).isZero();

        materia.agregarTema(Tema.nuevo("Límites", EstadoTema.COMPLETADO, Prioridad.ALTA, null, ""));
        materia.agregarTema(Tema.nuevo("Derivadas", EstadoTema.PENDIENTE, Prioridad.MEDIA, null, ""));

        assertThat(materia.calcularProgreso()).isEqualTo(0.5);
    }
}
