package co.edu.udea.aslan.materias.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface MateriaJpaRepositorio extends JpaRepository<MateriaEntidad, UUID> {

    List<MateriaEntidad> findByEstudianteIdOrderByNombreAsc(UUID estudianteId);

    boolean existsByEstudianteIdAndNombreIgnoreCase(UUID estudianteId, String nombre);
}
