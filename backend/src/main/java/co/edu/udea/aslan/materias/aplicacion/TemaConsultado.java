package co.edu.udea.aslan.materias.aplicacion;

import java.util.UUID;

public record TemaConsultado(UUID materiaId, String materia, UUID temaId, String tema, String prioridad) {
}
