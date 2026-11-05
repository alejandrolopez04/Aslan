package co.edu.udea.aslan.usuarios.infraestructura;

import co.edu.udea.aslan.usuarios.dominio.IRepositorioTokens;
import co.edu.udea.aslan.usuarios.dominio.TokenSesion;
import org.springframework.stereotype.Repository;

@Repository
class RepositorioTokensJpa implements IRepositorioTokens {

    private final TokenSesionJpaRepositorio jpa;

    RepositorioTokensJpa(TokenSesionJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public TokenSesion guardar(TokenSesion token) {
        return jpa.save(TokenSesionEntidad.desde(token)).aDominio();
    }
}
