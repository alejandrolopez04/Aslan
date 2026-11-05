package co.edu.udea.aslan.materias.api;

import co.edu.udea.aslan.materias.dominio.Materia;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RespuestaMateriaDto(UUID id, String nombre, String color, String icono, boolean archivada,
                                  double progreso, List<RespuestaTemaDto> temas) {

    static RespuestaMateriaDto desde(Materia materia, LocalDate hoy) {
        return new RespuestaMateriaDto(materia.getId(), materia.getNombre(), materia.getColor(),
                materia.getIcono(), materia.isArchivada(), materia.calcularProgreso(),
                materia.getTemas().stream().map(tema -> RespuestaTemaDto.desde(tema, hoy)).toList());
    }
}
