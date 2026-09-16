package ar.edu.unlp.dssd.rescuesync.usuario;

import ar.edu.unlp.dssd.rescuesync.organizacion.TipoOrganizacion;

public enum Rol {
    OPERADOR_MUNICIPAL(TipoOrganizacion.MUNICIPIO),
    CCR(TipoOrganizacion.CCR),
    REPRESENTANTE_ONG(TipoOrganizacion.ONG),
    AUDITOR(TipoOrganizacion.AUDITORIA);

    private final TipoOrganizacion tipoOrganizacion;

    Rol(TipoOrganizacion tipoOrganizacion) {
        this.tipoOrganizacion = tipoOrganizacion;
    }

    /** Tipo de organización a la que debe pertenecer un usuario con este rol. */
    public TipoOrganizacion tipoOrganizacion() {
        return tipoOrganizacion;
    }
}
