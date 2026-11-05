package co.edu.udea.aslan.usuarios.dominio;

import java.time.Instant;
import java.util.UUID;

public record ReclamosToken(UUID sesionId, UUID estudianteId, String nombre, String correo, Instant inicio,
                            Instant expiracion) {
}
