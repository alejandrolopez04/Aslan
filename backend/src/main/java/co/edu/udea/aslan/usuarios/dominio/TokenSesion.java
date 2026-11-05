package co.edu.udea.aslan.usuarios.dominio;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TokenSesion(String valor, UUID cuentaId, Instant creadoEn, Instant expiraEn) {

    static final Duration VIGENCIA = Duration.ofHours(2);

    public TokenSesion {
        Objects.requireNonNull(valor);
        Objects.requireNonNull(cuentaId);
        Objects.requireNonNull(creadoEn);
        Objects.requireNonNull(expiraEn);
    }

    public static TokenSesion emitir(String valor, UUID cuentaId, Instant ahora) {
        return new TokenSesion(valor, cuentaId, ahora, ahora.plus(VIGENCIA));
    }

    public boolean estaVigente(Instant ahora) {
        return ahora.isBefore(expiraEn);
    }
}
