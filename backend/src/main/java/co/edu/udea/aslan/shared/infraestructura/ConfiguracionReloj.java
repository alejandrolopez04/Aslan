package co.edu.udea.aslan.shared.infraestructura;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
class ConfiguracionReloj {

    @Bean
    Clock reloj() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}
