package co.edu.udea.aslan.ia.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface PerfilAprendizajeJpaRepositorio extends JpaRepository<PerfilAprendizajeEntidad, UUID> {
}
