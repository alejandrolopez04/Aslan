package co.edu.udea.aslan.usuarios.dominio;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Cuenta {

    private final UUID id;
    private final String nombre;
    private final String correo;
    private final String contrasenaHash;
    private final Instant creadaEn;

    public Cuenta(UUID id, String nombre, String correo, String contrasenaHash, Instant creadaEn) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.correo = Objects.requireNonNull(correo);
        this.contrasenaHash = Objects.requireNonNull(contrasenaHash);
        this.creadaEn = Objects.requireNonNull(creadaEn);
    }

    public static Cuenta registrar(String nombre, String correo, String contrasenaHash, Instant ahora) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        return new Cuenta(UUID.randomUUID(), nombre.trim(), normalizarCorreo(correo), contrasenaHash, ahora);
    }

    public static String normalizarCorreo(String correo) {
        return Objects.requireNonNull(correo).trim().toLowerCase(Locale.ROOT);
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
}
