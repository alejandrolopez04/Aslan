package co.edu.udea.aslan.materias.dominio;

import co.edu.udea.aslan.shared.dominio.IRepositorio;

import java.util.List;
import java.util.UUID;

public interface IMateriaRepositorio extends IRepositorio<Materia> {

    List<Materia> listarPorEstudiante(UUID estudianteId);

    boolean existeNombre(UUID estudianteId, String nombre);
}
