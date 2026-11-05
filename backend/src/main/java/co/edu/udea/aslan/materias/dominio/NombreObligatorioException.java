package co.edu.udea.aslan.materias.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class NombreObligatorioException extends ExcepcionNegocio {

    public NombreObligatorioException(String mensaje) {
        super("NOMBRE_OBLIGATORIO", TipoError.DATOS_INVALIDOS, mensaje);
    }
}
