package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.SesionAutenticacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sesiones_autenticacion")
class SesionAutenticacionEntidad {

    @Id
    private UUID id;

    @Column(name = "estudiante_id", nullable = false)
    private UUID estudianteId;

    @Column(nullable = false, length = 1024)
    private String token;

    @Column(nullable = false)
    private Instant inicio;

    @Column(nullable = false)
    private Instant expiracion;

    @Column(nullable = false)
    private boolean activa;

    protected SesionAutenticacionEntidad() {
    }

    static SesionAutenticacionEntidad desde(SesionAutenticacion sesion) {
        SesionAutenticacionEntidad entidad = new SesionAutenticacionEntidad();
        entidad.id = sesion.getId();
        entidad.estudianteId = sesion.getEstudianteId();
        entidad.token = sesion.getToken();
        entidad.inicio = sesion.getInicio();
        entidad.expiracion = sesion.getExpiracion();
        entidad.activa = sesion.isActiva();
        return entidad;
    }

    SesionAutenticacion aDominio() {
        return new SesionAutenticacion(id, estudianteId, token, inicio, expiracion, activa);
    }
}
