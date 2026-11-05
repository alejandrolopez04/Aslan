package co.edu.udea.aslan.usuarios.dominio;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class SesionAutenticacionTest {

    private static final Instant AHORA = Instant.parse("2026-11-05T15:00:00Z");

    private final Estudiante estudiante = Estudiante.registrar("María", AHORA);
    private final Cuenta cuenta = Cuenta.abrir(estudiante.getId(), "maria@udea.edu.co", "hash");

    @Test
    void abrirFirmaUnTokenConLosDatosDelEstudianteYVigenciaDeDosHoras() {
        IServicioTokens tokens = mock(IServicioTokens.class);
        when(tokens.firmar(any())).thenReturn("jwt-firmado");

        SesionAutenticacion sesion = SesionAutenticacion.abrir(estudiante, cuenta, AHORA, tokens);

        ArgumentCaptor<ReclamosToken> reclamos = ArgumentCaptor.forClass(ReclamosToken.class);
        verify(tokens).firmar(reclamos.capture());
        assertThat(reclamos.getValue().sesionId()).isEqualTo(sesion.getId());
        assertThat(reclamos.getValue().estudianteId()).isEqualTo(estudiante.getId());
        assertThat(reclamos.getValue().nombre()).isEqualTo("María");
        assertThat(reclamos.getValue().correo()).isEqualTo("maria@udea.edu.co");
        assertThat(sesion.getToken()).isEqualTo("jwt-firmado");
        assertThat(sesion.getInicio()).isEqualTo(AHORA);
        assertThat(sesion.getExpiracion()).isEqualTo(AHORA.plus(Duration.ofHours(2)));
        assertThat(sesion.isActiva()).isTrue();
    }

    @Test
    void estaVigenteMientrasEsteActivaYNoHayaExpirado() {
        SesionAutenticacion sesion = SesionAutenticacion.abrir(estudiante, cuenta, AHORA, new TokenFijo("jwt"));

        assertThat(sesion.estaVigente(AHORA.plus(Duration.ofMinutes(119)))).isTrue();
        assertThat(sesion.estaVigente(AHORA.plus(Duration.ofHours(2)))).isFalse();
    }

    @Test
    void invalidarCierraLaSesionAunqueNoHayaExpirado() {
        SesionAutenticacion sesion = SesionAutenticacion.abrir(estudiante, cuenta, AHORA, new TokenFijo("jwt"));

        sesion.invalidar();

        assertThat(sesion.isActiva()).isFalse();
        assertThat(sesion.estaVigente(AHORA.plus(Duration.ofMinutes(1)))).isFalse();
    }
}
