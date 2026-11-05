package co.edu.udea.aslan.sesiones.infraestructura;

import co.edu.udea.aslan.sesiones.dominio.RachaEstudio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rachas_estudio")
class RachaEstudioEntidad {

    @Id
    @Column(name = "estudiante_id")
    private UUID estudianteId;

    @Column(name = "racha_actual", nullable = false)
    private int rachaActual;

    @Column(name = "racha_maxima", nullable = false)
    private int rachaMaxima;

    @Column(name = "ultimo_dia_estudio")
    private LocalDate ultimoDiaEstudio;

    protected RachaEstudioEntidad() {
    }

    static RachaEstudioEntidad desde(RachaEstudio racha) {
        RachaEstudioEntidad entidad = new RachaEstudioEntidad();
        entidad.estudianteId = racha.getEstudianteId();
        entidad.rachaActual = racha.getRachaActual();
        entidad.rachaMaxima = racha.getRachaMaxima();
        entidad.ultimoDiaEstudio = racha.getUltimoDiaEstudio().orElse(null);
        return entidad;
    }

    RachaEstudio aDominio() {
        return new RachaEstudio(estudianteId, rachaActual, rachaMaxima, ultimoDiaEstudio);
    }
}
