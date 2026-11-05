package co.edu.udea.aslan.usuarios.aplicacion;

import co.edu.udea.aslan.shared.aplicacion.IValidadorAcceso;
import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import co.edu.udea.aslan.usuarios.dominio.ContrasenaInseguraException;
import co.edu.udea.aslan.usuarios.dominio.CorreoEnUsoException;
import co.edu.udea.aslan.usuarios.dominio.CredencialesInvalidasException;
import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.CuentaBloqueadaException;
import co.edu.udea.aslan.usuarios.dominio.Estudiante;
import co.edu.udea.aslan.usuarios.dominio.ICuentaRepositorio;
import co.edu.udea.aslan.usuarios.dominio.IPoliticaContrasena;
import co.edu.udea.aslan.usuarios.dominio.IServicioHash;
import co.edu.udea.aslan.usuarios.dominio.IServicioTokens;
import co.edu.udea.aslan.usuarios.dominio.ResultadoValidacion;
import co.edu.udea.aslan.usuarios.dominio.SesionAutenticacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServicioAutenticacion implements IValidadorAcceso {

    private final ICuentaRepositorio cuentas;
    private final IRepositorio<Estudiante> estudiantes;
    private final IRepositorio<SesionAutenticacion> sesiones;
    private final IServicioHash hasher;
    private final IPoliticaContrasena politica;
    private final IServicioTokens tokens;
    private final Clock reloj;

    public ServicioAutenticacion(ICuentaRepositorio cuentas, IRepositorio<Estudiante> estudiantes,
                                 IRepositorio<SesionAutenticacion> sesiones, IServicioHash hasher,
                                 IPoliticaContrasena politica, IServicioTokens tokens, Clock reloj) {
        this.cuentas = cuentas;
        this.estudiantes = estudiantes;
        this.sesiones = sesiones;
        this.hasher = hasher;
        this.politica = politica;
        this.tokens = tokens;
        this.reloj = reloj;
    }

    @Transactional
    public Estudiante registrar(String nombre, String correo, String contrasena) {
        ResultadoValidacion resultado = politica.validar(contrasena);
        if (!resultado.esValido()) {
            throw new ContrasenaInseguraException(politica.describirRequisitos(), resultado.errores());
        }
        if (cuentas.existeCorreo(Cuenta.normalizarCorreo(correo))) {
            throw new CorreoEnUsoException();
        }
        Estudiante estudiante = Estudiante.registrar(nombre, Instant.now(reloj));
        estudiantes.guardar(estudiante);
        cuentas.guardar(Cuenta.abrir(estudiante.getId(), correo, hasher.generarHash(contrasena)));
        return estudiante;
    }

    @Transactional(noRollbackFor = ExcepcionNegocio.class)
    public SesionAutenticacion iniciarSesion(String correo, String contrasena) {
        Instant ahora = Instant.now(reloj);
        Cuenta cuenta = cuentas.buscarPorCorreo(Cuenta.normalizarCorreo(correo))
                .orElseThrow(CredencialesInvalidasException::new);
        verificarQueNoEsteBloqueada(cuenta, ahora);

        if (!cuenta.verificar(contrasena, hasher)) {
            cuenta.registrarIntentoFallido(ahora);
            cuentas.guardar(cuenta);
            verificarQueNoEsteBloqueada(cuenta, ahora);
            throw new CredencialesInvalidasException();
        }

        cuenta.reiniciarIntentos();
        cuentas.guardar(cuenta);
        Estudiante estudiante = estudiantes.buscarPorId(cuenta.getEstudianteId()).orElseThrow();
        SesionAutenticacion sesion = SesionAutenticacion.abrir(estudiante, cuenta, ahora, tokens);
        sesiones.guardar(sesion);
        return sesion;
    }

    @Transactional
    public void cerrarSesion(String token) {
        buscarSesion(token).ifPresent(sesion -> {
            sesion.invalidar();
            sesiones.guardar(sesion);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> validarAcceso(String token) {
        Instant ahora = Instant.now(reloj);
        return buscarSesion(token)
                .filter(sesion -> sesion.estaVigente(ahora))
                .map(SesionAutenticacion::getEstudianteId);
    }

    private Optional<SesionAutenticacion> buscarSesion(String token) {
        return tokens.leer(token).flatMap(reclamos -> sesiones.buscarPorId(reclamos.sesionId()));
    }

    private void verificarQueNoEsteBloqueada(Cuenta cuenta, Instant ahora) {
        if (cuenta.estaBloqueada(ahora)) {
            throw new CuentaBloqueadaException(cuenta.getBloqueadaHasta().orElseThrow());
        }
    }
}
