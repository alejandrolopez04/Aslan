package co.edu.udea.aslan.usuarios.dominio;

import java.util.Optional;

public interface IServicioTokens {

    String firmar(ReclamosToken reclamos);

    Optional<ReclamosToken> leer(String token);
}
