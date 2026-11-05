package co.edu.udea.aslan.sesiones.aplicacion;

import co.edu.udea.aslan.ia.aplicacion.IModeloPredictivo;
import co.edu.udea.aslan.ia.aplicacion.TecnicaSugerida;
import co.edu.udea.aslan.ia.aplicacion.TemaSugerencia;
import co.edu.udea.aslan.materias.aplicacion.IConsultaTemas;
import co.edu.udea.aslan.materias.aplicacion.TemaConsultado;
import co.edu.udea.aslan.sesiones.dominio.ConfirmacionRequeridaException;
import co.edu.udea.aslan.sesiones.dominio.IObservadorSesion;
import co.edu.udea.aslan.sesiones.dominio.ISesionEstudioRepositorio;
import co.edu.udea.aslan.sesiones.dominio.RegistroSesion;
import co.edu.udea.aslan.sesiones.dominio.SeleccionIncompletaException;
import co.edu.udea.aslan.sesiones.dominio.SesionEnCursoException;
import co.edu.udea.aslan.sesiones.dominio.SesionEstudio;
import co.edu.udea.aslan.sesiones.dominio.SesionNoEncontradaException;
import co.edu.udea.aslan.sesiones.dominio.TemaNoDisponibleException;
import co.edu.udea.aslan.sesiones.dominio.TipoSonido;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServicioSesionEstudio {

    private final ISesionEstudioRepositorio sesiones;
    private final IRepositorio<RegistroSesion> registros;
    private final ServicioConfiguracionPomodoro configuraciones;
    private final IConsultaTemas temas;
    private final IModeloPredictivo modelo;
    private final Clock reloj;
    private final List<IObservadorSesion> observadores = new ArrayList<>();

    public ServicioSesionEstudio(ISesionEstudioRepositorio sesiones, IRepositorio<RegistroSesion> registros,
                                 ServicioConfiguracionPomodoro configuraciones, IConsultaTemas temas,
                                 IModeloPredictivo modelo, List<IObservadorSesion> observadores, Clock reloj) {
        this.sesiones = sesiones;
        this.registros = registros;
        this.configuraciones = configuraciones;
        this.temas = temas;
        this.modelo = modelo;
        this.reloj = reloj;
        observadores.forEach(this::suscribir);
    }

    public void suscribir(IObservadorSesion observador) {
        observadores.add(observador);
    }

    @Transactional(readOnly = true)
    public Sugerencia sugerir(UUID estudianteId, UUID materiaId, UUID temaId) {
        return sugerir(estudianteId, buscarTema(estudianteId, materiaId, temaId));
    }

    @Transactional
    public SesionEstudio iniciarSesion(UUID estudianteId, UUID materiaId, UUID temaId, TipoSonido ambiente) {
        TemaConsultado tema = buscarTema(estudianteId, materiaId, temaId);
        if (sesiones.buscarAbierta(estudianteId).isPresent()) {
            throw new SesionEnCursoException();
        }
        Sugerencia sugerencia = sugerir(estudianteId, tema);
        SesionEstudio sesion = SesionEstudio.iniciar(estudianteId, materiaId, temaId,
                Objects.requireNonNullElse(ambiente, sugerencia.ambiente()), sugerencia.tecnica(),
                configuraciones.obtener(estudianteId), Instant.now(reloj));
        sesiones.guardar(sesion);
        return sesion;
    }

    @Transactional(readOnly = true)
    public Optional<SesionEstudio> sesionAbierta(UUID estudianteId) {
        return sesiones.buscarAbierta(estudianteId);
    }

    @Transactional
    public SesionEstudio ejecutarAccion(UUID estudianteId, UUID sesionId, AccionCiclo accion, int transcurridoSeg) {
        SesionEstudio sesion = buscarSesion(estudianteId, sesionId);
        accion.aplicar(sesion, transcurridoSeg, configuraciones.obtener(estudianteId), Instant.now(reloj));
        guardar(sesion);
        return sesion;
    }

    @Transactional
    public SesionEstudio finalizarSesion(UUID estudianteId, UUID sesionId, int transcurridoSeg, boolean confirmado,
                                         boolean guardarProgreso) {
        SesionEstudio sesion = buscarSesion(estudianteId, sesionId);
        if (sesion.tieneCicloActivo() && !confirmado) {
            throw new ConfirmacionRequeridaException();
        }
        sesion.finalizar(guardarProgreso, transcurridoSeg, Instant.now(reloj));
        guardar(sesion);
        return sesion;
    }

    private void guardar(SesionEstudio sesion) {
        sesiones.guardar(sesion);
        sesion.getRegistro().ifPresent(registro -> {
            registros.guardar(registro);
            notificarFinalizacion(registro);
        });
    }

    private void notificarFinalizacion(RegistroSesion registro) {
        observadores.forEach(observador -> observador.alFinalizarSesion(registro));
    }

    private Sugerencia sugerir(UUID estudianteId, TemaConsultado tema) {
        if (!modelo.tieneDatosSuficientes(estudianteId)) {
            return Sugerencia.ESTANDAR;
        }
        TemaSugerencia consulta = new TemaSugerencia(tema.temaId(), tema.tema(), tema.prioridad());
        TecnicaSugerida tecnica = modelo.sugerirTecnica(estudianteId, consulta);
        return new Sugerencia(true, TipoSonido.valueOf(modelo.sugerirAmbiente(estudianteId, consulta)),
                tecnica.nombre(), tecnica.motivo());
    }

    private TemaConsultado buscarTema(UUID estudianteId, UUID materiaId, UUID temaId) {
        List<String> faltantes = new ArrayList<>();
        if (materiaId == null) {
            faltantes.add("materia");
        }
        if (temaId == null) {
            faltantes.add("tema");
        }
        if (!faltantes.isEmpty()) {
            throw new SeleccionIncompletaException(faltantes);
        }
        return temas.buscarTema(estudianteId, materiaId, temaId).orElseThrow(TemaNoDisponibleException::new);
    }

    private SesionEstudio buscarSesion(UUID estudianteId, UUID sesionId) {
        return sesiones.buscarPorId(sesionId)
                .filter(sesion -> sesion.perteneceA(estudianteId))
                .orElseThrow(SesionNoEncontradaException::new);
    }
}
