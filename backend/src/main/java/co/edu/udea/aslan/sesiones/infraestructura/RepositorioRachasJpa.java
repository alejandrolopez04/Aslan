package co.edu.udea.aslan.sesiones.infraestructura;

import co.edu.udea.aslan.sesiones.dominio.RachaEstudio;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class RepositorioRachasJpa implements IRepositorio<RachaEstudio> {

    private final RachaEstudioJpaRepositorio jpa;

    RepositorioRachasJpa(RachaEstudioJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(RachaEstudio valor) {
        jpa.save(RachaEstudioEntidad.desde(valor));
    }

    @Override
    public Optional<RachaEstudio> buscarPorId(UUID id) {
        return jpa.findById(id).map(RachaEstudioEntidad::aDominio);
    }

    @Override
    public List<RachaEstudio> listar() {
        return jpa.findAll().stream().map(RachaEstudioEntidad::aDominio).toList();
    }

    @Override
    public void eliminar(UUID id) {
        jpa.deleteById(id);
    }
}
