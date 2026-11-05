package co.edu.udea.aslan.materias.infraestructura;

import co.edu.udea.aslan.materias.dominio.Materia;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "materias")
class MateriaEntidad {

    @Id
    private UUID id;

    @Column(name = "estudiante_id", nullable = false)
    private UUID estudianteId;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String color;

    @Column(nullable = false, length = 20)
    private String icono;

    @Column(nullable = false)
    private boolean archivada;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "materia_id", nullable = false)
    @OrderColumn(name = "posicion")
    private List<TemaEntidad> temas = new ArrayList<>();

    protected MateriaEntidad() {
    }

    static MateriaEntidad desde(Materia materia) {
        MateriaEntidad entidad = new MateriaEntidad();
        entidad.id = materia.getId();
        entidad.estudianteId = materia.getEstudianteId();
        entidad.nombre = materia.getNombre();
        entidad.color = materia.getColor();
        entidad.icono = materia.getIcono();
        entidad.archivada = materia.isArchivada();
        entidad.temas = new ArrayList<>(materia.getTemas().stream().map(TemaEntidad::desde).toList());
        return entidad;
    }

    Materia aDominio() {
        return new Materia(id, estudianteId, nombre, color, icono, archivada,
                temas.stream().map(TemaEntidad::aDominio).toList());
    }
}
