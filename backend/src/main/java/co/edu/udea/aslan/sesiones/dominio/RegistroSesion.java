package co.edu.udea.aslan.sesiones.dominio;

import java.time.Instant;
import java.util.UUID;

public record RegistroSesion(UUID sesionId, UUID estudianteId, UUID materiaId, UUID temaId, TipoSonido ambiente,
                             Instant fecha, int duracionEfectivaMin, int ciclosCompletados, boolean completada) {

    private static final double SEGUNDOS_POR_MINUTO = 60.0;

    public static RegistroSesion desde(SesionEstudio sesion) {
        return new RegistroSesion(sesion.getId(), sesion.getEstudianteId(), sesion.getMateriaId(),
                sesion.getTemaId(), sesion.getAmbiente().orElse(null), sesion.getFin().orElseThrow(),
                (int) Math.round(sesion.segundosDeEstudio() / SEGUNDOS_POR_MINUTO), sesion.ciclosCompletados(),
                sesion.getEstado() == EstadoSesion.FINALIZADA);
    }
}
