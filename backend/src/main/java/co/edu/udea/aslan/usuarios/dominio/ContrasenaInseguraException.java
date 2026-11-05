package co.edu.udea.aslan.usuarios.dominio;

import co.edu.udea.aslan.shared.dominio.ExcepcionNegocio;
import co.edu.udea.aslan.shared.dominio.TipoError;

import java.util.List;
import java.util.Map;

public class ContrasenaInseguraException extends ExcepcionNegocio {

    private final List<String> errores;

    public ContrasenaInseguraException(String requisitos, List<String> errores) {
        super("CONTRASENA_INSEGURA", TipoError.DATOS_INVALIDOS, requisitos);
        this.errores = List.copyOf(errores);
    }

    @Override
    public Map<String, Object> getDetalles() {
        return Map.of("errores", errores);
    }
}
