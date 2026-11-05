package co.edu.udea.aslan.sesiones.infraestructura;

import co.edu.udea.aslan.sesiones.dominio.RegistroSesion;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class RepositorioRegistrosJpa implements IRepositorio<RegistroSesion> {

    private final RegistroSesionJpaRepositorio jpa;

    RepositorioRegistrosJpa(RegistroSesionJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(RegistroSesion valor) {
        jpa.save(RegistroSesionEntidad.desde(valor));
    }

    @Override
    public Optional<RegistroSesion> buscarPorId(UUID id) {
        return jpa.findById(id).map(RegistroSesionEntidad::aDominio);
    }

    @Override
    public List<RegistroSesion> listar() {
        return jpa.findAll().stream().map(RegistroSesionEntidad::aDominio).toList();
    }

    @Override
    public void eliminar(UUID id) {
        jpa.deleteById(id);
    }
}
