package co.edu.udea.aslan.materias.api;

import co.edu.udea.aslan.materias.aplicacion.ServicioMaterias;
import co.edu.udea.aslan.shared.api.EstudianteActual;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/materias")
public class MateriasController {

    private final ServicioMaterias servicio;
    private final Clock reloj;

    public MateriasController(ServicioMaterias servicio, Clock reloj) {
        this.servicio = servicio;
        this.reloj = reloj;
    }

    @GetMapping
    public List<RespuestaMateriaDto> listar(@EstudianteActual UUID estudianteId) {
        LocalDate hoy = LocalDate.now(reloj);
        return servicio.listarMaterias(estudianteId).stream()
                .map(materia -> RespuestaMateriaDto.desde(materia, hoy))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaMateriaDto crear(@EstudianteActual UUID estudianteId,
                                     @Valid @RequestBody SolicitudMateriaDto solicitud) {
        return RespuestaMateriaDto.desde(servicio.crearMateria(estudianteId, solicitud.nombre(), solicitud.color(),
                solicitud.icono(), solicitud.confirmarDuplicado()), LocalDate.now(reloj));
    }

    @PostMapping("/{materiaId}/temas")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaTemaDto crearTema(@EstudianteActual UUID estudianteId, @PathVariable UUID materiaId,
                                      @RequestBody SolicitudTemaDto solicitud) {
        return RespuestaTemaDto.desde(servicio.crearTema(estudianteId, materiaId, solicitud.aDatos()),
                LocalDate.now(reloj));
    }
}
