package co.edu.udea.aslan.materias.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Materia {

    private final UUID id;
    private final UUID estudianteId;
    private final String nombre;
    private final String color;
    private final String icono;
    private final boolean archivada;
    private final List<Tema> temas;

    public Materia(UUID id, UUID estudianteId, String nombre, String color, String icono, boolean archivada,
                   List<Tema> temas) {
        this.id = Objects.requireNonNull(id);
        this.estudianteId = Objects.requireNonNull(estudianteId);
        this.nombre = NombreObligatorio.validar(nombre, "Ingresa un nombre para la materia.");
        this.color = Objects.requireNonNull(color);
        this.icono = Objects.requireNonNull(icono);
        this.archivada = archivada;
        this.temas = new ArrayList<>(temas);
    }

    public static Materia crear(UUID estudianteId, String nombre, String color, String icono) {
        return new Materia(UUID.randomUUID(), estudianteId, nombre, color, icono, false, List.of());
    }

    public void agregarTema(Tema tema) {
        temas.add(Objects.requireNonNull(tema));
    }

    public double calcularProgreso() {
        if (temas.isEmpty()) {
            return 0;
        }
        return (double) temas.stream().filter(Tema::estaCompletado).count() / temas.size();
    }

    public boolean perteneceA(UUID estudiante) {
        return estudianteId.equals(estudiante);
    }

    public Optional<Tema> buscarTema(UUID temaId) {
        return temas.stream().filter(tema -> tema.getId().equals(temaId)).findFirst();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEstudianteId() {
        return estudianteId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getColor() {
        return color;
    }

    public String getIcono() {
        return icono;
    }

    public boolean isArchivada() {
        return archivada;
    }

    public List<Tema> getTemas() {
        return List.copyOf(temas);
    }
}
