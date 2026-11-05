package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.IServicioTokens;
import co.edu.udea.aslan.usuarios.dominio.ReclamosToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
class ServicioTokensJwt implements IServicioTokens {

    private final SecretKey clave;

    ServicioTokensJwt(@Value("${aslan.jwt.secreto}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String firmar(ReclamosToken reclamos) {
        return Jwts.builder()
                .id(reclamos.sesionId().toString())
                .subject(reclamos.estudianteId().toString())
                .claim("nombre", reclamos.nombre())
                .claim("correo", reclamos.correo())
                .issuedAt(Date.from(reclamos.inicio()))
                .expiration(Date.from(reclamos.expiracion()))
                .signWith(clave)
                .compact();
    }

    @Override
    public Optional<ReclamosToken> leer(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new ReclamosToken(UUID.fromString(claims.getId()),
                    UUID.fromString(claims.getSubject()), claims.get("nombre", String.class),
                    claims.get("correo", String.class), claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
