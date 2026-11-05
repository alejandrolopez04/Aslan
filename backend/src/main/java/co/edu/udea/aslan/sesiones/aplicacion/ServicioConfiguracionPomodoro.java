package co.edu.udea.aslan.sesiones.aplicacion;

import co.edu.udea.aslan.sesiones.dominio.ConfiguracionPomodoro;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ServicioConfiguracionPomodoro {

    private final IRepositorio<ConfiguracionPomodoro> configuraciones;

    public ServicioConfiguracionPomodoro(IRepositorio<ConfiguracionPomodoro> configuraciones) {
        this.configuraciones = configuraciones;
    }

    @Transactional(readOnly = true)
    public ConfiguracionPomodoro obtener(UUID estudianteId) {
        return configuraciones.buscarPorId(estudianteId)
                .orElseGet(() -> ConfiguracionPomodoro.estandar(estudianteId));
    }

    @Transactional
    public ConfiguracionPomodoro configurar(UUID estudianteId, int duracionTrabajoMin, boolean monitoreoActivo) {
        ConfiguracionPomodoro cfg = obtener(estudianteId);
        cfg.establecerDuracion(duracionTrabajoMin);
        if (monitoreoActivo) {
            cfg.activarMonitoreo();
        } else {
            cfg.desactivarMonitoreo();
        }
        configuraciones.guardar(cfg);
        return cfg;
    }
}
