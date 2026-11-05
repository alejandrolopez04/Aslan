package co.edu.udea.aslan.sesiones.aplicacion;

import co.edu.udea.aslan.ia.aplicacion.IModeloPredictivo;
import co.edu.udea.aslan.ia.aplicacion.TecnicaSugerida;
import co.edu.udea.aslan.materias.aplicacion.IConsultaTemas;
import co.edu.udea.aslan.materias.aplicacion.TemaConsultado;
import co.edu.udea.aslan.sesiones.dominio.ConfiguracionPomodoro;
import co.edu.udea.aslan.sesiones.dominio.ConfirmacionRequeridaException;
import co.edu.udea.aslan.sesiones.dominio.EstadoSesion;
import co.edu.udea.aslan.sesiones.dominio.IObservadorSesion;
import co.edu.udea.aslan.sesiones.dominio.ISesionEstudioRepositorio;
import co.edu.udea.aslan.sesiones.dominio.RegistroSesion;
import co.edu.udea.aslan.sesiones.dominio.SeleccionIncompletaException;
import co.edu.udea.aslan.sesiones.dominio.SesionEnCursoException;
import co.edu.udea.aslan.sesiones.dominio.SesionEstudio;
import co.edu.udea.aslan.sesiones.dominio.SesionNoEncontradaException;
import co.edu.udea.aslan.sesiones.dominio.TipoSonido;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioSesionEstudioTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");

    private final UUID estudiante = UUID.randomUUID();
    private final UUID materia = UUID.randomUUID();
    private final UUID tema = UUID.randomUUID();
    private final TemaConsultado temaConsultado = new TemaConsultado(materia, "Física", tema, "Óptica", "ALTA");

    @Mock
    private ISesionEstudioRepositorio sesiones;

    @Mock
    private IRepositorio<RegistroSesion> registros;

    @Mock
    private ServicioConfiguracionPomodoro configuraciones;

    @Mock
    private IConsultaTemas temas;

    @Mock
    private IModeloPredictivo modelo;

    @Mock
    private IObservadorSesion observador;

    private ServicioSesionEstudio servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioSesionEstudio(sesiones, registros, configuraciones, temas, modelo, List.of(observador),
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Nested
    class HU05IniciarSesionDeEstudio {

        @Test
        void conHistorialSugiereAmbienteYTecnicaSegunSuPerfil() {
            when(temas.buscarTema(estudiante, materia, tema)).thenReturn(Optional.of(temaConsultado));
            when(modelo.tieneDatosSuficientes(estudiante)).thenReturn(true);
            when(modelo.sugerirAmbiente(any(), any())).thenReturn("RUIDO_BLANCO");
            when(modelo.sugerirTecnica(any(), any())).thenReturn(new TecnicaSugerida("Recuerdo activo", "motivo"));

            Sugerencia sugerencia = servicio.sugerir(estudiante, materia, tema);

            assertThat(sugerencia.personalizada()).isTrue();
            assertThat(sugerencia.ambiente()).isEqualTo(TipoSonido.RUIDO_BLANCO);
            assertThat(sugerencia.tecnica()).isEqualTo("Recuerdo activo");
        }

        @Test
        void sinHistorialIniciaConConfiguracionEstandarSinSugerencias() {
            prepararInicio(false);

            SesionEstudio sesion = servicio.iniciarSesion(estudiante, materia, tema, null);

            assertThat(sesion.getAmbiente()).contains(TipoSonido.SILENCIO);
            assertThat(sesion.getTecnica()).contains("Pomodoro clásico");
            assertThat(sesion.cicloActual().getDuracionPlaneadaSeg()).isEqualTo(25 * 60);
            verify(sesiones).guardar(sesion);
        }

        @Test
        void elAmbienteElegidoPorElEstudianteTienePrioridad() {
            prepararInicio(false);

            SesionEstudio sesion = servicio.iniciarSesion(estudiante, materia, tema, TipoSonido.NATURALEZA);

            assertThat(sesion.getAmbiente()).contains(TipoSonido.NATURALEZA);
        }

        @Test
        void seleccionIncompletaIndicaLosCamposFaltantes() {
            assertThatThrownBy(() -> servicio.iniciarSesion(estudiante, null, null, null))
                    .isInstanceOf(SeleccionIncompletaException.class)
                    .hasMessage("Falta seleccionar la materia y el tema.")
                    .satisfies(e -> assertThat(((SeleccionIncompletaException) e).getDetalles())
                            .containsEntry("faltantes", List.of("materia", "tema")));
            assertThatThrownBy(() -> servicio.iniciarSesion(estudiante, materia, null, null))
                    .hasMessage("Falta seleccionar el tema.");
            verifyNoInteractions(sesiones);
        }

        @Test
        void noPermiteDosSesionesAbiertas() {
            when(temas.buscarTema(estudiante, materia, tema)).thenReturn(Optional.of(temaConsultado));
            when(sesiones.buscarAbierta(estudiante)).thenReturn(Optional.of(nuevaSesion()));

            assertThatThrownBy(() -> servicio.iniciarSesion(estudiante, materia, tema, null))
                    .isInstanceOf(SesionEnCursoException.class);
        }
    }

    @Nested
    class HU07EjecutarPomodoro {

        @Test
        void registraLosCambiosDeEstadoDelCiclo() {
            SesionEstudio sesion = prepararSesionGuardada();

            servicio.ejecutarAccion(estudiante, sesion.getId(), AccionCiclo.INICIAR, 0);
            servicio.ejecutarAccion(estudiante, sesion.getId(), AccionCiclo.PAUSAR, 420);

            assertThat(sesion.getEstado()).isEqualTo(EstadoSesion.PAUSADA);
            assertThat(sesion.cicloActual().getTranscurridoSeg()).isEqualTo(420);
            verify(sesiones, org.mockito.Mockito.times(2)).guardar(sesion);
        }

        @Test
        void sesionDeOtroEstudianteNoSeEncuentra() {
            SesionEstudio sesion = prepararSesionGuardada();

            assertThatThrownBy(() -> servicio.ejecutarAccion(UUID.randomUUID(), sesion.getId(), AccionCiclo.INICIAR, 0))
                    .isInstanceOf(SesionNoEncontradaException.class);
        }
    }

    @Nested
    class HU09FinalizarSesion {

        @Test
        void conTiempoDeCicloActivoPideConfirmacion() {
            SesionEstudio sesion = prepararSesionGuardada();
            sesion.iniciarCiclo();

            assertThatThrownBy(() -> servicio.finalizarSesion(estudiante, sesion.getId(), 300, false, true))
                    .isInstanceOf(ConfirmacionRequeridaException.class);
            assertThat(sesion.estaAbierta()).isTrue();
        }

        @Test
        void alConfirmarGuardaElProgresoYNotificaALosObservadores() {
            SesionEstudio sesion = prepararSesionGuardada();
            sesion.iniciarCiclo();

            servicio.finalizarSesion(estudiante, sesion.getId(), 600, true, true);

            RegistroSesion registro = sesion.getRegistro().orElseThrow();
            assertThat(registro.duracionEfectivaMin()).isEqualTo(10);
            verify(registros).guardar(registro);
            verify(observador).alFinalizarSesion(registro);
        }

        @Test
        void sinGuardarProgresoNoNotifica() {
            SesionEstudio sesion = prepararSesionGuardada();
            sesion.iniciarCiclo();

            servicio.finalizarSesion(estudiante, sesion.getId(), 600, true, false);

            assertThat(sesion.estaAbierta()).isFalse();
            verify(registros, never()).guardar(any());
            verifyNoInteractions(observador);
        }

        @Test
        void elUltimoCicloFinalizaLaSesionAutomaticamenteYNotifica() {
            ConfiguracionPomodoro unCiclo = new ConfiguracionPomodoro(estudiante, 25, 5, 15, 1, true);
            SesionEstudio sesion = SesionEstudio.iniciar(estudiante, materia, tema, null, null, unCiclo, AHORA);
            when(sesiones.buscarPorId(sesion.getId())).thenReturn(Optional.of(sesion));
            when(configuraciones.obtener(estudiante)).thenReturn(unCiclo);
            sesion.iniciarCiclo();

            servicio.ejecutarAccion(estudiante, sesion.getId(), AccionCiclo.SIGUIENTE, 25 * 60);

            assertThat(sesion.getEstado()).isEqualTo(EstadoSesion.FINALIZADA);
            verify(observador).alFinalizarSesion(sesion.getRegistro().orElseThrow());
        }
    }

    private void prepararInicio(boolean conHistorial) {
        when(temas.buscarTema(estudiante, materia, tema)).thenReturn(Optional.of(temaConsultado));
        when(sesiones.buscarAbierta(estudiante)).thenReturn(Optional.empty());
        when(modelo.tieneDatosSuficientes(estudiante)).thenReturn(conHistorial);
        when(configuraciones.obtener(estudiante)).thenReturn(ConfiguracionPomodoro.estandar(estudiante));
    }

    private SesionEstudio nuevaSesion() {
        return SesionEstudio.iniciar(estudiante, materia, tema, TipoSonido.LOFI, "Pomodoro clásico",
                ConfiguracionPomodoro.estandar(estudiante), AHORA);
    }

    private SesionEstudio prepararSesionGuardada() {
        SesionEstudio sesion = nuevaSesion();
        when(sesiones.buscarPorId(sesion.getId())).thenReturn(Optional.of(sesion));
        org.mockito.Mockito.lenient().when(configuraciones.obtener(estudiante))
                .thenReturn(ConfiguracionPomodoro.estandar(estudiante));
        return sesion;
    }
}
