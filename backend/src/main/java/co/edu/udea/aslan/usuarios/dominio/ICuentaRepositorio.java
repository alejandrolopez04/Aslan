package co.edu.udea.aslan.usuarios.dominio;

import co.edu.udea.aslan.shared.dominio.IRepositorio;

import java.util.Optional;

public interface ICuentaRepositorio extends IRepositorio<Cuenta> {

    Optional<Cuenta> buscarPorCorreo(String correo);

    boolean existeCorreo(String correo);
}
