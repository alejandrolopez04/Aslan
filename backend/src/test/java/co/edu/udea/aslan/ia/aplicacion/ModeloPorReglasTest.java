package co.edu.udea.aslan.ia.aplicacion;

import co.edu.udea.aslan.ia.dominio.PerfilAprendizaje;
import co.edu.udea.aslan.shared.dominio.EventoSesionFinalizada;
import co.edu.udea.aslan.shared.dominio.IRepositorio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ModeloPorReglasTest {

    private final UUID estudiante = UUID.randomUUID();
    private final TemaSugerencia temaAlto = new TemaSugerencia(UUID.randomUUID(), "Óptica", "ALTA");

    @SuppressWarnings("unchecked")
    private final IRepositorio<PerfilAprendizaje> perfiles = mock(IRepositorio.class);
    private final ModeloPorReglas modelo = new ModeloPorReglas(perfiles);

    @Test
    void sinSesionesNoTieneDatosSuficientes() {
        when(perfiles.buscarPorId(estudiante)).thenReturn(Optional.empty());

        assertThat(modelo.tieneDatosSuficientes(estudiante)).isFalse();
    }

    @Test
    void sugiereElAmbienteConMasSesionesCompletadas() {
        when(perfiles.buscarPorId(estudiante)).thenReturn(Optional.of(
                new PerfilAprendizaje(estudiante, 3, Map.of("LOFI", 0.5, "NATURALEZA", 2.0))));

        assertThat(modelo.tieneDatosSuficientes(estudiante)).isTrue();
        assertThat(modelo.sugerirAmbiente(estudiante, temaAlto)).isEqualTo("NATURALEZA");
    }

    @Test
    void sinPreferenciaDeAmbienteUsaLaPrioridadDelTema() {
        when(perfiles.buscarPorId(estudiante)).thenReturn(Optional.of(new PerfilAprendizaje(estudiante, 1, Map.of())));

        assertThat(modelo.sugerirAmbiente(estudiante, temaAlto)).isEqualTo("RUIDO_BLANCO");
        assertThat(modelo.sugerirTecnica(estudiante, temaAlto).nombre()).isEqualTo("Recuerdo activo");
        assertThat(modelo.sugerirTecnica(estudiante, new TemaSugerencia(null, "x", "OTRA")).nombre())
                .isEqualTo("Práctica intercalada");
    }

    @Test
    void aprendeDeCadaSesionFinalizada() {
        when(perfiles.buscarPorId(estudiante)).thenReturn(Optional.empty());

        modelo.alFinalizarSesion(new EventoSesionFinalizada(estudiante, UUID.randomUUID(), "LOFI", true, 25,
                Instant.now()));

        ArgumentCaptor<PerfilAprendizaje> perfil = ArgumentCaptor.forClass(PerfilAprendizaje.class);
        verify(perfiles).guardar(perfil.capture());
        assertThat(perfil.getValue().tieneDatosSuficientes()).isTrue();
        assertThat(perfil.getValue().ambientePreferido()).contains("LOFI");
    }
}
