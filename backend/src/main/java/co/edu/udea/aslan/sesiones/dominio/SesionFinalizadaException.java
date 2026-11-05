package co.edu.udea.aslan.sesiones.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class SesionFinalizadaException extends ExcepcionNegocio {

    public SesionFinalizadaException() {
        super("SESION_FINALIZADA", TipoError.CONFLICTO, "La sesión de estudio ya finalizó.");
    }
}
