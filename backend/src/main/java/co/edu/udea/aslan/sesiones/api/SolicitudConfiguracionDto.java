package co.edu.udea.aslan.sesiones.api;

import jakarta.validation.constraints.NotNull;

public record SolicitudConfiguracionDto(
        @NotNull(message = "La duración debe estar entre 15 y 60 minutos.") Integer duracionTrabajoMin,
        @NotNull(message = "Indica si el monitoreo está activo.") Boolean monitoreoActivo) {
}
