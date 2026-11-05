package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.PoliticaContrasenaSegura;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ConfiguracionUsuarios {

    @Bean
    PoliticaContrasenaSegura politicaContrasenaSegura() {
        return new PoliticaContrasenaSegura();
    }
}
