package co.edu.udea.aslan.usuarios.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class CorreoEnUsoException extends ExcepcionNegocio {

    public CorreoEnUsoException() {
        super("CORREO_EN_USO", TipoError.CONFLICTO,
                "Este correo ya está en uso. Intenta iniciar sesión o utiliza otro correo para crear tu cuenta.");
    }
}
