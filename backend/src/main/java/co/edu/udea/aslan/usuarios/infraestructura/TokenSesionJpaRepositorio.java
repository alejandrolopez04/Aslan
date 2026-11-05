package co.edu.udea.aslan.usuarios.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;

interface TokenSesionJpaRepositorio extends JpaRepository<TokenSesionEntidad, String> {
}
