package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.Estudiante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "estudiantes")
class EstudianteEntidad {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    protected EstudianteEntidad() {
    }

    static EstudianteEntidad desde(Estudiante estudiante) {
        EstudianteEntidad entidad = new EstudianteEntidad();
        entidad.id = estudiante.getId();
        entidad.nombre = estudiante.getNombre();
        entidad.fechaRegistro = estudiante.getFechaRegistro();
        return entidad;
    }

    Estudiante aDominio() {
        return new Estudiante(id, nombre, fechaRegistro);
    }
}
