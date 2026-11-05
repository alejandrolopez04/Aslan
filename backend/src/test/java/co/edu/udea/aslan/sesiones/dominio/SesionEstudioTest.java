package co.edu.udea.aslan.sesiones.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SesionEstudioTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");
    private static final int TRABAJO = 25 * 60;

    private final ConfiguracionPomodoro cfg = ConfiguracionPomodoro.estandar(UUID.randomUUID());
    private SesionEstudio sesion;

    @BeforeEach
    void iniciar() {
        sesion = SesionEstudio.iniciar(cfg.getEstudianteId(), UUID.randomUUID(), UUID.randomUUID(),
                TipoSonido.LOFI, "Recuerdo activo", cfg, AHORA);
    }

    @Test
    void creaElPrimerCicloDeTrabajoConLaDuracionConfigurada() {
        assertThat(sesion.cicloActual().getTipo()).isEqualTo(TipoCiclo.TRABAJO);
        assertThat(sesion.cicloActual().getDuracionPlaneadaSeg()).isEqualTo(TRABAJO);
        assertThat(sesion.cicloActual().getEstado()).isEqualTo(EstadoCiclo.NO_INICIADO);
        assertThat(sesion.getCiclosPlaneados()).isEqualTo(4);
    }

    @Nested
    class HU07EjecutarPomodoro {

        @Test
        void pausarYReanudarRetomaDesdeDondeSePauso() {
            sesion.iniciarCiclo();
            sesion.pausar(600);
            assertThat(sesion.getEstado()).isEqualTo(EstadoSesion.PAUSADA);

            sesion.reanudar();

            assertThat(sesion.getEstado()).isEqualTo(EstadoSesion.EN_CURSO);
            assertThat(sesion.cicloActual().getEstado()).isEqualTo(EstadoCiclo.EN_CURSO);
            assertThat(sesion.cicloActual().getTranscurridoSeg()).isEqualTo(600);
        }

        @Test
        void alAgotarseElTrabajoIniciaElDescansoCortoAutomaticamente() {
            sesion.iniciarCiclo();
            sesion.avanzarCiclo(TRABAJO, cfg, AHORA);

            assertThat(sesion.cicloActual().getTipo()).isEqualTo(TipoCiclo.DESCANSO_CORTO);
            assertThat(sesion.cicloActual().getEstado()).isEqualTo(EstadoCiclo.EN_CURSO);
            assertThat(sesion.cicloActual().getDuracionPlaneadaSeg()).isEqualTo(5 * 60);
            assertThat(sesion.ciclosCompletados()).isEqualTo(1);
        }

        @Test
        void trasElDescansoElSiguienteTrabajoEsperaAQueElEstudianteLoInicie() {
            completarTrabajo();
            sesion.avanzarCiclo(5 * 60, cfg, AHORA);

            assertThat(sesion.cicloActual().getTipo()).isEqualTo(TipoCiclo.TRABAJO);
            assertThat(sesion.cicloActual().getNumero()).isEqualTo(2);
            assertThat(sesion.cicloActual().getEstado()).isEqualTo(EstadoCiclo.NO_INICIADO);
        }

        @Test
        void elDescansoEsLargoCadaCiclosParaDescansoLargo() {
            ConfiguracionPomodoro dosCiclos = new ConfiguracionPomodoro(cfg.getEstudianteId(), 25, 5, 15, 2, true);
            SesionEstudio larga = SesionEstudio.iniciar(cfg.getEstudianteId(), UUID.randomUUID(),
                    UUID.randomUUID(), null, null, cfg, AHORA);
            larga.iniciarCiclo();
            larga.avanzarCiclo(TRABAJO, dosCiclos, AHORA);
            larga.avanzarCiclo(300, dosCiclos, AHORA);
            larga.iniciarCiclo();
            larga.avanzarCiclo(TRABAJO, dosCiclos, AHORA);

            assertThat(larga.cicloActual().getTipo()).isEqualTo(TipoCiclo.DESCANSO_LARGO);
            assertThat(larga.cicloActual().getDuracionPlaneadaSeg()).isEqualTo(15 * 60);
        }

        @Test
        void detenerAntesDeTiempoDejaElCicloDetenidoYSinContarlo() {
            sesion.iniciarCiclo();
            sesion.finalizar(true, 300, AHORA.plus(Duration.ofMinutes(5)));

            assertThat(sesion.cicloActual().getEstado()).isEqualTo(EstadoCiclo.DETENIDO);
            assertThat(sesion.ciclosCompletados()).isZero();
        }

        @Test
        void noSePuedeReanudarUnCicloQueNoEstaPausado() {
            sesion.iniciarCiclo();

            assertThatThrownBy(sesion::reanudar).isInstanceOf(AccionNoPermitidaException.class);
            assertThatThrownBy(() -> sesion.pausar(-1)).isInstanceOf(AccionNoPermitidaException.class);
        }
    }

    @Nested
    class HU09FinalizarSesion {

        @Test
        void alCompletarTodosLosCiclosPlaneadosFinalizaAutomaticamente() {
            for (int ciclo = 1; ciclo < 4; ciclo++) {
                completarTrabajo();
                sesion.avanzarCiclo(300, cfg, AHORA);
            }
            sesion.iniciarCiclo();
            sesion.avanzarCiclo(TRABAJO, cfg, AHORA.plus(Duration.ofHours(2)));

            assertThat(sesion.estaAbierta()).isFalse();
            assertThat(sesion.getEstado()).isEqualTo(EstadoSesion.FINALIZADA);
            RegistroSesion registro = sesion.getRegistro().orElseThrow();
            assertThat(registro.completada()).isTrue();
            assertThat(registro.ciclosCompletados()).isEqualTo(4);
            assertThat(registro.duracionEfectivaMin()).isEqualTo(100);
        }

        @Test
        void finalizarAnticipadamenteGuardaElProgresoAlcanzado() {
            completarTrabajo();
            sesion.avanzarCiclo(300, cfg, AHORA);
            sesion.iniciarCiclo();

            RegistroSesion registro = sesion.finalizar(true, 600, AHORA.plus(Duration.ofMinutes(40))).orElseThrow();

            assertThat(sesion.getEstado()).isEqualTo(EstadoSesion.FINALIZADA_ANTICIPADA);
            assertThat(registro.completada()).isFalse();
            assertThat(registro.ciclosCompletados()).isEqualTo(1);
            assertThat(registro.duracionEfectivaMin()).isEqualTo(35);
            assertThat(registro.ambiente()).isEqualTo(TipoSonido.LOFI);
        }

        @Test
        void sinGuardarProgresoNoGeneraRegistro() {
            sesion.iniciarCiclo();

            assertThat(sesion.finalizar(false, 120, AHORA)).isEmpty();
            assertThat(sesion.estaAbierta()).isFalse();
        }

        @Test
        void detectaSiQuedaTiempoDeCicloActivo() {
            assertThat(sesion.tieneCicloActivo()).isFalse();
            sesion.iniciarCiclo();
            assertThat(sesion.tieneCicloActivo()).isTrue();
        }

        @Test
        void unaSesionFinalizadaNoAceptaMasAcciones() {
            sesion.finalizar(true, 0, AHORA);

            assertThatThrownBy(sesion::iniciarCiclo).isInstanceOf(SesionFinalizadaException.class);
            assertThatThrownBy(() -> sesion.finalizar(true, 0, AHORA)).isInstanceOf(SesionFinalizadaException.class);
        }
    }

    private void completarTrabajo() {
        sesion.iniciarCiclo();
        sesion.avanzarCiclo(TRABAJO, cfg, AHORA);
    }
}
