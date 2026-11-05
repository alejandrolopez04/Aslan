package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.TokenSesion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tokens_sesion")
class TokenSesionEntidad {

    @Id
    @Column(length = 64)
    private String token;

    @Column(name = "cuenta_id", nullable = false)
    private UUID cuentaId;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    protected TokenSesionEntidad() {
    }

    static TokenSesionEntidad desde(TokenSesion token) {
        TokenSesionEntidad entidad = new TokenSesionEntidad();
        entidad.token = token.valor();
        entidad.cuentaId = token.cuentaId();
        entidad.creadoEn = token.creadoEn();
        entidad.expiraEn = token.expiraEn();
        return entidad;
    }

    TokenSesion aDominio() {
        return new TokenSesion(token, cuentaId, creadoEn, expiraEn);
    }
}
