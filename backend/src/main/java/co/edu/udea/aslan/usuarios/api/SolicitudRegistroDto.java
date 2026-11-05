package co.edu.udea.aslan.usuarios.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitudRegistroDto(
        @NotBlank(message = "Ingresa tu nombre.")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
        String nombre,

        @NotBlank(message = "Ingresa un correo electrónico válido.")
        @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Ingresa un correo electrónico válido.")
        @Size(max = 254, message = "Ingresa un correo electrónico válido.")
        String correo,

        @NotNull(message = "La contraseña no cumple todos los requisitos.")
        String contrasena) {
}
