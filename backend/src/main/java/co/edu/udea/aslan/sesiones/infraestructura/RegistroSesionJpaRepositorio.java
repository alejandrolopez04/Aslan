package co.edu.udea.aslan.sesiones.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface RegistroSesionJpaRepositorio extends JpaRepository<RegistroSesionEntidad, UUID> {
}
