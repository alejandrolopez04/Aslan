package co.edu.udea.aslan.sesiones.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class TemaNoDisponibleException extends ExcepcionNegocio {

    public TemaNoDisponibleException() {
        super("TEMA_NO_ENCONTRADO", TipoError.NO_ENCONTRADO, "No encontramos la materia o el tema seleccionados.");
    }
}
