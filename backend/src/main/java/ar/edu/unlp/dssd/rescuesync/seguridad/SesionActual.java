package ar.edu.unlp.dssd.rescuesync.seguridad;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** Acceso al usuario autenticado desde los servicios. */
@Component
public class SesionActual {

    public UsuarioActual usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado");
        }
        return JwtService.usuarioDe(jwt);
    }
}
