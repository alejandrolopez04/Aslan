package co.edu.udea.aslan.usuarios.dominio;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Cuenta {

    static final int MAX_INTENTOS = 5;
    static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final UUID estudianteId;
    private final String correo;
    private final String contrasenaHash;
    private int intentosFallidos;
    private Instant bloqueadaHasta;

    public Cuenta(UUID estudianteId, String correo, String contrasenaHash, int intentosFallidos,
                  Instant bloqueadaHasta) {
        this.estudianteId = Objects.requireNonNull(estudianteId);
        this.correo = Objects.requireNonNull(correo);
        this.contrasenaHash = Objects.requireNonNull(contrasenaHash);
        this.intentosFallidos = intentosFallidos;
        this.bloqueadaHasta = bloqueadaHasta;
    }

    public static Cuenta abrir(UUID estudianteId, String correo, String contrasenaHash) {
        return new Cuenta(estudianteId, normalizarCorreo(correo), contrasenaHash, 0, null);
    }

    public static String normalizarCorreo(String correo) {
        return Objects.requireNonNull(correo).trim().toLowerCase(Locale.ROOT);
    }

    public boolean verificar(String contrasena, IServicioHash hasher) {
        return hasher.coincide(contrasena, contrasenaHash);
    }

    public boolean estaBloqueada(Instant ahora) {
        return bloqueadaHasta != null && ahora.isBefore(bloqueadaHasta);
    }

    public void registrarIntentoFallido(Instant ahora) {
        if (bloqueadaHasta != null && !estaBloqueada(ahora)) {
            reiniciarIntentos();
        }
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueadaHasta = ahora.plus(DURACION_BLOQUEO);
        }
    }

    public void reiniciarIntentos() {
        intentosFallidos = 0;
        bloqueadaHasta = null;
    }

    public UUID getEstudianteId() {
        return estudianteId;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public Optional<Instant> getBloqueadaHasta() {
        return Optional.ofNullable(bloqueadaHasta);
    }
}
