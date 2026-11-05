package co.edu.udea.aslan.usuarios.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

import java.time.Instant;
import java.util.Map;

public class CuentaBloqueadaException extends ExcepcionNegocio {

    private final Instant bloqueadaHasta;

    public CuentaBloqueadaException(Instant bloqueadaHasta) {
        super("CUENTA_BLOQUEADA", TipoError.BLOQUEADO,
                "Alcanzaste el límite de intentos. Espera antes de volver a intentarlo.");
        this.bloqueadaHasta = bloqueadaHasta;
    }

    @Override
    public Map<String, Object> getDetalles() {
        return Map.of("bloqueadaHasta", bloqueadaHasta.toString());
    }
}
