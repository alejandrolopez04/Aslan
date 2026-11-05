package co.edu.udea.aslan.materias.aplicacion;

import co.edu.udea.aslan.materias.dominio.EstadoTema;
import co.edu.udea.aslan.materias.dominio.IMateriaRepositorio;
import co.edu.udea.aslan.materias.dominio.Materia;
import co.edu.udea.aslan.materias.dominio.MateriaDuplicadaException;
import co.edu.udea.aslan.materias.dominio.MateriaNoEncontradaException;
import co.edu.udea.aslan.materias.dominio.NombreObligatorioException;
import co.edu.udea.aslan.materias.dominio.Prioridad;
import co.edu.udea.aslan.materias.dominio.Tema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioMateriasTest {

    private final UUID estudiante = UUID.randomUUID();

    @Mock
    private IMateriaRepositorio materias;

    private ServicioMaterias servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioMaterias(materias);
    }

    @Test
    void crearMateriaExitosamente() {
        when(materias.existeNombre(estudiante, "Física")).thenReturn(false);

        Materia materia = servicio.crearMateria(estudiante, "Física", "blue", "science", false);

        verify(materias).guardar(materia);
        assertThat(materia.getNombre()).isEqualTo("Física");
    }

    @Test
    void nombreDuplicadoAdvierteAntesDeGuardar() {
        when(materias.existeNombre(estudiante, "Física")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crearMateria(estudiante, "Física", "blue", "science", false))
                .isInstanceOf(MateriaDuplicadaException.class);
        verify(materias, never()).guardar(any());
    }

    @Test
    void nombreDuplicadoConfirmadoSeGuarda() {
        Materia materia = servicio.crearMateria(estudiante, "Física", "blue", "science", true);

        verify(materias).guardar(materia);
    }

    @Test
    void nombreVacioNoConsultaNiGuarda() {
        assertThatThrownBy(() -> servicio.crearMateria(estudiante, "", "blue", "science", false))
                .isInstanceOf(NombreObligatorioException.class);
        verify(materias, never()).guardar(any());
    }

    @Test
    void crearTemaLoAgregaALaMateria() {
        Materia materia = Materia.crear(estudiante, "Física", "blue", "science");
        when(materias.buscarPorId(materia.getId())).thenReturn(Optional.of(materia));
        LocalDate limite = LocalDate.of(2026, 12, 1);

        Tema tema = servicio.crearTema(estudiante, materia.getId(),
                new DatosTema("Cinemática", EstadoTema.EN_PROGRESO, Prioridad.ALTA, limite, "notas"));

        assertThat(materia.getTemas()).containsExactly(tema);
        assertThat(tema.getFechaLimite()).contains(limite);
        verify(materias).guardar(materia);
    }

    @Test
    void crearTemaEnMateriaDeOtroEstudianteNoLaEncuentra() {
        Materia ajena = Materia.crear(UUID.randomUUID(), "Física", "blue", "science");
        when(materias.buscarPorId(ajena.getId())).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> servicio.crearTema(estudiante, ajena.getId(),
                new DatosTema("Cinemática", null, null, null, null)))
                .isInstanceOf(MateriaNoEncontradaException.class);
    }

    @Test
    void buscarTemaDevuelveLosDatosParaOtrosModulos() {
        Materia materia = Materia.crear(estudiante, "Física", "blue", "science");
        Tema tema = Tema.nuevo("Óptica", EstadoTema.PENDIENTE, Prioridad.BAJA, null, "");
        materia.agregarTema(tema);
        when(materias.buscarPorId(materia.getId())).thenReturn(Optional.of(materia));

        assertThat(servicio.buscarTema(estudiante, materia.getId(), tema.getId()))
                .contains(new TemaConsultado(materia.getId(), "Física", tema.getId(), "Óptica", "BAJA"));
        assertThat(servicio.buscarTema(UUID.randomUUID(), materia.getId(), tema.getId())).isEmpty();
    }
}
