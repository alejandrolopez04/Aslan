package co.edu.udea.aslan.usuarios.dominio;

public class PoliticaContrasenaSegura {

    static final int LONGITUD_MINIMA = 8;

    public boolean esValida(String contrasena) {
        return contrasena != null
                && contrasena.length() >= LONGITUD_MINIMA
                && contrasena.chars().anyMatch(Character::isLetter)
                && contrasena.chars().anyMatch(Character::isDigit);
    }

    public void verificar(String contrasena) {
        if (!esValida(contrasena)) {
            throw new ContrasenaInseguraException();
        }
    }
}
