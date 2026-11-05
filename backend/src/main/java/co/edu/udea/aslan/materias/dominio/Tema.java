package co.edu.udea.aslan.materias.dominio;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Tema {

    private final UUID id;
    private final String nombre;
    private final EstadoTema estado;
    private final Prioridad prioridad;
    private final LocalDate fechaLimite;
    private final String notas;

    public Tema(UUID id, String nombre, EstadoTema estado, Prioridad prioridad, LocalDate fechaLimite, String notas) {
        this.id = Objects.requireNonNull(id);
        this.nombre = NombreObligatorio.validar(nombre, "Ingresa un nombre para el tema.");
        this.estado = Objects.requireNonNullElse(estado, EstadoTema.PENDIENTE);
        this.prioridad = Objects.requireNonNullElse(prioridad, Prioridad.MEDIA);
        this.fechaLimite = fechaLimite;
        this.notas = Objects.requireNonNullElse(notas, "");
    }

    public static Tema nuevo(String nombre, EstadoTema estado, Prioridad prioridad, LocalDate fechaLimite,
                             String notas) {
        return new Tema(UUID.randomUUID(), nombre, estado, prioridad, fechaLimite, notas);
    }

    public boolean estaVencido(LocalDate hoy) {
        return fechaLimite != null && hoy.isAfter(fechaLimite) && estado != EstadoTema.COMPLETADO;
    }

    public boolean estaCompletado() {
        return estado == EstadoTema.COMPLETADO;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoTema getEstado() {
        return estado;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public Optional<LocalDate> getFechaLimite() {
        return Optional.ofNullable(fechaLimite);
    }

    public String getNotas() {
        return notas;
    }
}
