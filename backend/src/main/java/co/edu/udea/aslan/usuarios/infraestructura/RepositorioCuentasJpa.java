package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import co.edu.udea.aslan.usuarios.dominio.IRepositorioCuentas;
import org.springframework.stereotype.Repository;

@Repository
class RepositorioCuentasJpa implements IRepositorioCuentas {

    private final CuentaJpaRepositorio jpa;

    RepositorioCuentasJpa(CuentaJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return jpa.existsByCorreo(correo);
    }

    @Override
    public Cuenta guardar(Cuenta cuenta) {
        return jpa.save(CuentaEntidad.desde(cuenta)).aDominio();
    }
}
