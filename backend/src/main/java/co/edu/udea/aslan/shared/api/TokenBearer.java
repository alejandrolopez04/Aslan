package co.edu.udea.aslan.shared.api;

import java.util.Optional;

public final class TokenBearer {

    private static final String PREFIJO = "Bearer ";

    private TokenBearer() {
    }

    public static Optional<String> extraer(String encabezado) {
        if (encabezado == null || !encabezado.startsWith(PREFIJO)) {
            return Optional.empty();
        }
        return Optional.of(encabezado.substring(PREFIJO.length()).trim()).filter(token -> !token.isEmpty());
    }
}
