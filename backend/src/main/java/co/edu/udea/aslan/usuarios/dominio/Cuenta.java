package co.edu.udea.aslan.usuarios.dominio;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Cuenta {

    static final int INTENTOS_PERMITIDOS = 5;
    static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final UUID id;
    private final String nombre;
    private final String correo;
    private final String contrasenaHash;
    private final Instant creadaEn;
    private int intentosFallidos;
    private Instant bloqueadaHasta;

    public Cuenta(UUID id, String nombre, String correo, String contrasenaHash, Instant creadaEn,
                  int intentosFallidos, Instant bloqueadaHasta) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.correo = Objects.requireNonNull(correo);
        this.contrasenaHash = Objects.requireNonNull(contrasenaHash);
        this.creadaEn = Objects.requireNonNull(creadaEn);
        this.intentosFallidos = intentosFallidos;
        this.bloqueadaHasta = bloqueadaHasta;
    }

    public static Cuenta registrar(String nombre, String correo, String contrasenaHash, Instant ahora) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        return new Cuenta(UUID.randomUUID(), nombre.trim(), normalizarCorreo(correo), contrasenaHash, ahora, 0, null);
    }

    public static String normalizarCorreo(String correo) {
        return Objects.requireNonNull(correo).trim().toLowerCase(Locale.ROOT);
    }

    public boolean estaBloqueada(Instant ahora) {
        return bloqueadaHasta != null && ahora.isBefore(bloqueadaHasta);
    }

    public void registrarIntentoFallido(Instant ahora) {
        if (bloqueadaHasta != null && !estaBloqueada(ahora)) {
            bloqueadaHasta = null;
            intentosFallidos = 0;
        }
        intentosFallidos++;
        if (intentosFallidos >= INTENTOS_PERMITIDOS) {
            bloqueadaHasta = ahora.plus(DURACION_BLOQUEO);
        }
    }

    public void registrarIngresoExitoso() {
        intentosFallidos = 0;
        bloqueadaHasta = null;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public Optional<Instant> getBloqueadaHasta() {
        return Optional.ofNullable(bloqueadaHasta);
    }
}
