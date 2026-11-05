package co.edu.udea.aslan.sesiones.api;

import co.edu.udea.aslan.sesiones.dominio.TipoSonido;

import java.util.UUID;

public record SolicitudInicioSesionDto(UUID materiaId, UUID temaId, TipoSonido ambiente) {
}
