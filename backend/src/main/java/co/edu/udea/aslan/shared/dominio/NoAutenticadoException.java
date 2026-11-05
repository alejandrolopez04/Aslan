package co.edu.udea.aslan.shared.dominio;

public class NoAutenticadoException extends ExcepcionNegocio {

    public NoAutenticadoException() {
        super("NO_AUTENTICADO", TipoError.NO_AUTORIZADO, "Tu sesión no es válida o expiró. Inicia sesión de nuevo.");
    }
}
