package co.edu.udea.aslan.sesiones.dominio;

import java.util.Objects;
import java.util.UUID;

public class ConfiguracionPomodoro {

    static final int DURACION_MIN = 15;
    static final int DURACION_MAX = 60;
    private static final int SEGUNDOS_POR_MINUTO = 60;

    private final UUID estudianteId;
    private int duracionTrabajoMin;
    private final int descansoCortoMin;
    private final int descansoLargoMin;
    private final int ciclosParaDescansoLargo;
    private boolean monitoreoActivo;

    public ConfiguracionPomodoro(UUID estudianteId, int duracionTrabajoMin, int descansoCortoMin,
                                 int descansoLargoMin, int ciclosParaDescansoLargo, boolean monitoreoActivo) {
        this.estudianteId = Objects.requireNonNull(estudianteId);
        this.duracionTrabajoMin = duracionTrabajoMin;
        this.descansoCortoMin = descansoCortoMin;
        this.descansoLargoMin = descansoLargoMin;
        this.ciclosParaDescansoLargo = ciclosParaDescansoLargo;
        this.monitoreoActivo = monitoreoActivo;
    }

    public static ConfiguracionPomodoro estandar(UUID estudianteId) {
        return new ConfiguracionPomodoro(estudianteId, 25, 5, 15, 4, true);
    }

    public void establecerDuracion(int minutos) {
        if (minutos < DURACION_MIN || minutos > DURACION_MAX) {
            throw new DuracionFueraDeRangoException(DURACION_MIN, DURACION_MAX);
        }
        duracionTrabajoMin = minutos;
    }

    public void activarMonitoreo() {
        monitoreoActivo = true;
    }

    public void desactivarMonitoreo() {
        monitoreoActivo = false;
    }

    public boolean isMonitoreoActivo() {
        return monitoreoActivo;
    }

    int duracionTrabajoSeg() {
        return duracionTrabajoMin * SEGUNDOS_POR_MINUTO;
    }

    int duracionDescansoSeg(boolean largo) {
        return (largo ? descansoLargoMin : descansoCortoMin) * SEGUNDOS_POR_MINUTO;
    }

    boolean tocaDescansoLargo(int numeroCicloTrabajo) {
        return numeroCicloTrabajo % ciclosParaDescansoLargo == 0;
    }

    public UUID getEstudianteId() {
        return estudianteId;
    }

    public int getDuracionTrabajoMin() {
        return duracionTrabajoMin;
    }

    public int getDescansoCortoMin() {
        return descansoCortoMin;
    }

    public int getDescansoLargoMin() {
        return descansoLargoMin;
    }

    public int getCiclosParaDescansoLargo() {
        return ciclosParaDescansoLargo;
    }
}
