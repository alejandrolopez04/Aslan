package co.edu.udea.aslan.shared.infraestructura;

import co.edu.udea.aslan.shared.aplicacion.IVerificadorBaseDatos;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class VerificadorBaseDatosJdbc implements IVerificadorBaseDatos {

    private final JdbcTemplate jdbcTemplate;

    public VerificadorBaseDatosJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean estaDisponible() {
        try {
            Integer uno = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(uno);
        } catch (DataAccessException e) {
            return false;
        }
    }
}
