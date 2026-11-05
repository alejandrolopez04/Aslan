package co.edu.udea.aslan.ia.infraestructura;

import co.edu.udea.aslan.ia.dominio.PerfilAprendizaje;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "perfiles_aprendizaje")
class PerfilAprendizajeEntidad {

    @Id
    @Column(name = "estudiante_id")
    private UUID estudianteId;

    @Column(name = "sesiones_procesadas", nullable = false)
    private int sesionesProcesadas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pesos_ambiente", joinColumns = @JoinColumn(name = "estudiante_id"))
    @MapKeyColumn(name = "ambiente")
    @Column(name = "peso", nullable = false)
    private Map<String, Double> pesos = new HashMap<>();

    protected PerfilAprendizajeEntidad() {
    }

    static PerfilAprendizajeEntidad desde(PerfilAprendizaje perfil) {
        PerfilAprendizajeEntidad entidad = new PerfilAprendizajeEntidad();
        entidad.estudianteId = perfil.getEstudianteId();
        entidad.sesionesProcesadas = perfil.getSesionesProcesadas();
        entidad.pesos = new HashMap<>(perfil.getPesos());
        return entidad;
    }

    PerfilAprendizaje aDominio() {
        return new PerfilAprendizaje(estudianteId, sesionesProcesadas, pesos);
    }
}
