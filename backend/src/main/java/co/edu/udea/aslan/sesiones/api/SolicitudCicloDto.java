package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.aplicacion.AccionCiclo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Objects;

public record SolicitudCicloDto(
        @NotNull(message = "Indica la acción del ciclo: iniciar, pausar, reanudar o siguiente.")
        AccionCiclo accion,
        @PositiveOrZero(message = "El tiempo transcurrido no puede ser negativo.") Integer transcurridoSeg) {

    int transcurrido() {
        return Objects.requireNonNullElse(transcurridoSeg, 0);
    }
}
