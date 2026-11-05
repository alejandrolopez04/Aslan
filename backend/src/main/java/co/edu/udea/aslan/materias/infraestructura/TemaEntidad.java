package co.edu.udea.aslan.materias.infraestructura;

import co.edu.udea.aslan.materias.dominio.EstadoTema;
import co.edu.udea.aslan.materias.dominio.Prioridad;
import co.edu.udea.aslan.materias.dominio.Tema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "temas")
class TemaEntidad {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTema estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridad prioridad;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(nullable = false)
    private String notas;

    protected TemaEntidad() {
    }

    static TemaEntidad desde(Tema tema) {
        TemaEntidad entidad = new TemaEntidad();
        entidad.id = tema.getId();
        entidad.nombre = tema.getNombre();
        entidad.estado = tema.getEstado();
        entidad.prioridad = tema.getPrioridad();
        entidad.fechaLimite = tema.getFechaLimite().orElse(null);
        entidad.notas = tema.getNotas();
        return entidad;
    }

    Tema aDominio() {
        return new Tema(id, nombre, estado, prioridad, fechaLimite, notas);
    }
}
