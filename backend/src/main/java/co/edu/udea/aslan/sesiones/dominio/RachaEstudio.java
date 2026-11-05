package co.edu.udea.aslan.sesiones.dominio;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class RachaEstudio {

    private final UUID estudianteId;
    private int rachaActual;
    private int rachaMaxima;
    private LocalDate ultimoDiaEstudio;

    public RachaEstudio(UUID estudianteId, int rachaActual, int rachaMaxima, LocalDate ultimoDiaEstudio) {
        this.estudianteId = Objects.requireNonNull(estudianteId);
        this.rachaActual = rachaActual;
        this.rachaMaxima = rachaMaxima;
        this.ultimoDiaEstudio = ultimoDiaEstudio;
    }

    public static RachaEstudio nueva(UUID estudianteId) {
        return new RachaEstudio(estudianteId, 0, 0, null);
    }

    public void actualizarRacha(LocalDate fecha) {
        if (fecha.equals(ultimoDiaEstudio)) {
            return;
        }
        rachaActual = fecha.minusDays(1).equals(ultimoDiaEstudio) ? rachaActual + 1 : 1;
        rachaMaxima = Math.max(rachaMaxima, rachaActual);
        ultimoDiaEstudio = fecha;
    }

    public UUID getEstudianteId() {
        return estudianteId;
    }

    public int getRachaActual() {
        return rachaActual;
    }

    public int getRachaMaxima() {
        return rachaMaxima;
    }

    public Optional<LocalDate> getUltimoDiaEstudio() {
        return Optional.ofNullable(ultimoDiaEstudio);
    }
}
