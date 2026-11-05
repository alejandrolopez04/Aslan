package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.shared.dominio.IRepositorio;
import co.edu.udea.aslan.usuarios.dominio.Estudiante;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class RepositorioEstudiantesJpa implements IRepositorio<Estudiante> {

    private final EstudianteJpaRepositorio jpa;

    RepositorioEstudiantesJpa(EstudianteJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Estudiante estudiante) {
        jpa.save(EstudianteEntidad.desde(estudiante));
    }

    @Override
    public Optional<Estudiante> buscarPorId(UUID id) {
        return jpa.findById(id).map(EstudianteEntidad::aDominio);
    }

    @Override
    public List<Estudiante> listar() {
        return jpa.findAll().stream().map(EstudianteEntidad::aDominio).toList();
    }

    @Override
    public void eliminar(UUID id) {
        jpa.deleteById(id);
    }
}
