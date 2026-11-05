package co.edu.udea.aslan.shared.api;

import co.edu.udea.aslan.shared.aplicacion.EstadoSalud;
import co.edu.udea.aslan.shared.aplicacion.ServicioSalud;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/salud")
public class SaludController {

    private final ServicioSalud servicioSalud;

    public SaludController(ServicioSalud servicioSalud) {
        this.servicioSalud = servicioSalud;
    }

    @GetMapping
    public ResponseEntity<EstadoSalud> consultar() {
        EstadoSalud estado = servicioSalud.consultarEstado();
        HttpStatus codigo = estado.estaSaludable() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(codigo).body(estado);
    }
}
