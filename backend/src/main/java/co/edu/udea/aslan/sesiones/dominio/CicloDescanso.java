package co.edu.udea.aslan.sesiones.dominio;

public class CicloDescanso extends CicloPomodoro {

    private final boolean largo;

    public CicloDescanso(int numero, int duracionPlaneadaSeg, int transcurridoSeg, EstadoCiclo estado,
                         boolean largo) {
        super(numero, duracionPlaneadaSeg, transcurridoSeg, estado);
        this.largo = largo;
    }

    static CicloDescanso nuevo(int numero, boolean largo, ConfiguracionPomodoro cfg) {
        return new CicloDescanso(numero, cfg.duracionDescansoSeg(largo), 0, EstadoCiclo.NO_INICIADO, largo);
    }

    @Override
    public CicloPomodoro siguiente(ConfiguracionPomodoro cfg) {
        return CicloTrabajo.nuevo(numero + 1, cfg);
    }

    @Override
    public TipoCiclo getTipo() {
        return largo ? TipoCiclo.DESCANSO_LARGO : TipoCiclo.DESCANSO_CORTO;
    }

    @Override
    boolean iniciaAutomaticamente() {
        return true;
    }

    @Override
    int segundosDeEstudio() {
        return 0;
    }

    @Override
    boolean esTrabajoCompletado() {
        return false;
    }
}
