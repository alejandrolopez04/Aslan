package co.edu.udea.aslan.shared.api;

import co.edu.udea.aslan.shared.aplicacion.IValidadorAcceso;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
class ConfiguracionAutenticacion implements WebMvcConfigurer {

    private final IValidadorAcceso validador;

    ConfiguracionAutenticacion(IValidadorAcceso validador) {
        this.validador = validador;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InterceptorAutenticacion(validador))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/salud", "/api/auth/registro", "/api/auth/login", "/api/auth/logout");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ResolutorEstudianteActual());
    }
}
