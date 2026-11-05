package co.edu.udea.aslan.usuarios.dominio;

import java.util.ArrayList;
import java.util.List;

public class PoliticaContrasenaSegura implements IPoliticaContrasena {

    private static final int LONGITUD_MINIMA = 8;

    @Override
    public ResultadoValidacion validar(String contrasena) {
        String texto = contrasena == null ? "" : contrasena;
        List<String> errores = new ArrayList<>();
        if (texto.length() < LONGITUD_MINIMA) {
            errores.add("Debe tener al menos " + LONGITUD_MINIMA + " caracteres.");
        }
        if (texto.chars().noneMatch(Character::isLetter)) {
            errores.add("Debe incluir al menos una letra.");
        }
        if (texto.chars().noneMatch(Character::isDigit)) {
            errores.add("Debe incluir al menos un número.");
        }
        return ResultadoValidacion.con(errores);
    }

    @Override
    public String describirRequisitos() {
        return "La contraseña aún no es válida. Debe tener al menos " + LONGITUD_MINIMA
                + " caracteres, una letra y un número.";
    }
}
