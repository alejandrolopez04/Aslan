package co.edu.udea.aslan.sesiones.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SesionEstudio {

    private final UUID id;
    private final UUID estudianteId;
    private final UUID materiaId;
    private final UUID temaId;
    private final Instant inicio;
    private Instant fin;
    private EstadoSesion estado;
    private final int ciclosPlaneados;
    private final TipoSonido ambiente;
    private final String tecnica;
    private final List<CicloPomodoro> ciclos;
    private RegistroSesion registro;

    public SesionEstudio(UUID id, UUID estudianteId, UUID materiaId, UUID temaId, Instant inicio, Instant fin,
                         EstadoSesion estado, int ciclosPlaneados, TipoSonido ambiente, String tecnica,
                         List<CicloPomodoro> ciclos) {
        this.id = Objects.requireNonNull(id);
        this.estudianteId = Objects.requireNonNull(estudianteId);
        this.materiaId = Objects.requireNonNull(materiaId);
        this.temaId = Objects.requireNonNull(temaId);
        this.inicio = Objects.requireNonNull(inicio);
        this.fin = fin;
        this.estado = Objects.requireNonNull(estado);
        this.ciclosPlaneados = ciclosPlaneados;
        this.ambiente = ambiente;
        this.tecnica = tecnica;
        this.ciclos = new ArrayList<>(ciclos);
    }

    public static SesionEstudio iniciar(UUID estudianteId, UUID materiaId, UUID temaId, TipoSonido ambiente,
                                        String tecnica, ConfiguracionPomodoro cfg, Instant ahora) {
        return new SesionEstudio(UUID.randomUUID(), estudianteId, materiaId, temaId, ahora, null,
                EstadoSesion.EN_CURSO, cfg.getCiclosParaDescansoLargo(), ambiente, tecnica,
                List.of(CicloTrabajo.nuevo(1, cfg)));
    }

    public CicloPomodoro cicloActual() {
        return ciclos.getLast();
    }

    public void iniciarCiclo() {
        exigirAbierta();
        cicloActual().iniciar();
        estado = EstadoSesion.EN_CURSO;
    }

    public void pausar(int transcurridoSeg) {
        exigirAbierta();
        cicloActual().pausar(transcurridoSeg);
        estado = EstadoSesion.PAUSADA;
    }

    public void reanudar() {
        exigirAbierta();
        cicloActual().reanudar();
        estado = EstadoSesion.EN_CURSO;
    }

    public void avanzarCiclo(int transcurridoSeg, ConfiguracionPomodoro cfg, Instant ahora) {
        exigirAbierta();
        cicloActual().detener(transcurridoSeg);
        estado = EstadoSesion.EN_CURSO;
        if (completoCiclosPlaneados()) {
            finalizar(true, transcurridoSeg, ahora);
            return;
        }
        CicloPomodoro siguiente = cicloActual().siguiente(cfg);
        ciclos.add(siguiente);
        if (siguiente.iniciaAutomaticamente()) {
            siguiente.iniciar();
        }
    }

    public Optional<RegistroSesion> finalizar(boolean guardarProgreso, int transcurridoSeg, Instant ahora) {
        exigirAbierta();
        if (cicloActual().estaActivo()) {
            cicloActual().detener(transcurridoSeg);
        }
        estado = completoCiclosPlaneados() ? EstadoSesion.FINALIZADA : EstadoSesion.FINALIZADA_ANTICIPADA;
        fin = ahora;
        registro = guardarProgreso ? RegistroSesion.desde(this) : null;
        return getRegistro();
    }

    public boolean tieneCicloActivo() {
        return estaAbierta() && cicloActual().estaActivo();
    }

    public boolean estaAbierta() {
        return fin == null;
    }

    public boolean perteneceA(UUID estudiante) {
        return estudianteId.equals(estudiante);
    }

    public int ciclosCompletados() {
        return (int) ciclos.stream().filter(CicloPomodoro::esTrabajoCompletado).count();
    }

    int segundosDeEstudio() {
        return ciclos.stream().mapToInt(CicloPomodoro::segundosDeEstudio).sum();
    }

    private boolean completoCiclosPlaneados() {
        return ciclosCompletados() >= ciclosPlaneados;
    }

    private void exigirAbierta() {
        if (!estaAbierta()) {
            throw new SesionFinalizadaException();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getEstudianteId() {
        return estudianteId;
    }

    public UUID getMateriaId() {
        return materiaId;
    }

    public UUID getTemaId() {
        return temaId;
    }

    public Instant getInicio() {
        return inicio;
    }

    public Optional<Instant> getFin() {
        return Optional.ofNullable(fin);
    }

    public EstadoSesion getEstado() {
        return estado;
    }

    public int getCiclosPlaneados() {
        return ciclosPlaneados;
    }

    public Optional<TipoSonido> getAmbiente() {
        return Optional.ofNullable(ambiente);
    }

    public Optional<String> getTecnica() {
        return Optional.ofNullable(tecnica);
    }

    public List<CicloPomodoro> getCiclos() {
        return List.copyOf(ciclos);
    }

    public Optional<RegistroSesion> getRegistro() {
        return Optional.ofNullable(registro);
    }
}
