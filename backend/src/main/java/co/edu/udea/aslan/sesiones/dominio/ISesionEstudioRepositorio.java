package co.edu.udea.aslan.sesiones.dominio;

import co.edu.udea.aslan.shared.dominio.IRepositorio;

import java.util.Optional;
import java.util.UUID;

public interface ISesionEstudioRepositorio extends IRepositorio<SesionEstudio> {

    Optional<SesionEstudio> buscarAbierta(UUID estudianteId);
}
