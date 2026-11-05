package co.edu.udea.aslan.usuarios.dominio;

public interface IRepositorioCuentas {

    boolean existePorCorreo(String correo);

    Cuenta guardar(Cuenta cuenta);
}
