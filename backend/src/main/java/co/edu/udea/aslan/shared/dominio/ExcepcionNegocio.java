package co.edu.udea.aslan.shared.dominio;

public abstract class ExcepcionNegocio extends RuntimeException {

    private final String codigo;
    private final TipoError tipo;

    protected ExcepcionNegocio(String codigo, TipoError tipo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
        this.tipo = tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoError getTipo() {
        return tipo;
    }
}
