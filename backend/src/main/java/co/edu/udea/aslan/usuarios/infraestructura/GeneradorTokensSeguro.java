package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.IGeneradorTokens;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
class GeneradorTokensSeguro implements IGeneradorTokens {

    private static final int BYTES = 32;

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String generar() {
        byte[] bytes = new byte[BYTES];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
