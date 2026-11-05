package co.edu.udea.aslan.ia.infraestructura;

import co.edu.udea.aslan.ia.dominio.PerfilAprendizaje;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class RepositorioPerfilesJpa implements IRepositorio<PerfilAprendizaje> {

    private final PerfilAprendizajeJpaRepositorio jpa;

    RepositorioPerfilesJpa(PerfilAprendizajeJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(PerfilAprendizaje perfil) {
        jpa.save(PerfilAprendizajeEntidad.desde(perfil));
    }

    @Override
    public Optional<PerfilAprendizaje> buscarPorId(UUID estudianteId) {
        return jpa.findById(estudianteId).map(PerfilAprendizajeEntidad::aDominio);
    }

    @Override
    public List<PerfilAprendizaje> listar() {
        return jpa.findAll().stream().map(PerfilAprendizajeEntidad::aDominio).toList();
    }

    @Override
    public void eliminar(UUID estudianteId) {
        jpa.deleteById(estudianteId);
    }
}
