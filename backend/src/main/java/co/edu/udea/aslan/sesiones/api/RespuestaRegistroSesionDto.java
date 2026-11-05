package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.dominio.RegistroSesion;

public record RespuestaRegistroSesionDto(int duracionEfectivaMin, int ciclosCompletados, boolean completada) {

    static RespuestaRegistroSesionDto desde(RegistroSesion registro) {
        return new RespuestaRegistroSesionDto(registro.duracionEfectivaMin(), registro.ciclosCompletados(),
                registro.completada());
    }
}
