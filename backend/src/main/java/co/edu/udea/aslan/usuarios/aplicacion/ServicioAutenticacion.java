package co.edu.udea.aslan.usuarios.aplicacion;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.usuarios.dominio.CredencialesInvalidasException;
import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.CuentaBloqueadaException;
import co.edu.udea.aslan.usuarios.dominio.ICifradorContrasenas;
import co.edu.udea.aslan.usuarios.dominio.IGeneradorTokens;
import co.edu.udea.aslan.usuarios.dominio.IRepositorioCuentas;
import co.edu.udea.aslan.usuarios.dominio.IRepositorioTokens;
import co.edu.udea.aslan.usuarios.dominio.TokenSesion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ServicioAutenticacion {

    private final IRepositorioCuentas repositorioCuentas;
    private final IRepositorioTokens repositorioTokens;
    private final ICifradorContrasenas cifrador;
    private final IGeneradorTokens generadorTokens;
    private final Clock reloj;

    public ServicioAutenticacion(IRepositorioCuentas repositorioCuentas, IRepositorioTokens repositorioTokens,
                                 ICifradorContrasenas cifrador, IGeneradorTokens generadorTokens, Clock reloj) {
        this.repositorioCuentas = repositorioCuentas;
        this.repositorioTokens = repositorioTokens;
        this.cifrador = cifrador;
        this.generadorTokens = generadorTokens;
        this.reloj = reloj;
    }

    @Transactional(noRollbackFor = ExcepcionNegocio.class)
    public SesionIniciada iniciarSesion(String correo, String contrasena) {
        Instant ahora = Instant.now(reloj);
        Cuenta cuenta = repositorioCuentas.buscarPorCorreo(Cuenta.normalizarCorreo(correo))
                .orElseThrow(CredencialesInvalidasException::new);
        verificarQueNoEsteBloqueada(cuenta, ahora);

        if (!cifrador.coincide(contrasena, cuenta.getContrasenaHash())) {
            cuenta.registrarIntentoFallido(ahora);
            repositorioCuentas.guardar(cuenta);
            verificarQueNoEsteBloqueada(cuenta, ahora);
            throw new CredencialesInvalidasException();
        }

        cuenta.registrarIngresoExitoso();
        repositorioCuentas.guardar(cuenta);
        TokenSesion token = repositorioTokens.guardar(
                TokenSesion.emitir(generadorTokens.generar(), cuenta.getId(), ahora));
        return new SesionIniciada(token.valor(), token.expiraEn(), ResumenCuenta.desde(cuenta));
    }

    private void verificarQueNoEsteBloqueada(Cuenta cuenta, Instant ahora) {
        if (cuenta.estaBloqueada(ahora)) {
            throw new CuentaBloqueadaException(cuenta.getBloqueadaHasta().orElseThrow());
        }
    }
}
