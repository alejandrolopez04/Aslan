package co.edu.udea.aslan.sesiones.infraestructura;

import co.edu.udea.aslan.sesiones.dominio.EstadoSesion;
import co.edu.udea.aslan.sesiones.dominio.SesionEstudio;
import co.edu.udea.aslan.sesiones.dominio.TipoSonido;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sesiones_estudio")
class SesionEstudioEntidad {

    @Id
    private UUID id;

    @Column(name = "estudiante_id", nullable = false)
    private UUID estudianteId;

    @Column(name = "materia_id", nullable = false)
    private UUID materiaId;

    @Column(name = "tema_id", nullable = false)
    private UUID temaId;

    @Column(nullable = false)
    private Instant inicio;

    private Instant fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSesion estado;

    @Column(name = "ciclos_planeados", nullable = false)
    private int ciclosPlaneados;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoSonido ambiente;

    @Column(length = 80)
    private String tecnica;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ciclos_pomodoro", joinColumns = @JoinColumn(name = "sesion_id"))
    @OrderColumn(name = "posicion")
    private List<CicloEmbebido> ciclos = new ArrayList<>();

    protected SesionEstudioEntidad() {
    }

    static SesionEstudioEntidad desde(SesionEstudio sesion) {
        SesionEstudioEntidad entidad = new SesionEstudioEntidad();
        entidad.id = sesion.getId();
        entidad.estudianteId = sesion.getEstudianteId();
        entidad.materiaId = sesion.getMateriaId();
        entidad.temaId = sesion.getTemaId();
        entidad.inicio = sesion.getInicio();
        entidad.fin = sesion.getFin().orElse(null);
        entidad.estado = sesion.getEstado();
        entidad.ciclosPlaneados = sesion.getCiclosPlaneados();
        entidad.ambiente = sesion.getAmbiente().orElse(null);
        entidad.tecnica = sesion.getTecnica().orElse(null);
        entidad.ciclos = new ArrayList<>(sesion.getCiclos().stream().map(CicloEmbebido::desde).toList());
        return entidad;
    }

    SesionEstudio aDominio() {
        return new SesionEstudio(id, estudianteId, materiaId, temaId, inicio, fin, estado, ciclosPlaneados,
                ambiente, tecnica, ciclos.stream().map(CicloEmbebido::aDominio).toList());
    }
}
