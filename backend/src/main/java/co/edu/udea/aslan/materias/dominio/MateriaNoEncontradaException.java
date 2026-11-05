package co.edu.udea.aslan.materias.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

public class MateriaNoEncontradaException extends ExcepcionNegocio {

    public MateriaNoEncontradaException() {
        super("MATERIA_NO_ENCONTRADA", TipoError.NO_ENCONTRADO, "No encontramos la materia.");
    }
}
