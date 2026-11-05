package co.edu.udea.aslan.usuarios.api;

import jakarta.validation.constraints.NotBlank;

public record SolicitudLoginDto(
        @NotBlank(message = "Correo o contraseña inválida.") String correo,
        @NotBlank(message = "Correo o contraseña inválida.") String contrasena) {
}
