package co.edu.udea.aslan.materias.api;

import jakarta.validation.constraints.NotBlank;

public record SolicitudMateriaDto(
        String nombre,
        @NotBlank(message = "Selecciona un color para la materia.") String color,
        @NotBlank(message = "Selecciona un ícono para la materia.") String icono,
        boolean confirmarDuplicado) {
}
