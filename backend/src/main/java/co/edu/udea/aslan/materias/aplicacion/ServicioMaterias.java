package co.edu.udea.aslan.materias.aplicacion;

import co.edu.udea.aslan.materias.dominio.IMateriaRepositorio;
import co.edu.udea.aslan.materias.dominio.Materia;
import co.edu.udea.aslan.materias.dominio.MateriaDuplicadaException;
import co.edu.udea.aslan.materias.dominio.MateriaNoEncontradaException;
import co.edu.udea.aslan.materias.dominio.Tema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServicioMaterias implements IConsultaTemas {

    private final IMateriaRepositorio materias;

    public ServicioMaterias(IMateriaRepositorio materias) {
        this.materias = materias;
    }

    @Transactional
    public Materia crearMateria(UUID estudianteId, String nombre, String color, String icono,
                                boolean confirmarDuplicado) {
        Materia materia = Materia.crear(estudianteId, nombre, color, icono);
        if (!confirmarDuplicado && materias.existeNombre(estudianteId, materia.getNombre())) {
            throw new MateriaDuplicadaException();
        }
        materias.guardar(materia);
        return materia;
    }

    @Transactional
    public Tema crearTema(UUID estudianteId, UUID materiaId, DatosTema datos) {
        Materia materia = buscarMateria(estudianteId, materiaId);
        Tema tema = Tema.nuevo(datos.nombre(), datos.estado(), datos.prioridad(), datos.fechaLimite(), datos.notas());
        materia.agregarTema(tema);
        materias.guardar(materia);
        return tema;
    }

    @Transactional(readOnly = true)
    public List<Materia> listarMaterias(UUID estudianteId) {
        return materias.listarPorEstudiante(estudianteId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TemaConsultado> buscarTema(UUID estudianteId, UUID materiaId, UUID temaId) {
        return materias.buscarPorId(materiaId)
                .filter(materia -> materia.perteneceA(estudianteId))
                .flatMap(materia -> materia.buscarTema(temaId).map(tema -> new TemaConsultado(
                        materia.getId(), materia.getNombre(), tema.getId(), tema.getNombre(),
                        tema.getPrioridad().name())));
    }

    private Materia buscarMateria(UUID estudianteId, UUID materiaId) {
        return materias.buscarPorId(materiaId)
                .filter(materia -> materia.perteneceA(estudianteId))
                .orElseThrow(MateriaNoEncontradaException::new);
    }
}
