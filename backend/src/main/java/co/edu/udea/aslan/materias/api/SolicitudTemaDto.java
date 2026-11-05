package co.edu.udea.aslan.materias.api;

import co.edu.udea.aslan.materias.aplicacion.DatosTema;
import co.edu.udea.aslan.materias.dominio.EstadoTema;
import co.edu.udea.aslan.materias.dominio.Prioridad;

import java.time.LocalDate;

public record SolicitudTemaDto(String nombre, EstadoTema estado, Prioridad prioridad, LocalDate fechaLimite,
                               String notas) {

    DatosTema aDatos() {
        return new DatosTema(nombre, estado, prioridad, fechaLimite, notas);
    }
}
