package co.edu.udea.aslan.sesiones.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class ConfirmacionRequeridaException extends ExcepcionNegocio {

    public ConfirmacionRequeridaException() {
        super("CONFIRMACION_REQUERIDA", TipoError.CONFLICTO, "Aún queda tiempo en el ciclo activo. Confirma que deseas finalizar antes de tiempo.");
    }
}
