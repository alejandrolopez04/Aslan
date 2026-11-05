package co.edu.udea.aslan.usuarios.dominio;

import java.util.Optional;

public record TokenFijo(String valor) implements IServicioTokens {

    @Override
    public String firmar(ReclamosToken reclamos) {
        return valor;
    }

    @Override
    public Optional<ReclamosToken> leer(String token) {
        return Optional.empty();
    }
}
