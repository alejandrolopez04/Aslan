package co.edu.udea.aslan.usuarios.api;

import co.edu.udea.aslan.shared.api.TokenBearer;
import co.edu.udea.aslan.usuarios.aplicacion.ServicioAutenticacion;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final ServicioAutenticacion servicioAutenticacion;

    public AutenticacionController(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaRegistroDto registrar(@Valid @RequestBody SolicitudRegistroDto solicitud) {
        return RespuestaRegistroDto.desde(
                servicioAutenticacion.registrar(solicitud.nombre(), solicitud.correo(), solicitud.contrasena()),
                solicitud.correo());
    }

    @PostMapping("/login")
    public RespuestaSesionDto iniciarSesion(@Valid @RequestBody SolicitudLoginDto solicitud) {
        return RespuestaSesionDto.desde(
                servicioAutenticacion.iniciarSesion(solicitud.correo(), solicitud.contrasena()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrarSesion(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacion) {
        TokenBearer.extraer(autorizacion).ifPresent(servicioAutenticacion::cerrarSesion);
    }
}
