package co.edu.udea.aslan.usuarios.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface CuentaJpaRepositorio extends JpaRepository<CuentaEntidad, UUID> {

    boolean existsByCorreo(String correo);

    Optional<CuentaEntidad> findByCorreo(String correo);
}
