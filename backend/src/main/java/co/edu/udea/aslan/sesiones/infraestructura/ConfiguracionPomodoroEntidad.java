package co.edu.udea.aslan.sesiones.infraestructura;

import co.edu.udea.aslan.sesiones.dominio.ConfiguracionPomodoro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "configuraciones_pomodoro")
class ConfiguracionPomodoroEntidad {

    @Id
    @Column(name = "estudiante_id")
    private UUID estudianteId;

    @Column(name = "duracion_trabajo_min", nullable = false)
    private int duracionTrabajoMin;

    @Column(name = "descanso_corto_min", nullable = false)
    private int descansoCortoMin;

    @Column(name = "descanso_largo_min", nullable = false)
    private int descansoLargoMin;

    @Column(name = "ciclos_para_descanso_largo", nullable = false)
    private int ciclosParaDescansoLargo;

    @Column(name = "monitoreo_activo", nullable = false)
    private boolean monitoreoActivo;

    protected ConfiguracionPomodoroEntidad() {
    }

    static ConfiguracionPomodoroEntidad desde(ConfiguracionPomodoro cfg) {
        ConfiguracionPomodoroEntidad entidad = new ConfiguracionPomodoroEntidad();
        entidad.estudianteId = cfg.getEstudianteId();
        entidad.duracionTrabajoMin = cfg.getDuracionTrabajoMin();
        entidad.descansoCortoMin = cfg.getDescansoCortoMin();
        entidad.descansoLargoMin = cfg.getDescansoLargoMin();
        entidad.ciclosParaDescansoLargo = cfg.getCiclosParaDescansoLargo();
        entidad.monitoreoActivo = cfg.isMonitoreoActivo();
        return entidad;
    }

    ConfiguracionPomodoro aDominio() {
        return new ConfiguracionPomodoro(estudianteId, duracionTrabajoMin, descansoCortoMin, descansoLargoMin,
                ciclosParaDescansoLargo, monitoreoActivo);
    }
}
