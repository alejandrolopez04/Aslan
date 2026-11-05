package co.edu.udea.aslan.usuarios.dominio;

public interface IServicioHash {

    String generarHash(String texto);

    boolean coincide(String texto, String hash);
}
