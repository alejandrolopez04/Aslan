package co.edu.udea.aslan.usuarios.aplicacion;

import co.edu.udea.aslan.usuarios.dominio.CorreoEnUsoException;
import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.ICifradorContrasenas;
import co.edu.udea.aslan.usuarios.dominio.IRepositorioCuentas;
import co.edu.udea.aslan.usuarios.dominio.PoliticaContrasenaSegura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ServicioRegistro {

    private final IRepositorioCuentas repositorioCuentas;
    private final ICifradorContrasenas cifrador;
    private final PoliticaContrasenaSegura politica;
    private final Clock reloj;

    public ServicioRegistro(IRepositorioCuentas repositorioCuentas, ICifradorContrasenas cifrador,
                            PoliticaContrasenaSegura politica, Clock reloj) {
        this.repositorioCuentas = repositorioCuentas;
        this.cifrador = cifrador;
        this.politica = politica;
        this.reloj = reloj;
    }

    @Transactional
    public CuentaRegistrada registrar(SolicitudRegistro solicitud) {
        politica.verificar(solicitud.contrasena());
        String correo = Cuenta.normalizarCorreo(solicitud.correo());
        if (repositorioCuentas.existePorCorreo(correo)) {
            throw new CorreoEnUsoException();
        }
        Cuenta cuenta = Cuenta.registrar(solicitud.nombre(), correo,
                cifrador.cifrar(solicitud.contrasena()), Instant.now(reloj));
        return CuentaRegistrada.desde(repositorioCuentas.guardar(cuenta));
    }
}
