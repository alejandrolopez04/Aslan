package co.edu.udea.aslan.usuarios.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Estudiante {

    private final UUID id;
    private final String nombre;
    private final Instant fechaRegistro;

    public Estudiante(UUID id, String nombre, Instant fechaRegistro) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.fechaRegistro = Objects.requireNonNull(fechaRegistro);
    }

    public static Estudiante registrar(String nombre, Instant ahora) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        return new Estudiante(UUID.randomUUID(), nombre.trim(), ahora);
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }
}
