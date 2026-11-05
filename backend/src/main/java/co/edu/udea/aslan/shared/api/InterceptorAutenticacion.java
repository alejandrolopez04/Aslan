package co.edu.udea.aslan.shared.api;

import co.edu.udea.aslan.shared.aplicacion.IValidadorAcceso;
import co.edu.udea.aslan.shared.dominio.NoAutenticadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

class InterceptorAutenticacion implements HandlerInterceptor {

    static final String ATRIBUTO_ESTUDIANTE = "aslan.estudianteId";

    private final IValidadorAcceso validador;

    InterceptorAutenticacion(IValidadorAcceso validador) {
        this.validador = validador;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod) || CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        UUID estudianteId = TokenBearer.extraer(request.getHeader(HttpHeaders.AUTHORIZATION))
                .flatMap(validador::validarAcceso)
                .orElseThrow(NoAutenticadoException::new);
        request.setAttribute(ATRIBUTO_ESTUDIANTE, estudianteId);
        return true;
    }
}
