package co.edu.udea.aslan.usuarios.dominio;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class SesionAutenticacion {

    static final Duration VIGENCIA = Duration.ofHours(2);

    private final UUID id;
    private final UUID estudianteId;
    private final String token;
    private final Instant inicio;
    private final Instant expiracion;
    private boolean activa;

    public SesionAutenticacion(UUID id, UUID estudianteId, String token, Instant inicio, Instant expiracion,
                               boolean activa) {
        this.id = Objects.requireNonNull(id);
        this.estudianteId = Objects.requireNonNull(estudianteId);
        this.token = Objects.requireNonNull(token);
        this.inicio = Objects.requireNonNull(inicio);
        this.expiracion = Objects.requireNonNull(expiracion);
        this.activa = activa;
    }

    public static SesionAutenticacion abrir(Estudiante estudiante, Cuenta cuenta, Instant ahora,
                                            IServicioTokens tokens) {
        UUID id = UUID.randomUUID();
        Instant expiracion = ahora.plus(VIGENCIA);
        String token = tokens.firmar(new ReclamosToken(id, estudiante.getId(), estudiante.getNombre(),
                cuenta.getCorreo(), ahora, expiracion));
        return new SesionAutenticacion(id, estudiante.getId(), token, ahora, expiracion, true);
    }

    public boolean estaVigente(Instant ahora) {
        return activa && ahora.isBefore(expiracion);
    }

    public void invalidar() {
        activa = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEstudianteId() {
        return estudianteId;
    }

    public String getToken() {
        return token;
    }

    public Instant getInicio() {
        return inicio;
    }

    public Instant getExpiracion() {
        return expiracion;
    }

    public boolean isActiva() {
        return activa;
    }
}
