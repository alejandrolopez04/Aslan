package co.edu.udea.aslan.usuarios.dominio;

import java.util.Optional;

public interface IRepositorioCuentas {

    boolean existePorCorreo(String correo);

    Optional<Cuenta> buscarPorCorreo(String correo);

    Cuenta guardar(Cuenta cuenta);
}
