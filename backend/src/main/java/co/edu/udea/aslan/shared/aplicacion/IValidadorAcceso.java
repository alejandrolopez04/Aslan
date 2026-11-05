package co.edu.udea.aslan.shared.aplicacion;

import java.util.Optional;
import java.util.UUID;

public interface IValidadorAcceso {

    Optional<UUID> validarAcceso(String token);
}
