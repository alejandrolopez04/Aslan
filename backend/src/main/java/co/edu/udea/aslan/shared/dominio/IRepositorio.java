package co.edu.udea.aslan.shared.dominio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRepositorio<T> {

    void guardar(T entidad);

    Optional<T> buscarPorId(UUID id);

    List<T> listar();

    void eliminar(UUID id);
}
