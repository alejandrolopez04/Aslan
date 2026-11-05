package co.edu.udea.aslan.usuarios.aplicacion;

import java.time.Instant;

public record SesionIniciada(String token, Instant expiraEn, ResumenCuenta cuenta) {
}
