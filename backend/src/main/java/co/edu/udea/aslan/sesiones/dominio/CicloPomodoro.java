package co.edu.udea.aslan.sesiones.dominio;

import java.util.Objects;

public abstract class CicloPomodoro {

    protected final int numero;
    protected final int duracionPlaneadaSeg;
    protected int transcurridoSeg;
    protected EstadoCiclo estado;

    protected CicloPomodoro(int numero, int duracionPlaneadaSeg, int transcurridoSeg, EstadoCiclo estado) {
        this.numero = numero;
        this.duracionPlaneadaSeg = duracionPlaneadaSeg;
        this.transcurridoSeg = transcurridoSeg;
        this.estado = Objects.requireNonNull(estado);
    }

    public void iniciar() {
        exigir(EstadoCiclo.NO_INICIADO);
        estado = EstadoCiclo.EN_CURSO;
    }

    public void pausar(int transcurrido) {
        exigir(EstadoCiclo.EN_CURSO);
        registrarTranscurrido(transcurrido);
        estado = EstadoCiclo.PAUSADO;
    }

    public void reanudar() {
        exigir(EstadoCiclo.PAUSADO);
        estado = EstadoCiclo.EN_CURSO;
    }

    public void detener(int transcurrido) {
        if (haFinalizado()) {
            throw new AccionNoPermitidaException();
        }
        registrarTranscurrido(transcurrido);
        estado = haTerminado() ? EstadoCiclo.COMPLETADO : EstadoCiclo.DETENIDO;
    }

    public boolean haTerminado() {
        return transcurridoSeg >= duracionPlaneadaSeg;
    }

    public boolean estaActivo() {
        return estado == EstadoCiclo.EN_CURSO || estado == EstadoCiclo.PAUSADO;
    }

    public abstract CicloPomodoro siguiente(ConfiguracionPomodoro cfg);

    public abstract TipoCiclo getTipo();

    abstract boolean iniciaAutomaticamente();

    abstract int segundosDeEstudio();

    abstract boolean esTrabajoCompletado();

    public int getNumero() {
        return numero;
    }

    public int getDuracionPlaneadaSeg() {
        return duracionPlaneadaSeg;
    }

    public int getTranscurridoSeg() {
        return transcurridoSeg;
    }

    public EstadoCiclo getEstado() {
        return estado;
    }

    private boolean haFinalizado() {
        return estado == EstadoCiclo.COMPLETADO || estado == EstadoCiclo.DETENIDO;
    }

    private void exigir(EstadoCiclo esperado) {
        if (estado != esperado) {
            throw new AccionNoPermitidaException();
        }
    }

    private void registrarTranscurrido(int transcurrido) {
        if (transcurrido < 0) {
            throw new AccionNoPermitidaException();
        }
        transcurridoSeg = Math.max(transcurridoSeg, Math.min(transcurrido, duracionPlaneadaSeg));
    }
}
