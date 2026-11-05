package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.dominio.CicloPomodoro;
import co.edu.udea.aslan.sesiones.dominio.EstadoCiclo;
import co.edu.udea.aslan.sesiones.dominio.TipoCiclo;

public record RespuestaCicloDto(int numero, TipoCiclo tipo, int duracionPlaneadaSeg, int transcurridoSeg,
                                EstadoCiclo estado) {

    static RespuestaCicloDto desde(CicloPomodoro ciclo) {
        return new RespuestaCicloDto(ciclo.getNumero(), ciclo.getTipo(), ciclo.getDuracionPlaneadaSeg(),
                ciclo.getTranscurridoSeg(), ciclo.getEstado());
    }
}
