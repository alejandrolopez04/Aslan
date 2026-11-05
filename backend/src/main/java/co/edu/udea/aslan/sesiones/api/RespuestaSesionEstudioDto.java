package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.dominio.EstadoSesion;
import co.edu.udea.aslan.sesiones.dominio.SesionEstudio;
import co.edu.udea.aslan.sesiones.dominio.TipoSonido;

import java.time.Instant;
import java.util.UUID;

public record RespuestaSesionEstudioDto(UUID id, UUID materiaId, UUID temaId, Instant inicio, Instant fin,
                                        EstadoSesion estado, int ciclosPlaneados, int ciclosCompletados,
                                        TipoSonido ambiente, String tecnica, RespuestaCicloDto cicloActual,
                                        RespuestaRegistroSesionDto registro) {

    static RespuestaSesionEstudioDto desde(SesionEstudio sesion) {
        return new RespuestaSesionEstudioDto(sesion.getId(), sesion.getMateriaId(), sesion.getTemaId(),
                sesion.getInicio(), sesion.getFin().orElse(null), sesion.getEstado(), sesion.getCiclosPlaneados(),
                sesion.ciclosCompletados(), sesion.getAmbiente().orElse(null), sesion.getTecnica().orElse(null),
                RespuestaCicloDto.desde(sesion.cicloActual()),
                sesion.getRegistro().map(RespuestaRegistroSesionDto::desde).orElse(null));
    }
}
