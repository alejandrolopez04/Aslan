package co.edu.udea.aslan.sesiones.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class SesionNoEncontradaException extends ExcepcionNegocio {

    public SesionNoEncontradaException() {
        super("SESION_NO_ENCONTRADA", TipoError.NO_ENCONTRADO, "No encontramos la sesión de estudio.");
    }
}
