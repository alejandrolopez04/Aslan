package co.edu.udea.aslan.sesiones.infraestructura;

import co.edu.udea.aslan.sesiones.dominio.RegistroSesion;
import co.edu.udea.aslan.sesiones.dominio.TipoSonido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registros_sesion")
class RegistroSesionEntidad {

    @Id
    @Column(name = "sesion_id")
    private UUID sesionId;

    @Column(name = "estudiante_id", nullable = false)
    private UUID estudianteId;

    @Column(name = "materia_id", nullable = false)
    private UUID materiaId;

    @Column(name = "tema_id", nullable = false)
    private UUID temaId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoSonido ambiente;

    @Column(nullable = false)
    private Instant fecha;

    @Column(name = "duracion_efectiva_min", nullable = false)
    private int duracionEfectivaMin;

    @Column(name = "ciclos_completados", nullable = false)
    private int ciclosCompletados;

    @Column(nullable = false)
    private boolean completada;

    protected RegistroSesionEntidad() {
    }

    static RegistroSesionEntidad desde(RegistroSesion registro) {
        RegistroSesionEntidad entidad = new RegistroSesionEntidad();
        entidad.sesionId = registro.sesionId();
        entidad.estudianteId = registro.estudianteId();
        entidad.materiaId = registro.materiaId();
        entidad.temaId = registro.temaId();
        entidad.ambiente = registro.ambiente();
        entidad.fecha = registro.fecha();
        entidad.duracionEfectivaMin = registro.duracionEfectivaMin();
        entidad.ciclosCompletados = registro.ciclosCompletados();
        entidad.completada = registro.completada();
        return entidad;
    }

    RegistroSesion aDominio() {
        return new RegistroSesion(sesionId, estudianteId, materiaId, temaId, ambiente, fecha, duracionEfectivaMin,
                ciclosCompletados, completada);
    }
}
