package co.edu.udea.aslan.sesiones.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class AccionNoPermitidaException extends ExcepcionNegocio {

    public AccionNoPermitidaException() {
        super("ACCION_NO_PERMITIDA", TipoError.CONFLICTO, "Esta acción no está disponible en el estado actual del ciclo.");
    }
}
