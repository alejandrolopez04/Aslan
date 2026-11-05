package co.edu.udea.aslan.materias.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class MateriaDuplicadaException extends ExcepcionNegocio {

    public MateriaDuplicadaException() {
        super("MATERIA_DUPLICADA", TipoError.CONFLICTO,
                "Ya existe una materia con este nombre. Confirma si deseas crearla de todos modos.");
    }
}
