package co.edu.udea.aslan.shared.infraestructura;

import co.edu.udea.aslan.shared.aplicacion.IPublicadorEventos;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class PublicadorEventosSpring implements IPublicadorEventos {

    private final ApplicationEventPublisher publicador;

    PublicadorEventosSpring(ApplicationEventPublisher publicador) {
        this.publicador = publicador;
    }

    @Override
    public void publicar(Object evento) {
        publicador.publishEvent(evento);
    }
}
