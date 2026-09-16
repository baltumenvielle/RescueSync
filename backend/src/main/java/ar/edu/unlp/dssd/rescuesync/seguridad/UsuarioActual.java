package ar.edu.unlp.dssd.rescuesync.seguridad;

import ar.edu.unlp.dssd.rescuesync.usuario.Rol;

/** Datos del usuario autenticado, tomados de los claims del JWT. */
public record UsuarioActual(Long id, String username, Rol rol, Long organizacionId) {

    public boolean es(Rol otro) {
        return rol == otro;
    }
}
