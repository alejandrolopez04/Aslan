package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.IServicioHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class HasherBCrypt implements IServicioHash {

    private static final int COSTO = 10;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(COSTO);

    @Override
    public String generarHash(String texto) {
        return encoder.encode(texto);
    }

    @Override
    public boolean coincide(String texto, String hash) {
        return encoder.matches(texto, hash);
    }
}
