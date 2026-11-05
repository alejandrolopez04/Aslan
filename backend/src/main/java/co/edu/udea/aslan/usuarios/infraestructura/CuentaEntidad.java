package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.Cuenta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cuentas")
class CuentaEntidad {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 254)
    private String correo;

    @Column(name = "contrasena_hash", nullable = false, length = 100)
    private String contrasenaHash;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    protected CuentaEntidad() {
    }

    static CuentaEntidad desde(Cuenta cuenta) {
        CuentaEntidad entidad = new CuentaEntidad();
        entidad.id = cuenta.getId();
        entidad.nombre = cuenta.getNombre();
        entidad.correo = cuenta.getCorreo();
        entidad.contrasenaHash = cuenta.getContrasenaHash();
        entidad.creadaEn = cuenta.getCreadaEn();
        return entidad;
    }

    Cuenta aDominio() {
        return new Cuenta(id, nombre, correo, contrasenaHash, creadaEn);
    }
}
