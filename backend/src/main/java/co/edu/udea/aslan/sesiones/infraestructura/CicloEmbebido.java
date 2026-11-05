package co.edu.udea.aslan.sesiones.infraestructura;

import co.edu.udea.aslan.sesiones.dominio.CicloPomodoro;
import co.edu.udea.aslan.sesiones.dominio.EstadoCiclo;
import co.edu.udea.aslan.sesiones.dominio.TipoCiclo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
class CicloEmbebido {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCiclo tipo;

    @Column(nullable = false)
    private int numero;

    @Column(name = "duracion_planeada_seg", nullable = false)
    private int duracionPlaneadaSeg;

    @Column(name = "transcurrido_seg", nullable = false)
    private int transcurridoSeg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCiclo estado;

    protected CicloEmbebido() {
    }

    static CicloEmbebido desde(CicloPomodoro ciclo) {
        CicloEmbebido embebido = new CicloEmbebido();
        embebido.tipo = ciclo.getTipo();
        embebido.numero = ciclo.getNumero();
        embebido.duracionPlaneadaSeg = ciclo.getDuracionPlaneadaSeg();
        embebido.transcurridoSeg = ciclo.getTranscurridoSeg();
        embebido.estado = ciclo.getEstado();
        return embebido;
    }

    CicloPomodoro aDominio() {
        return tipo.reconstruir(numero, duracionPlaneadaSeg, transcurridoSeg, estado);
    }
}
