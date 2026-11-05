package co.edu.udea.aslan.sesiones.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

import java.util.List;
import java.util.Map;

public class SeleccionIncompletaException extends ExcepcionNegocio {

    private static final Map<String, String> ETIQUETAS = Map.of("materia", "la materia", "tema", "el tema");

    private final List<String> faltantes;

    public SeleccionIncompletaException(List<String> faltantes) {
        super("SELECCION_INCOMPLETA", TipoError.DATOS_INVALIDOS, "Falta seleccionar "
                + String.join(" y ", faltantes.stream().map(ETIQUETAS::get).toList()) + ".");
        this.faltantes = List.copyOf(faltantes);
    }

    @Override
    public Map<String, Object> getDetalles() {
        return Map.of("faltantes", faltantes);
    }
}
