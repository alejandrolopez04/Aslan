package co.edu.udea.aslan.sesiones.aplicacion;

import co.edu.udea.aslan.sesiones.dominio.IObservadorSesion;
import co.edu.udea.aslan.sesiones.dominio.RegistroSesion;
import co.edu.udea.aslan.sesiones.dominio.TipoSonido;
import co.edu.udea.aslan.shared.aplicacion.IPublicadorEventos;
import co.edu.udea.aslan.shared.dominio.EventoSesionFinalizada;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class PublicadorSesionFinalizada implements IObservadorSesion {

    private final IPublicadorEventos publicador;

    PublicadorSesionFinalizada(IPublicadorEventos publicador) {
        this.publicador = publicador;
    }

    @Override
    public void alFinalizarSesion(RegistroSesion registro) {
        publicador.publicar(new EventoSesionFinalizada(registro.estudianteId(), registro.sesionId(),
                Optional.ofNullable(registro.ambiente()).map(TipoSonido::name).orElse(null), registro.completada(),
                registro.duracionEfectivaMin(), registro.fecha()));
    }
}
