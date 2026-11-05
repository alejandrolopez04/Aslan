package co.edu.udea.aslan.ia.dominio;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PerfilAprendizaje {

    private static final int SESIONES_MINIMAS = 1;
    private static final double PESO_COMPLETADA = 1.0;
    private static final double PESO_INCOMPLETA = 0.5;

    private final UUID estudianteId;
    private int sesionesProcesadas;
    private final Map<String, Double> pesos;

    public PerfilAprendizaje(UUID estudianteId, int sesionesProcesadas, Map<String, Double> pesos) {
        this.estudianteId = Objects.requireNonNull(estudianteId);
        this.sesionesProcesadas = sesionesProcesadas;
        this.pesos = new HashMap<>(pesos);
    }

    public static PerfilAprendizaje nuevo(UUID estudianteId) {
        return new PerfilAprendizaje(estudianteId, 0, Map.of());
    }

    public void aprender(String ambiente, boolean completada) {
        sesionesProcesadas++;
        if (ambiente != null) {
            pesos.merge(ambiente, completada ? PESO_COMPLETADA : PESO_INCOMPLETA, Double::sum);
        }
    }

    public boolean tieneDatosSuficientes() {
        return sesionesProcesadas >= SESIONES_MINIMAS;
    }

    public Optional<String> ambientePreferido() {
        return pesos.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }

    public UUID getEstudianteId() {
        return estudianteId;
    }

    public int getSesionesProcesadas() {
        return sesionesProcesadas;
    }

    public Map<String, Double> getPesos() {
        return Map.copyOf(pesos);
    }
}
