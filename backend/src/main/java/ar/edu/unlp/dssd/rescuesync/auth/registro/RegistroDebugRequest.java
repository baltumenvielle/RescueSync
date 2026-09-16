package ar.edu.unlp.dssd.rescuesync.auth.registro;

import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta de una cuenta de prueba. Se elige una organización existente ({@code organizacionId})
 * o se crea una nueva del tipo que corresponde al rol ({@code nuevaOrganizacion}).
 */
public record RegistroDebugRequest(
        @NotBlank(message = "Ingrese su nombre")
        @Size(max = 200, message = "El nombre admite hasta 200 caracteres") String nombre,
        @NotBlank(message = "Ingrese un email")
        @Email(message = "El email no es válido")
        @Size(max = 200, message = "El email admite hasta 200 caracteres") String email,
        @NotBlank(message = "Elija un nombre de usuario")
        @Pattern(regexp = "[a-z0-9._-]{3,60}",
                message = "Entre 3 y 60 caracteres: minúsculas, números, punto, guion o guion bajo") String username,
        @NotBlank(message = "Elija una contraseña")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
        @NotNull(message = "Elija un perfil") Rol rol,
        Long organizacionId,
        @Size(max = 200, message = "El nombre admite hasta 200 caracteres") String nuevaOrganizacion) {

    @AssertTrue(message = "Elija una organización existente o indique el nombre de una nueva")
    boolean isOrganizacionIndicada() {
        boolean existente = organizacionId != null;
        boolean nueva = nuevaOrganizacion != null && !nuevaOrganizacion.isBlank();
        return existente ^ nueva;
    }
}
