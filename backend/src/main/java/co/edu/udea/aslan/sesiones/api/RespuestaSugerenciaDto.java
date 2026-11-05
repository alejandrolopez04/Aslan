package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.aplicacion.Sugerencia;
import co.edu.udea.aslan.sesiones.dominio.TipoSonido;

public record RespuestaSugerenciaDto(boolean personalizada, TipoSonido ambiente, String tecnica, String motivo) {

    static RespuestaSugerenciaDto desde(Sugerencia sugerencia) {
        return new RespuestaSugerenciaDto(sugerencia.personalizada(), sugerencia.ambiente(), sugerencia.tecnica(),
                sugerencia.motivo());
    }
}
