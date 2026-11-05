package co.edu.udea.aslan.materias.aplicacion;

import co.edu.udea.aslan.materias.dominio.EstadoTema;
import co.edu.udea.aslan.materias.dominio.Prioridad;

import java.time.LocalDate;

public record DatosTema(String nombre, EstadoTema estado, Prioridad prioridad, LocalDate fechaLimite, String notas) {
}
