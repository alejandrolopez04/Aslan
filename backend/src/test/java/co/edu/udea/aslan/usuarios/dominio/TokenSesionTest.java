package co.edu.udea.aslan.usuarios.dominio;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenSesionTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");

    @Test
    void emitirCreaUnTokenQueExpiraEnDosHoras() {
        UUID cuentaId = UUID.randomUUID();

        TokenSesion token = TokenSesion.emitir("abc", cuentaId, AHORA);

        assertThat(token.cuentaId()).isEqualTo(cuentaId);
        assertThat(token.creadoEn()).isEqualTo(AHORA);
        assertThat(token.expiraEn()).isEqualTo(AHORA.plus(Duration.ofHours(2)));
    }

    @Test
    void estaVigenteHastaSuExpiracion() {
        TokenSesion token = TokenSesion.emitir("abc", UUID.randomUUID(), AHORA);

        assertThat(token.estaVigente(AHORA.plus(Duration.ofMinutes(119)))).isTrue();
        assertThat(token.estaVigente(AHORA.plus(Duration.ofHours(2)))).isFalse();
    }
}
