package co.edu.udea.aslan.shared.dominio;

import java.time.Instant;
import java.util.UUID;

public record EventoSesionFinalizada(UUID estudianteId, UUID sesionId, String ambiente, boolean completada,
                                     int duracionEfectivaMin, Instant fecha) {
}
