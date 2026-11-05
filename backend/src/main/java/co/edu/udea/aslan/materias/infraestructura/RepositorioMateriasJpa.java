package co.edu.udea.aslan.materias.infraestructura;

import co.edu.udea.aslan.materias.dominio.IMateriaRepositorio;
import co.edu.udea.aslan.materias.dominio.Materia;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class RepositorioMateriasJpa implements IMateriaRepositorio {

    private final MateriaJpaRepositorio jpa;

    RepositorioMateriasJpa(MateriaJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Materia materia) {
        jpa.save(MateriaEntidad.desde(materia));
    }

    @Override
    public Optional<Materia> buscarPorId(UUID id) {
        return jpa.findById(id).map(MateriaEntidad::aDominio);
    }

    @Override
    public List<Materia> listar() {
        return jpa.findAll().stream().map(MateriaEntidad::aDominio).toList();
    }

    @Override
    public void eliminar(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public List<Materia> listarPorEstudiante(UUID estudianteId) {
        return jpa.findByEstudianteIdOrderByNombreAsc(estudianteId).stream().map(MateriaEntidad::aDominio).toList();
    }

    @Override
    public boolean existeNombre(UUID estudianteId, String nombre) {
        return jpa.existsByEstudianteIdAndNombreIgnoreCase(estudianteId, nombre);
    }
}
