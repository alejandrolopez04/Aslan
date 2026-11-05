package co.edu.udea.aslan.sesiones.aplicacion;

import co.edu.udea.aslan.sesiones.dominio.IObservadorSesion;
import co.edu.udea.aslan.sesiones.dominio.RachaEstudio;
import co.edu.udea.aslan.sesiones.dominio.RegistroSesion;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
class ActualizadorRacha implements IObservadorSesion {

    private final IRepositorio<RachaEstudio> rachas;
    private final Clock reloj;

    ActualizadorRacha(IRepositorio<RachaEstudio> rachas, Clock reloj) {
        this.rachas = rachas;
        this.reloj = reloj;
    }

    @Override
    public void alFinalizarSesion(RegistroSesion registro) {
        RachaEstudio racha = rachas.buscarPorId(registro.estudianteId())
                .orElseGet(() -> RachaEstudio.nueva(registro.estudianteId()));
        racha.actualizarRacha(LocalDate.ofInstant(registro.fecha(), reloj.getZone()));
        rachas.guardar(racha);
    }
}
