package co.edu.udea.aslan.materias.api;

import co.edu.udea.aslan.materias.dominio.EstadoTema;
import co.edu.udea.aslan.materias.dominio.Prioridad;
import co.edu.udea.aslan.materias.dominio.Tema;

import java.time.LocalDate;
import java.util.UUID;

public record RespuestaTemaDto(UUID id, String nombre, EstadoTema estado, Prioridad prioridad,
                               LocalDate fechaLimite, boolean sinVencimiento, boolean vencido, String notas) {

    static RespuestaTemaDto desde(Tema tema, LocalDate hoy) {
        return new RespuestaTemaDto(tema.getId(), tema.getNombre(), tema.getEstado(), tema.getPrioridad(),
                tema.getFechaLimite().orElse(null), tema.getFechaLimite().isEmpty(), tema.estaVencido(hoy),
                tema.getNotas());
    }
}
