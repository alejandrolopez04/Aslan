package co.edu.udea.aslan.materias.dominio;

final class NombreObligatorio {

    private NombreObligatorio() {
    }

    static String validar(String nombre, String mensajeSiFalta) {
        if (nombre == null || nombre.isBlank()) {
            throw new NombreObligatorioException(mensajeSiFalta);
        }
        return nombre.trim();
    }
}
