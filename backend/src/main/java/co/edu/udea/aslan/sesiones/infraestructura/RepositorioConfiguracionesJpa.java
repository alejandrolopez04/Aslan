package co.edu.udea.aslan.sesiones.infraestructura;

import co.edu.udea.aslan.sesiones.dominio.ConfiguracionPomodoro;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class RepositorioConfiguracionesJpa implements IRepositorio<ConfiguracionPomodoro> {

    private final ConfiguracionPomodoroJpaRepositorio jpa;

    RepositorioConfiguracionesJpa(ConfiguracionPomodoroJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(ConfiguracionPomodoro valor) {
        jpa.save(ConfiguracionPomodoroEntidad.desde(valor));
    }

    @Override
    public Optional<ConfiguracionPomodoro> buscarPorId(UUID id) {
        return jpa.findById(id).map(ConfiguracionPomodoroEntidad::aDominio);
    }

    @Override
    public List<ConfiguracionPomodoro> listar() {
        return jpa.findAll().stream().map(ConfiguracionPomodoroEntidad::aDominio).toList();
    }

    @Override
    public void eliminar(UUID id) {
        jpa.deleteById(id);
    }
}
