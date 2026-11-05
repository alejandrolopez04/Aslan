package co.edu.udea.aslan.usuarios.aplicacion;

import co.edu.udea.aslan.usuarios.dominio.Cuenta;

import java.util.UUID;

public record CuentaRegistrada(UUID id, String nombre, String correo) {

    static CuentaRegistrada desde(Cuenta cuenta) {
        return new CuentaRegistrada(cuenta.getId(), cuenta.getNombre(), cuenta.getCorreo());
    }
}
