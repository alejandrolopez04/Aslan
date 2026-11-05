package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.shared.dominio.IRepositorio;
import co.edu.udea.aslan.usuarios.dominio.SesionAutenticacion;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class RepositorioSesionesJpa implements IRepositorio<SesionAutenticacion> {

    private final SesionAutenticacionJpaRepositorio jpa;

    RepositorioSesionesJpa(SesionAutenticacionJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(SesionAutenticacion sesion) {
        jpa.save(SesionAutenticacionEntidad.desde(sesion));
    }

    @Override
    public Optional<SesionAutenticacion> buscarPorId(UUID id) {
        return jpa.findById(id).map(SesionAutenticacionEntidad::aDominio);
    }

    @Override
    public List<SesionAutenticacion> listar() {
        return jpa.findAll().stream().map(SesionAutenticacionEntidad::aDominio).toList();
    }

    @Override
    public void eliminar(UUID id) {
        jpa.deleteById(id);
    }
}
