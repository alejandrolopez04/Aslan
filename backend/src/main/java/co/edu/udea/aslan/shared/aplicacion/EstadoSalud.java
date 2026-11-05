package co.edu.udea.aslan.shared.aplicacion;

public record EstadoSalud(String estado, String baseDatos) {

    private static final String ARRIBA = "UP";
    private static final String ABAJO = "DOWN";

    public static EstadoSalud con(boolean baseDatosDisponible) {
        return new EstadoSalud(ARRIBA, baseDatosDisponible ? ARRIBA : ABAJO);
    }

    public boolean estaSaludable() {
        return ARRIBA.equals(baseDatos);
    }
}
