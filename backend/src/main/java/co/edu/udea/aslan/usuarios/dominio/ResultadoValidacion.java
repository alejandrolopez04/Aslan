package co.edu.udea.aslan.usuarios.dominio;

import java.util.List;

public record ResultadoValidacion(boolean valido, List<String> errores) {

    public ResultadoValidacion {
        errores = List.copyOf(errores);
    }

    public static ResultadoValidacion con(List<String> errores) {
        return new ResultadoValidacion(errores.isEmpty(), errores);
    }

    public boolean esValido() {
        return valido;
    }
}
