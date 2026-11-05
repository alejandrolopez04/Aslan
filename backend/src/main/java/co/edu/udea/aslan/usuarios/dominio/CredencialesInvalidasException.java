package co.edu.udea.aslan.usuarios.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class CredencialesInvalidasException extends ExcepcionNegocio {

    public CredencialesInvalidasException() {
        super("CREDENCIALES_INVALIDAS", TipoError.NO_AUTORIZADO, "Correo o contraseña inválida.");
    }
}
