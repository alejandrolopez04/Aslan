package co.edu.udea.aslan.sesiones.aplicacion;

import co.edu.udea.aslan.sesiones.dominio.RachaEstudio;
import co.edu.udea.aslan.sesiones.dominio.RegistroSesion;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActualizadorRachaTest {

    @SuppressWarnings("unchecked")
    private final IRepositorio<RachaEstudio> rachas = mock(IRepositorio.class);

    @Test
    void alFinalizarUnaSesionActualizaLaRachaConLaFechaLocal() {
        UUID estudiante = UUID.randomUUID();
        when(rachas.buscarPorId(estudiante)).thenReturn(Optional.empty());
        ActualizadorRacha actualizador = new ActualizadorRacha(rachas,
                Clock.system(ZoneId.of("America/Bogota")));

        actualizador.alFinalizarSesion(new RegistroSesion(UUID.randomUUID(), estudiante, UUID.randomUUID(),
                UUID.randomUUID(), null, Instant.parse("2026-11-06T02:00:00Z"), 25, 1, true));

        ArgumentCaptor<RachaEstudio> racha = ArgumentCaptor.forClass(RachaEstudio.class);
        verify(rachas).guardar(racha.capture());
        assertThat(racha.getValue().getRachaActual()).isEqualTo(1);
        assertThat(racha.getValue().getUltimoDiaEstudio()).contains(LocalDate.of(2026, 11, 5));
    }
}
