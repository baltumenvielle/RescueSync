package ar.edu.unlp.dssd.rescuesync.auth;

import ar.edu.unlp.dssd.rescuesync.organizacion.OrganizacionDto;
import ar.edu.unlp.dssd.rescuesync.usuario.Rol;
import ar.edu.unlp.dssd.rescuesync.usuario.Usuario;

public record UsuarioDto(Long id, String username, String nombre, String email, Rol rol,
                         OrganizacionDto organizacion) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getUsername(), u.getNombre(), u.getEmail(), u.getRol(),
                OrganizacionDto.de(u.getOrganizacion()));
    }
}
