package co.edu.udea.aslan.sesiones.api;

import jakarta.validation.constraints.PositiveOrZero;

import java.util.Objects;

public record SolicitudFinalizarDto(
        @PositiveOrZero(message = "El tiempo transcurrido no puede ser negativo.") Integer transcurridoSeg,
        boolean confirmado,
        Boolean guardarProgreso) {

    int transcurrido() {
        return Objects.requireNonNullElse(transcurridoSeg, 0);
    }

    boolean guardar() {
        return Objects.requireNonNullElse(guardarProgreso, true);
    }
}
