package co.edu.udea.aslan.shared.api;

import co.edu.udea.aslan.shared.dominio.NoAutenticadoException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

class ResolutorEstudianteActual implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return parametro.hasParameterAnnotation(EstudianteActual.class) && parametro.getParameterType() == UUID.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parametro, ModelAndViewContainer contenedor,
                                  NativeWebRequest peticion, WebDataBinderFactory fabrica) {
        Object estudianteId = peticion.getAttribute(InterceptorAutenticacion.ATRIBUTO_ESTUDIANTE,
                RequestAttributes.SCOPE_REQUEST);
        if (estudianteId == null) {
            throw new NoAutenticadoException();
        }
        return estudianteId;
    }
}
