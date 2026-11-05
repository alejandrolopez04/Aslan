package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.ReclamosToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServicioTokensJwtTest {

    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-bytes!!";

    @Test
    void firmaUnJwtConLosReclamosDeLaSesion() {
        Instant inicio = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        ReclamosToken reclamos = new ReclamosToken(UUID.randomUUID(), UUID.randomUUID(), "María",
                "maria@udea.edu.co", inicio, inicio.plus(Duration.ofHours(2)));

        String token = new ServicioTokensJwt(SECRETO).firmar(reclamos);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();
        assertThat(claims.getId()).isEqualTo(reclamos.sesionId().toString());
        assertThat(claims.getSubject()).isEqualTo(reclamos.estudianteId().toString());
        assertThat(claims.get("nombre", String.class)).isEqualTo("María");
        assertThat(claims.get("correo", String.class)).isEqualTo("maria@udea.edu.co");
        assertThat(claims.getExpiration().toInstant()).isEqualTo(inicio.plus(Duration.ofHours(2)));
    }

    @Test
    void leeLosReclamosDeUnTokenFirmadoYRechazaLosAlterados() {
        Instant inicio = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        ReclamosToken reclamos = new ReclamosToken(UUID.randomUUID(), UUID.randomUUID(), "María",
                "maria@udea.edu.co", inicio, inicio.plus(Duration.ofHours(2)));
        ServicioTokensJwt servicio = new ServicioTokensJwt(SECRETO);
        String token = servicio.firmar(reclamos);

        assertThat(servicio.leer(token)).contains(reclamos);
        assertThat(servicio.leer(token + "x")).isEmpty();
        assertThat(servicio.leer("no-es-un-jwt")).isEmpty();
        assertThat(new ServicioTokensJwt("otro-secreto-distinto-con-mas-de-32-bytes").leer(token)).isEmpty();
    }
}
