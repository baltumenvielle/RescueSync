package ar.edu.unlp.dssd.rescuesync.oferta.dto;

import ar.edu.unlp.dssd.rescuesync.emergencia.dto.EmergenciaResumenDto;
import ar.edu.unlp.dssd.rescuesync.oferta.EstadoOferta;
import ar.edu.unlp.dssd.rescuesync.oferta.Oferta;
import ar.edu.unlp.dssd.rescuesync.organizacion.OrganizacionDto;
import java.time.Instant;

/**
 * @param editable si la ventana sigue abierta y el estado permite nuevas versiones
 */
public record OfertaDto(Long id, EmergenciaResumenDto emergencia, OrganizacionDto ong, EstadoOferta estado,
                        int versionActual, Instant createdAt, boolean editable, VersionOfertaDto vigente) {

    public static OfertaDto de(Oferta o, boolean editable) {
        return new OfertaDto(o.getId(), EmergenciaResumenDto.de(o.getEmergencia()), OrganizacionDto.de(o.getOngLider()),
                o.getEstado(), o.getVersionActual(), o.getCreatedAt(), editable, VersionOfertaDto.de(o.versionVigente()));
    }
}
