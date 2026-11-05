package co.edu.udea.aslan.usuarios.api;

import co.edu.udea.aslan.usuarios.dominio.SesionAutenticacion;

import java.time.Instant;

public record RespuestaSesionDto(String token, Instant expiracion) {

    static RespuestaSesionDto desde(SesionAutenticacion sesion) {
        return new RespuestaSesionDto(sesion.getToken(), sesion.getExpiracion());
    }
}
