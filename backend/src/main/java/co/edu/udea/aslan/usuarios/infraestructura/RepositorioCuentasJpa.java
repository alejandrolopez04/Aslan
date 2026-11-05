package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.ICuentaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class RepositorioCuentasJpa implements ICuentaRepositorio {

    private final CuentaJpaRepositorio jpa;

    RepositorioCuentasJpa(CuentaJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Cuenta cuenta) {
        jpa.save(CuentaEntidad.desde(cuenta));
    }

    @Override
    public Optional<Cuenta> buscarPorId(UUID estudianteId) {
        return jpa.findById(estudianteId).map(CuentaEntidad::aDominio);
    }

    @Override
    public List<Cuenta> listar() {
        return jpa.findAll().stream().map(CuentaEntidad::aDominio).toList();
    }

    @Override
    public void eliminar(UUID estudianteId) {
        jpa.deleteById(estudianteId);
    }

    @Override
    public Optional<Cuenta> buscarPorCorreo(String correo) {
        return jpa.findByCorreo(correo).map(CuentaEntidad::aDominio);
    }

    @Override
    public boolean existeCorreo(String correo) {
        return jpa.existsByCorreo(correo);
    }
}
