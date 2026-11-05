package co.edu.udea.aslan.sesiones.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class SesionEnCursoException extends ExcepcionNegocio {

    public SesionEnCursoException() {
        super("SESION_EN_CURSO", TipoError.CONFLICTO, "Ya tienes una sesión de estudio en curso. Finalízala antes de iniciar otra.");
    }
}
