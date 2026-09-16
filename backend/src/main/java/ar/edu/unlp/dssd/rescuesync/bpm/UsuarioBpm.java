package ar.edu.unlp.dssd.rescuesync.bpm;

import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;

/**
 * Datos del usuario de la app que necesita el motor de procesos. Evita pasar la entidad JPA
 * a la capa de integración.
 *
 * @param bonitaUserId puede ser nulo: la implementación real lo resuelve por
 *                     {@code bonitaUsername} si hace falta
 */
public record UsuarioBpm(Long usuarioId, String username, String bonitaUsername, Long bonitaUserId, Rol rol) {

    public static UsuarioBpm de(Usuario usuario) {
        return new UsuarioBpm(usuario.getId(), usuario.getUsername(), usuario.getBonitaUsername(),
                usuario.getBonitaUserId(), usuario.getRol());
    }
}
