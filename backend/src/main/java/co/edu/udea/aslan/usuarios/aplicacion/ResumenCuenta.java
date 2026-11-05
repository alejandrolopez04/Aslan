package co.edu.udea.aslan.usuarios.aplicacion;

import co.edu.udea.aslan.usuarios.dominio.Cuenta;

import java.util.UUID;

public record ResumenCuenta(UUID id, String nombre, String correo) {

    static ResumenCuenta desde(Cuenta cuenta) {
        return new ResumenCuenta(cuenta.getId(), cuenta.getNombre(), cuenta.getCorreo());
    }
}
