package co.edu.udea.aslan.usuarios.dominio;

public interface ICifradorContrasenas {

    String cifrar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String contrasenaHash);
}
