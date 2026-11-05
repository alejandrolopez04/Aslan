package co.edu.udea.aslan.usuarios.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SesionAutenticacionJpaRepositorio extends JpaRepository<SesionAutenticacionEntidad, UUID> {
}
