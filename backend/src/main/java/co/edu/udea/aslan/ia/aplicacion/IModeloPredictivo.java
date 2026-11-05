package co.edu.udea.aslan.ia.aplicacion;

import java.util.UUID;

public interface IModeloPredictivo {

    boolean tieneDatosSuficientes(UUID estudianteId);

    String sugerirAmbiente(UUID estudianteId, TemaSugerencia tema);

    TecnicaSugerida sugerirTecnica(UUID estudianteId, TemaSugerencia tema);
}
