package co.edu.udea.aslan.shared.aplicacion;

import org.springframework.stereotype.Service;

@Service
public class ServicioSalud {

    private final IVerificadorBaseDatos verificadorBaseDatos;

    public ServicioSalud(IVerificadorBaseDatos verificadorBaseDatos) {
        this.verificadorBaseDatos = verificadorBaseDatos;
    }

    public EstadoSalud consultarEstado() {
        return EstadoSalud.con(verificadorBaseDatos.estaDisponible());
    }
}
