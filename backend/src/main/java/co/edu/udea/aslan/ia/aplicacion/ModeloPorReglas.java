package co.edu.udea.aslan.ia.aplicacion;

import co.edu.udea.aslan.ia.dominio.PerfilAprendizaje;
import co.edu.udea.aslan.shared.dominio.EventoSesionFinalizada;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ModeloPorReglas implements IModeloPredictivo {

    private static final String PRIORIDAD_POR_DEFECTO = "MEDIA";
    private static final Map<String, String> AMBIENTE_POR_PRIORIDAD = Map.of(
            "ALTA", "RUIDO_BLANCO",
            "MEDIA", "LOFI",
            "BAJA", "NATURALEZA");
    private static final Map<String, TecnicaSugerida> TECNICA_POR_PRIORIDAD = Map.of(
            "ALTA", new TecnicaSugerida("Recuerdo activo",
                    "Es un tema prioritario: intenta recordar lo estudiado sin mirar tus apuntes."),
            "MEDIA", new TecnicaSugerida("Práctica intercalada",
                    "Alterna ejercicios de distinto tipo para afianzar el tema."),
            "BAJA", new TecnicaSugerida("Técnica Feynman",
                    "Explica el tema con tus palabras para descubrir lo que aún no dominas."));

    private final IRepositorio<PerfilAprendizaje> perfiles;

    public ModeloPorReglas(IRepositorio<PerfilAprendizaje> perfiles) {
        this.perfiles = perfiles;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneDatosSuficientes(UUID estudianteId) {
        return perfiles.buscarPorId(estudianteId).map(PerfilAprendizaje::tieneDatosSuficientes).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public String sugerirAmbiente(UUID estudianteId, TemaSugerencia tema) {
        return perfiles.buscarPorId(estudianteId)
                .flatMap(PerfilAprendizaje::ambientePreferido)
                .orElseGet(() -> AMBIENTE_POR_PRIORIDAD.get(prioridadDe(tema)));
    }

    @Override
    public TecnicaSugerida sugerirTecnica(UUID estudianteId, TemaSugerencia tema) {
        return TECNICA_POR_PRIORIDAD.get(prioridadDe(tema));
    }

    @EventListener
    @Transactional
    public void alFinalizarSesion(EventoSesionFinalizada evento) {
        PerfilAprendizaje perfil = perfiles.buscarPorId(evento.estudianteId())
                .orElseGet(() -> PerfilAprendizaje.nuevo(evento.estudianteId()));
        perfil.aprender(evento.ambiente(), evento.completada());
        perfiles.guardar(perfil);
    }

    private static String prioridadDe(TemaSugerencia tema) {
        return AMBIENTE_POR_PRIORIDAD.containsKey(tema.prioridad()) ? tema.prioridad() : PRIORIDAD_POR_DEFECTO;
    }
}
