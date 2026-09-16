package ar.edu.unlp.dssd.rescuesync.organizacion;

public record OrganizacionDto(Long id, String nombre, TipoOrganizacion tipo) {

    public static OrganizacionDto de(Organizacion o) {
        return new OrganizacionDto(o.getId(), o.getNombre(), o.getTipo());
    }
}
