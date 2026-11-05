package co.edu.udea.aslan.sesiones.infraestructura;

import co.edu.udea.aslan.sesiones.dominio.ISesionEstudioRepositorio;
import co.edu.udea.aslan.sesiones.dominio.SesionEstudio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class RepositorioSesionesEstudioJpa implements ISesionEstudioRepositorio {

    private final SesionEstudioJpaRepositorio jpa;

    RepositorioSesionesEstudioJpa(SesionEstudioJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(SesionEstudio sesion) {
        jpa.save(SesionEstudioEntidad.desde(sesion));
    }

    @Override
    public Optional<SesionEstudio> buscarPorId(UUID id) {
        return jpa.findById(id).map(SesionEstudioEntidad::aDominio);
    }

    @Override
    public List<SesionEstudio> listar() {
        return jpa.findAll().stream().map(SesionEstudioEntidad::aDominio).toList();
    }

    @Override
    public void eliminar(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public Optional<SesionEstudio> buscarAbierta(UUID estudianteId) {
        return jpa.findFirstByEstudianteIdAndFinIsNull(estudianteId).map(SesionEstudioEntidad::aDominio);
    }
}
