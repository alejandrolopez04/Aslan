package co.edu.udea.aslan.materias.aplicacion;

import java.util.Optional;
import java.util.UUID;

public interface IConsultaTemas {

    Optional<TemaConsultado> buscarTema(UUID estudianteId, UUID materiaId, UUID temaId);
}
