package co.edu.udea.aslan.shared.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> datosInvalidos(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Los datos enviados no son válidos.");
        return responder(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", mensaje);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespuestaError> recursoNoEncontrado(NoResourceFoundException e) {
        return responder(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", "El recurso solicitado no existe.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespuestaError> metodoNoPermitido(HttpRequestMethodNotSupportedException e) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO", "El método HTTP no está permitido.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> errorInesperado(Exception e) {
        LOG.error("Error no controlado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error inesperado.");
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new RespuestaError(codigo, mensaje));
    }
}
