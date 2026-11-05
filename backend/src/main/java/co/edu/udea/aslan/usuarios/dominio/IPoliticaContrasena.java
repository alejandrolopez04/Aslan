package co.edu.udea.aslan.usuarios.dominio;

public interface IPoliticaContrasena {

    ResultadoValidacion validar(String contrasena);

    String describirRequisitos();
}
