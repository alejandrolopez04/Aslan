package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.aplicacion.ServicioSesionEstudio;
import co.edu.udea.aslan.shared.api.EstudianteActual;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/sesiones")
public class SesionesEstudioController {

    private final ServicioSesionEstudio servicio;

    public SesionesEstudioController(ServicioSesionEstudio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/sugerencia")
    public RespuestaSugerenciaDto sugerir(@EstudianteActual UUID estudianteId,
                                          @RequestParam(required = false) UUID materiaId,
                                          @RequestParam(required = false) UUID temaId) {
        return RespuestaSugerenciaDto.desde(servicio.sugerir(estudianteId, materiaId, temaId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaSesionEstudioDto iniciar(@EstudianteActual UUID estudianteId,
                                             @RequestBody SolicitudInicioSesionDto solicitud) {
        return RespuestaSesionEstudioDto.desde(servicio.iniciarSesion(estudianteId, solicitud.materiaId(),
                solicitud.temaId(), solicitud.ambiente()));
    }

    @GetMapping("/activa")
    public ResponseEntity<RespuestaSesionEstudioDto> activa(@EstudianteActual UUID estudianteId) {
        return servicio.sesionAbierta(estudianteId)
                .map(RespuestaSesionEstudioDto::desde)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PatchMapping("/{sesionId}/ciclo")
    public RespuestaSesionEstudioDto cambiarCiclo(@EstudianteActual UUID estudianteId, @PathVariable UUID sesionId,
                                                  @Valid @RequestBody SolicitudCicloDto solicitud) {
        return RespuestaSesionEstudioDto.desde(servicio.ejecutarAccion(estudianteId, sesionId, solicitud.accion(),
                solicitud.transcurrido()));
    }

    @PostMapping("/{sesionId}/finalizar")
    public RespuestaSesionEstudioDto finalizar(@EstudianteActual UUID estudianteId, @PathVariable UUID sesionId,
                                               @Valid @RequestBody SolicitudFinalizarDto solicitud) {
        return RespuestaSesionEstudioDto.desde(servicio.finalizarSesion(estudianteId, sesionId,
                solicitud.transcurrido(), solicitud.confirmado(), solicitud.guardar()));
    }
}
