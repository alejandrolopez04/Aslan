package co.edu.udea.aslan.sesiones.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

import java.util.Map;

public class DuracionFueraDeRangoException extends ExcepcionNegocio {

    private final int minimo;
    private final int maximo;

    public DuracionFueraDeRangoException(int minimo, int maximo) {
        super("DURACION_FUERA_DE_RANGO", TipoError.DATOS_INVALIDOS,
                "La duración debe estar entre " + minimo + " y " + maximo + " minutos.");
        this.minimo = minimo;
        this.maximo = maximo;
    }

    @Override
    public Map<String, Object> getDetalles() {
        return Map.of("minimo", minimo, "maximo", maximo);
    }
}
