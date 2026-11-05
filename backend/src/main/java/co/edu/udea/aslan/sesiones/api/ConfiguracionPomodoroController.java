package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.aplicacion.ServicioConfiguracionPomodoro;
import co.edu.udea.aslan.shared.api.EstudianteActual;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/configuracion-pomodoro")
public class ConfiguracionPomodoroController {

    private final ServicioConfiguracionPomodoro servicio;

    public ConfiguracionPomodoroController(ServicioConfiguracionPomodoro servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public RespuestaConfiguracionDto obtener(@EstudianteActual UUID estudianteId) {
        return RespuestaConfiguracionDto.desde(servicio.obtener(estudianteId));
    }

    @PutMapping
    public RespuestaConfiguracionDto configurar(@EstudianteActual UUID estudianteId,
                                                @Valid @RequestBody SolicitudConfiguracionDto solicitud) {
        return RespuestaConfiguracionDto.desde(servicio.configurar(estudianteId, solicitud.duracionTrabajoMin(),
                solicitud.monitoreoActivo()));
    }
}
