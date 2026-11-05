package co.edu.udea.aslan.usuarios.api;

import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.Estudiante;

import java.util.UUID;

public record RespuestaRegistroDto(UUID id, String nombre, String correo) {

    static RespuestaRegistroDto desde(Estudiante estudiante, String correo) {
        return new RespuestaRegistroDto(estudiante.getId(), estudiante.getNombre(), Cuenta.normalizarCorreo(correo));
    }
}
