package co.edu.udea.aslan.sesiones.dominio;

public class CicloTrabajo extends CicloPomodoro {

    public CicloTrabajo(int numero, int duracionPlaneadaSeg, int transcurridoSeg, EstadoCiclo estado) {
        super(numero, duracionPlaneadaSeg, transcurridoSeg, estado);
    }

    static CicloTrabajo nuevo(int numero, ConfiguracionPomodoro cfg) {
        return new CicloTrabajo(numero, cfg.duracionTrabajoSeg(), 0, EstadoCiclo.NO_INICIADO);
    }

    @Override
    public CicloPomodoro siguiente(ConfiguracionPomodoro cfg) {
        return CicloDescanso.nuevo(numero, cfg.tocaDescansoLargo(numero), cfg);
    }

    @Override
    public TipoCiclo getTipo() {
        return TipoCiclo.TRABAJO;
    }

    @Override
    boolean iniciaAutomaticamente() {
        return false;
    }

    @Override
    int segundosDeEstudio() {
        return transcurridoSeg;
    }

    @Override
    boolean esTrabajoCompletado() {
        return estado == EstadoCiclo.COMPLETADO;
    }
}
