package co.edu.udea.aslan.usuarios.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class ContrasenaInseguraException extends ExcepcionNegocio {

    public ContrasenaInseguraException() {
        super("CONTRASENA_INSEGURA", TipoError.DATOS_INVALIDOS,
                "La contraseña aún no es válida. Debe tener al menos 8 caracteres, una letra y un número.");
    }
}
