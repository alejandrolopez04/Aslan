package co.edu.udea.aslan.usuarios.api;

import co.edu.udea.aslan.usuarios.aplicacion.ResumenCuenta;
import co.edu.udea.aslan.usuarios.aplicacion.ServicioAutenticacion;
import co.edu.udea.aslan.usuarios.aplicacion.ServicioRegistro;
import co.edu.udea.aslan.usuarios.aplicacion.SesionIniciada;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final ServicioRegistro servicioRegistro;
    private final ServicioAutenticacion servicioAutenticacion;

    public AutenticacionController(ServicioRegistro servicioRegistro, ServicioAutenticacion servicioAutenticacion) {
        this.servicioRegistro = servicioRegistro;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumenCuenta registrar(@Valid @RequestBody SolicitudRegistroDto solicitud) {
        return servicioRegistro.registrar(solicitud.aSolicitud());
    }

    @PostMapping("/login")
    public SesionIniciada iniciarSesion(@Valid @RequestBody SolicitudLoginDto solicitud) {
        return servicioAutenticacion.iniciarSesion(solicitud.correo(), solicitud.contrasena());
    }
}
