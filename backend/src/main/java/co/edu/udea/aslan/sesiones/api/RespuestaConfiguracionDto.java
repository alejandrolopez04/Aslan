package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.dominio.ConfiguracionPomodoro;

public record RespuestaConfiguracionDto(int duracionTrabajoMin, int descansoCortoMin, int descansoLargoMin,
                                        int ciclosParaDescansoLargo, boolean monitoreoActivo) {

    static RespuestaConfiguracionDto desde(ConfiguracionPomodoro cfg) {
        return new RespuestaConfiguracionDto(cfg.getDuracionTrabajoMin(), cfg.getDescansoCortoMin(),
                cfg.getDescansoLargoMin(), cfg.getCiclosParaDescansoLargo(), cfg.isMonitoreoActivo());
    }
}
