package ar.edu.unlp.dssd.rescuesync.emergencia.dto;

import ar.edu.unlp.dssd.rescuesync.emergencia.Emergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.EstadoEmergencia;
import ar.edu.unlp.dssd.rescuesync.emergencia.Gravedad;
import ar.edu.unlp.dssd.rescuesync.emergencia.TipoDesastre;
import ar.edu.unlp.dssd.rescuesync.organizacion.OrganizacionDto;
import java.time.Instant;

public record EmergenciaResumenDto(Long id, TipoDesastre tipoDesastre, Gravedad gravedad, String zonaAfectada,
                                   EstadoEmergencia estado, OrganizacionDto municipio, Instant createdAt,
                                   Instant fechaCierreConvocatoria) {

    public static EmergenciaResumenDto de(Emergencia e) {
        return new EmergenciaResumenDto(e.getId(), e.getTipoDesastre(), e.getGravedad(), e.getZonaAfectada(),
                e.getEstado(), OrganizacionDto.de(e.getMunicipio()), e.getCreatedAt(),
                e.getFechaCierreConvocatoria());
    }
}
