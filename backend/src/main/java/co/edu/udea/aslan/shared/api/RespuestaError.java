package co.edu.udea.aslan.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public record RespuestaError(
        String codigo,
        String mensaje,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, Object> detalles) {

    public RespuestaError(String codigo, String mensaje) {
        this(codigo, mensaje, Map.of());
    }
}
